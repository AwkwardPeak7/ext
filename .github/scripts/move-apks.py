from pathlib import Path
import shutil
import os
import glob

REPO_APK_DIR = Path("repo/apk")

try:
    shutil.rmtree(REPO_APK_DIR)
except FileNotFoundError:
    pass

shutil.copytree(src="../repo/apk", dst=REPO_APK_DIR)

toDelete = json.loads(os.getenv("DELETED_MODULES"))

for module in toDelete:
    apkName = f"tachiyomi-{module}-v1.4.*.apk"
    for p in REPO_APK_DIR.glob(apkName):
        p.unlink()

for apk in (Path.home() / "apk-artifacts").glob("**/*.apk"):
    apk_name = apk.name.replace("-release.apk", ".apk")

    shutil.move(apk, REPO_APK_DIR / apk_name)
