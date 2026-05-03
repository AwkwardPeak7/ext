#!/usr/bin/env python3
"""
Build the extension repo artifacts from a directory of built APKs.

Boots upstream Suwayomi-Server (preview), installs every APK in repo/apk/,
pulls metadata + icons via GraphQL/REST, and writes:

    repo/index.min.json
    repo/icon/<pkg>.png

Usage:
    python create-repo.py [--apks-source ~/apk-artifacts]
"""

from __future__ import annotations

import argparse
import contextlib
import json
import os
import shutil
import signal
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
import uuid
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

PREVIEW_URL = "https://api.github.com/repos/Suwayomi/Suwayomi-Server-preview/releases/latest"
CACHE_DIR = Path(os.environ.get("XDG_CACHE_HOME", Path.home() / ".cache")) / "suwayomi-inspect"

REPO_DIR = Path("repo")
APK_DIR = REPO_DIR / "apk"
ICON_DIR = REPO_DIR / "icon"

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
      pkgName apkName name versionName versionCode isNsfw lang iconUrl isInstalled
      source { nodes { id name lang baseUrl } }
    }
  }
}
"""

SERVER_CONF = """\
server {{
    ip = "127.0.0.1"
    port = {port}
    webUIEnabled = false
    systemTrayEnabled = false
}}
"""


def log(msg: str) -> None:
    print(msg, file=sys.stderr, flush=True)


# --- HTTP helpers ----------------------------------------------------------

def encode_multipart(parts: list[tuple[str, tuple[str | None, bytes | str, str]]]) -> tuple[bytes, str]:
    """Encode a list of (name, (filename, body, content_type)) into multipart/form-data."""
    boundary = uuid.uuid4().hex
    chunks = []
    for name, (filename, body, ctype) in parts:
        header = f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"'
        if filename is not None:
            header += f'; filename="{filename}"'
        header += f'\r\nContent-Type: {ctype}\r\n\r\n'
        chunks.append(header.encode())
        chunks.append(body if isinstance(body, bytes) else body.encode())
        chunks.append(b'\r\n')
    chunks.append(f'--{boundary}--\r\n'.encode())
    return b''.join(chunks), f'multipart/form-data; boundary={boundary}'


def http_post_json(url: str, body: bytes, content_type: str, timeout: float) -> dict:
    req = urllib.request.Request(url, data=body, headers={"Content-Type": content_type})
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return json.load(r)


# --- jar acquisition -------------------------------------------------------

def fetch_jar() -> Path:
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    log("Resolving latest Suwayomi-Server-preview release...")
    req = urllib.request.Request(PREVIEW_URL, headers={"User-Agent": "suwayomi-inspect"})
    with urllib.request.urlopen(req, timeout=30) as r:
        rel = json.load(r)
    asset = next(a for a in rel["assets"] if a["name"].endswith(".jar"))
    target = CACHE_DIR / asset["name"]
    if target.exists() and target.stat().st_size == asset["size"]:
        log(f"Using cached {target.name}")
        return target
    log(f"Downloading {asset['name']} ({asset['size'] // 1_000_000} MB)...")
    dl = urllib.request.Request(asset["browser_download_url"], headers={"User-Agent": "suwayomi-inspect"})
    with urllib.request.urlopen(dl, timeout=300) as r, open(target, "wb") as f:
        shutil.copyfileobj(r, f)
    return target


# --- server lifecycle ------------------------------------------------------

@contextlib.contextmanager
def suwayomi_server(jar: Path, port: int, startup_timeout: int):
    root = Path(tempfile.mkdtemp(prefix="suwayomi-inspect-"))
    (root / "server.conf").write_text(SERVER_CONF.format(port=port))
    log_path = root / "server.log"
    proc = subprocess.Popen(
        ["java", f"-Dsuwayomi.tachidesk.config.server.rootDir={root}", "-jar", str(jar)],
        stdout=open(log_path, "wb"),
        stderr=subprocess.STDOUT,
        start_new_session=True,
    )
    base_url = f"http://127.0.0.1:{port}"
    try:
        deadline = time.time() + startup_timeout
        while time.time() < deadline:
            if proc.poll() is not None:
                tail = log_path.read_text(errors="replace")[-2000:]
                raise RuntimeError(f"Server exited (code {proc.returncode}). Tail:\n{tail}")
            try:
                with urllib.request.urlopen(f"{base_url}/api/graphql", timeout=2) as r:
                    if r.status == 200:
                        break
            except (urllib.error.URLError, ConnectionError, OSError):
                pass
            time.sleep(0.5)
        else:
            raise RuntimeError(f"Server did not become ready within {startup_timeout}s")
        yield base_url
    finally:
        if proc.poll() is None:
            with contextlib.suppress(ProcessLookupError):
                os.killpg(proc.pid, signal.SIGTERM)
            try:
                proc.wait(timeout=15)
            except subprocess.TimeoutExpired:
                with contextlib.suppress(ProcessLookupError):
                    os.killpg(proc.pid, signal.SIGKILL)
                proc.wait(timeout=5)
        shutil.rmtree(root, ignore_errors=True)


# --- parallel runner -------------------------------------------------------

def run_parallel(items, fn, label, workers: int) -> int:
    """Run fn(item) on each item in a thread pool, logging progress; return failure count."""
    items = list(items)
    failures = 0
    with ThreadPoolExecutor(max_workers=workers) as pool:
        futures = {pool.submit(fn, i): i for i in items}
        for done, fut in enumerate(as_completed(futures), 1):
            item = futures[fut]
            try:
                fut.result()
                log(f"  [{done}/{len(items)}] {label(item)}")
            except Exception as e:
                failures += 1
                log(f"  [{done}/{len(items)}] {label(item)}  FAILED: {e}")
    return failures


# --- suwayomi operations ---------------------------------------------------

def install_apk(base_url: str, apk: Path) -> None:
    operations = json.dumps({
        "query": INSTALL_MUTATION,
        "variables": {"extensionFile": None},
    })
    map_ = json.dumps({"0": ["variables.extensionFile"]})
    body, ctype = encode_multipart([
        ("operations", (None, operations, "application/json")),
        ("map", (None, map_, "application/json")),
        ("0", (apk.name, apk.read_bytes(), "application/vnd.android.package-archive")),
    ])
    result = http_post_json(f"{base_url}/api/graphql", body, ctype, timeout=120)
    if result.get("errors"):
        raise RuntimeError(result["errors"])


def list_extensions(base_url: str) -> dict[str, dict]:
    body = json.dumps({"query": LIST_QUERY}).encode()
    result = http_post_json(f"{base_url}/api/graphql", body, "application/json", timeout=60)
    if result.get("errors"):
        raise RuntimeError(result["errors"])
    return {
        e["apkName"]: e
        for e in result["data"]["extensions"]["nodes"]
        if e["isInstalled"]
    }


def download_icon(base_url: str, ext: dict) -> None:
    with urllib.request.urlopen(f"{base_url}{ext['iconUrl']}", timeout=30) as r:
        content = r.read()
    (ICON_DIR / f"{ext['pkgName']}.png").write_bytes(content)


# --- index assembly --------------------------------------------------------

def build_entry(apk: Path, ext: dict) -> dict:
    sources = [
        {"name": s["name"], "lang": s["lang"], "id": str(s["id"]), "baseUrl": s["baseUrl"] or ""}
        for s in ext["source"]["nodes"]
    ]
    # For single-source extensions, prefer the source's own lang over the manifest lang
    # unless either side is the catch-all "all" / "other".
    lang = ext["lang"]
    if len(sources) == 1:
        slang = sources[0]["lang"]
        if slang != lang and slang not in {"all", "other"} and lang not in {"all", "other"}:
            lang = slang
    return {
        "name": f"Tachiyomi: {ext['name']}",
        "pkg": ext["pkgName"],
        "apk": apk.name,
        "lang": lang,
        "code": ext["versionCode"],
        "version": ext["versionName"],
        "nsfw": int(bool(ext["isNsfw"])),
        "sources": sources,
    }


# --- driver ----------------------------------------------------------------

def collect_apks(source_dir: Path) -> None:
    shutil.rmtree(APK_DIR, ignore_errors=True)
    APK_DIR.mkdir(parents=True, exist_ok=True)
    for apk in source_dir.glob("**/*.apk"):
        shutil.move(apk, APK_DIR / apk.name.replace("-release.apk", ".apk"))


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--apks-source", type=Path, help="Move APKs from here into repo/apk/ before building")
    ap.add_argument("--port", type=int, default=4567)
    ap.add_argument("--workers", type=int, default=4)
    ap.add_argument("--startup-timeout", type=int, default=180)
    args = ap.parse_args()

    if args.apks_source:
        if not args.apks_source.is_dir():
            log(f"error: --apks-source {args.apks_source} is not a directory")
            return 2
        collect_apks(args.apks_source)
    if not APK_DIR.is_dir():
        log(f"error: {APK_DIR} is not a directory")
        return 2

    apks = sorted(APK_DIR.glob("*.apk"))
    log(f"Found {len(apks)} APK(s)")
    if not apks:
        REPO_DIR.joinpath("index.min.json").write_text("[]")
        return 0

    ICON_DIR.mkdir(parents=True, exist_ok=True)
    jar = fetch_jar()

    with suwayomi_server(jar, args.port, args.startup_timeout) as base_url:
        log("Server ready, installing extensions...")
        install_failures = run_parallel(apks, lambda a: install_apk(base_url, a), lambda a: a.name, args.workers)

        log("Querying metadata...")
        ext_by_apk = list_extensions(base_url)

        log(f"Downloading icons for {len(ext_by_apk)} extension(s)...")
        run_parallel(ext_by_apk.values(), lambda e: download_icon(base_url, e), lambda e: e["pkgName"], args.workers)

    index = []
    for apk in apks:
        ext = ext_by_apk.get(apk.name)
        if ext is None:
            log(f"warning: {apk.name} has no Suwayomi metadata")
            continue
        index.append(build_entry(apk, ext))

    REPO_DIR.joinpath("index.min.json").write_text(
        json.dumps(index, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    log(f"Wrote {len(index)} entries to {REPO_DIR / 'index.min.json'}")
    return 1 if install_failures else 0


if __name__ == "__main__":
    sys.exit(main())
