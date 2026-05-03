#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "usage: $0 <repo-dir>" >&2
    exit 2
fi
cd "$1"

git config --global user.email "156378334+keiyoushi-bot@users.noreply.github.com"
git config --global user.name "keiyoushi-bot"
git status
if [ -n "$(git status --porcelain)" ]; then
    git add .
    git commit -m "Update extensions repo"
    git push

    curl https://purge.jsdelivr.net/gh/keiyoushi/extensions@repo/index.min.json
else
    echo "No changes to commit"
fi
