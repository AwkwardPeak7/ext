#!/usr/bin/env python3
"""
Build the extension repo artifacts from a directory of built APKs.

Optionally collects APKs from --apks-source into repo/apk/, then spins up an
upstream Suwayomi-Server (preview channel), installs every APK, and pulls all
extension/source metadata + icons from Suwayomi via GraphQL/REST. Outputs:

    repo/index.min.json
    repo/icon/<pkg>.png

Usage:
    python create-repo.py [--apks-source ~/apk-artifacts] [--port 4567] [--workers 4]
"""

from __future__ import annotations

import argparse
import json
import os
import shutil
import signal
import subprocess
import sys
import tempfile
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import httpx

GITHUB_LATEST_URL = "https://api.github.com/repos/Suwayomi/Suwayomi-Server-preview/releases/latest"
DEFAULT_CACHE_DIR = Path(os.environ.get("XDG_CACHE_HOME", Path.home() / ".cache")) / "suwayomi-inspect"

REPO_DIR = Path("repo")
REPO_APK_DIR = REPO_DIR / "apk"
REPO_ICON_DIR = REPO_DIR / "icon"

INSTALL_MUTATION = """
mutation Install($extensionFile: Upload!) {
  installExternalExtension(input: {extensionFile: $extensionFile}) {
    extension { pkgName }
  }
}
"""

LIST_QUERY = """
query {
  extensions {
    nodes {
      pkgName
      apkName
      name
      versionName
      versionCode
      isNsfw
      lang
      iconUrl
      isInstalled
      source { nodes { id name lang baseUrl } }
    }
  }
}
"""

SERVER_CONF = """\
server {{
    ip = "127.0.0.1"
    port = {port}
    authMode = NONE
    initialOpenInBrowserEnabled = false
    systemTrayEnabled = false
    debugLogsEnabled = false
    webUIEnabled = false
}}
"""


def log(msg: str) -> None:
    print(msg, file=sys.stderr, flush=True)


# --- jar acquisition -------------------------------------------------------

def fetch_latest_jar(cache_dir: Path) -> Path:
    cache_dir.mkdir(parents=True, exist_ok=True)
    log("Resolving latest Suwayomi-Server-preview release...")
    req = urllib.request.Request(GITHUB_LATEST_URL, headers={"User-Agent": "suwayomi-inspect"})
    with urllib.request.urlopen(req, timeout=30) as r:
        rel = json.load(r)
    tag = rel["tag_name"]
    asset = next((a for a in rel["assets"] if a["name"].endswith(".jar")), None)
    if asset is None:
        raise RuntimeError(f"No .jar asset on release {tag}")
    target = cache_dir / asset["name"]
    if target.exists() and target.stat().st_size == asset["size"]:
        log(f"Using cached {target.name}")
        return target
    log(f"Downloading {asset['name']} ({asset['size'] // 1_000_000} MB)...")
    tmp = target.with_suffix(target.suffix + ".part")
    dl = urllib.request.Request(asset["browser_download_url"], headers={"User-Agent": "suwayomi-inspect"})
    with urllib.request.urlopen(dl, timeout=300) as r, open(tmp, "wb") as f:
        shutil.copyfileobj(r, f)
    tmp.rename(target)
    return target


# --- server lifecycle ------------------------------------------------------

def start_server(jar: Path, root_dir: Path, port: int, log_file: Path) -> subprocess.Popen:
    (root_dir / "server.conf").write_text(SERVER_CONF.format(port=port))
    cmd = [
        "java",
        f"-Dsuwayomi.tachidesk.config.server.rootDir={root_dir}",
        "-jar",
        str(jar),
    ]
    return subprocess.Popen(
        cmd,
        stdout=open(log_file, "wb"),
        stderr=subprocess.STDOUT,
        start_new_session=True,
    )


def wait_ready(client: httpx.Client, proc: subprocess.Popen, timeout: float, log_file: Path) -> None:
    deadline = time.time() + timeout
    while time.time() < deadline:
        if proc.poll() is not None:
            tail = log_file.read_text(errors="replace")[-2000:] if log_file.exists() else ""
            raise RuntimeError(f"Server exited early (code {proc.returncode}). Tail:\n{tail}")
        try:
            r = client.get("/api/graphql", timeout=2.0)
            if r.status_code == 200:
                return
        except httpx.HTTPError:
            pass
        time.sleep(0.5)
    raise RuntimeError(f"Server did not become ready within {timeout}s")


def stop_server(proc: subprocess.Popen) -> None:
    if proc.poll() is not None:
        return
    try:
        os.killpg(proc.pid, signal.SIGTERM)
    except ProcessLookupError:
        return
    try:
        proc.wait(timeout=15)
    except subprocess.TimeoutExpired:
        os.killpg(proc.pid, signal.SIGKILL)
        proc.wait(timeout=5)


# --- graphql ---------------------------------------------------------------

def install_apk(client: httpx.Client, apk: Path) -> None:
    operations = {
        "query": INSTALL_MUTATION,
        "variables": {"extensionFile": None},
    }
    map_ = {"0": ["variables.extensionFile"]}
    parts = [
        ("operations", (None, json.dumps(operations), "application/json")),
        ("map", (None, json.dumps(map_), "application/json")),
        ("0", (apk.name, apk.read_bytes(), "application/vnd.android.package-archive")),
    ]
    r = client.post("/api/graphql", files=parts, timeout=120.0)
    r.raise_for_status()
    body = r.json()
    if body.get("errors"):
        raise RuntimeError(body["errors"])


def list_extensions(client: httpx.Client) -> dict[str, dict]:
    r = client.post("/api/graphql", json={"query": LIST_QUERY}, timeout=60.0)
    r.raise_for_status()
    body = r.json()
    if body.get("errors"):
        raise RuntimeError(f"list query failed: {body['errors']}")
    out: dict[str, dict] = {}
    for ext in body["data"]["extensions"]["nodes"]:
        if not ext["isInstalled"]:
            continue
        out[ext["apkName"]] = ext
    return out


def download_icon(client: httpx.Client, ext: dict) -> None:
    r = client.get(ext["iconUrl"], timeout=30.0)
    r.raise_for_status()
    (REPO_ICON_DIR / f"{ext['pkgName']}.png").write_bytes(r.content)


# --- index assembly --------------------------------------------------------

def build_entry(apk: Path, ext: dict) -> dict:
    sources = [
        {
            "name": s["name"],
            "lang": s["lang"],
            "id": str(s["id"]),
            "baseUrl": s["baseUrl"] or "",
        }
        for s in ext["source"]["nodes"]
    ]

    language = ext["lang"]
    if len(sources) == 1:
        source_language = sources[0]["lang"]
        if (
            source_language != language
            and source_language not in {"all", "other"}
            and language not in {"all", "other"}
        ):
            language = source_language

    return {
        "name": f"Tachiyomi: {ext['name']}",
        "pkg": ext["pkgName"],
        "apk": apk.name,
        "lang": language,
        "code": ext["versionCode"],
        "version": ext["versionName"],
        "nsfw": int(bool(ext["isNsfw"])),
        "sources": sources,
    }


# --- driver ----------------------------------------------------------------

def collect_apks(source_dir: Path) -> None:
    shutil.rmtree(REPO_APK_DIR, ignore_errors=True)
    REPO_APK_DIR.mkdir(parents=True, exist_ok=True)
    for apk in source_dir.glob("**/*.apk"):
        shutil.move(apk, REPO_APK_DIR / apk.name.replace("-release.apk", ".apk"))


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument(
        "--apks-source",
        type=Path,
        default=None,
        help="Directory of APKs to move into repo/apk/ before building. "
             "If omitted, expects APKs already in repo/apk/.",
    )
    ap.add_argument("--port", type=int, default=4567)
    ap.add_argument("--workers", type=int, default=4)
    ap.add_argument("--startup-timeout", type=int, default=180, help="Seconds to wait for server to come up")
    ap.add_argument("--cache-dir", type=Path, default=DEFAULT_CACHE_DIR, help="Where the jar is cached")
    args = ap.parse_args()

    if args.apks_source is not None:
        if not args.apks_source.is_dir():
            log(f"error: --apks-source {args.apks_source} is not a directory")
            return 2
        collect_apks(args.apks_source)

    if not REPO_APK_DIR.is_dir():
        log(f"error: {REPO_APK_DIR} is not a directory")
        return 2

    apks = sorted(REPO_APK_DIR.glob("*.apk"))
    log(f"Found {len(apks)} APK(s)")
    if not apks:
        REPO_DIR.joinpath("index.min.json").write_text("[]")
        return 0

    REPO_ICON_DIR.mkdir(parents=True, exist_ok=True)

    jar = fetch_latest_jar(args.cache_dir)
    root_dir = Path(tempfile.mkdtemp(prefix="suwayomi-inspect-"))
    log_file = root_dir / "server.log"
    log(f"rootDir: {root_dir}")

    proc = start_server(jar, root_dir, args.port, log_file)
    icon_failures: list[tuple[str, str]] = []
    try:
        with httpx.Client(base_url=f"http://127.0.0.1:{args.port}") as client:
            wait_ready(client, proc, args.startup_timeout, log_file)
            log("Server ready, installing extensions...")

            ok = 0
            failed: list[tuple[Path, str]] = []
            with ThreadPoolExecutor(max_workers=args.workers) as pool:
                futures = {pool.submit(install_apk, client, apk): apk for apk in apks}
                for i, fut in enumerate(as_completed(futures), 1):
                    apk = futures[fut]
                    try:
                        fut.result()
                        ok += 1
                    except Exception as e:
                        failed.append((apk, str(e)))
                    log(f"  [{i}/{len(apks)}] {apk.name}{'' if fut.exception() is None else '  FAILED'}")

            log(f"Installed {ok}/{len(apks)}; querying metadata...")
            ext_by_apk = list_extensions(client)

            log(f"Downloading icons for {len(ext_by_apk)} extension(s)...")
            with ThreadPoolExecutor(max_workers=args.workers) as pool:
                futures = {pool.submit(download_icon, client, e): e for e in ext_by_apk.values()}
                for fut in as_completed(futures):
                    ext = futures[fut]
                    try:
                        fut.result()
                    except Exception as e:
                        icon_failures.append((ext["pkgName"], str(e)))
    finally:
        stop_server(proc)
        shutil.rmtree(root_dir, ignore_errors=True)

    log(f"Got metadata for {len(ext_by_apk)} extension(s); building index...")
    index = []
    missing: list[Path] = []
    for apk in apks:
        ext = ext_by_apk.get(apk.name)
        if ext is None:
            missing.append(apk)
            continue
        index.append(build_entry(apk, ext))

    REPO_DIR.joinpath("index.min.json").open("w", encoding="utf-8").write(
        json.dumps(index, ensure_ascii=False, separators=(",", ":"))
    )
    log(f"Wrote {len(index)} entries to {REPO_DIR / 'index.min.json'}")

    if icon_failures:
        log(f"\n{len(icon_failures)} icon download(s) failed:")
        for pkg, err in icon_failures:
            log(f"  {pkg}: {err}")
    if missing:
        log(f"\n{len(missing)} APK(s) had no Suwayomi metadata:")
        for apk in missing:
            log(f"  {apk.name}")
    if failed:
        log(f"\n{len(failed)} APK(s) failed to install:")
        for apk, err in failed:
            log(f"  {apk.name}: {err}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
