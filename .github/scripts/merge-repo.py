#!/usr/bin/env python3
"""
Merge a freshly-built local repo (apk/, icon/, index.min.json) into a remote
extension-repo checkout, accounting for deleted modules. Also writes
index.json (pretty), index.min.json (minified), and index.html.

Usage:
    python merge-repo.py --remote <remote-repo-dir> --local <local-build-dir> [--delete <json-list>]
"""

import argparse
import html
import json
import shutil
from pathlib import Path


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--remote", type=Path, required=True, help="Path to the published-repo checkout")
    ap.add_argument("--local", type=Path, required=True, help="Path to the freshly-built repo (create-repo.py output)")
    ap.add_argument("--delete", required=True, help="JSON list of module ids (e.g. en.foo) to remove from the published repo")
    args = ap.parse_args()

    remote: Path = args.remote
    local: Path = args.local
    to_delete: list[str] = json.loads(args.delete)

    for module in to_delete:
        for file in remote.joinpath("apk").glob(f"tachiyomi-{module}-v*.*.*.apk"):
            print(file.name)
            file.unlink(missing_ok=True)
        for file in remote.joinpath("icon").glob(f"eu.kanade.tachiyomi.extension.{module}.png"):
            print(file.name)
            file.unlink(missing_ok=True)

    shutil.copytree(local / "apk", remote / "apk", dirs_exist_ok=True)
    shutil.copytree(local / "icon", remote / "icon", dirs_exist_ok=True)

    with (remote / "index.json").open() as f:
        remote_index = json.load(f)
    with (local / "index.min.json").open() as f:
        local_index = json.load(f)

    index = [
        item for item in remote_index
        if not any(item["pkg"].endswith(f".{module}") for module in to_delete)
    ]
    index.extend(local_index)
    index.sort(key=lambda x: x["pkg"])

    with (remote / "index.json").open("w", encoding="utf-8") as f:
        json.dump(index, f, ensure_ascii=False, indent=2)

    for item in index:
        for source in item["sources"]:
            source.pop("versionId", None)

    with (remote / "index.min.json").open("w", encoding="utf-8") as f:
        json.dump(index, f, ensure_ascii=False, separators=(",", ":"))

    with (remote / "index.html").open("w", encoding="utf-8") as f:
        f.write('<!DOCTYPE html>\n<html>\n<head>\n<meta charset="UTF-8">\n<title>apks</title>\n</head>\n<body>\n<pre>\n')
        for entry in index:
            apk = "apk/" + html.escape(entry["apk"])
            name = html.escape(entry["name"])
            f.write(f'<a href="{apk}">{name}</a>\n')
        f.write("</pre>\n</body>\n</html>\n")


if __name__ == "__main__":
    main()
