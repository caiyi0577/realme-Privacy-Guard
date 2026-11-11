"""Download the compile-only API 82 dependency and verify the pinned SHA-256."""
from pathlib import Path
import hashlib
import json
import urllib.request
import zipfile


def ensure_dependency(root):
    root = Path(root)
    lock = json.loads((root / 'dependencies.json').read_text('utf-8'))['xposed_api']
    destination = root / 'libs' / 'xposed-api-82.jar'
    if destination.is_file():
        content = destination.read_bytes()
    else:
        request = urllib.request.Request(lock['download_url'], headers={'User-Agent': 'RealmePrivacyGuard-build'})
        with urllib.request.urlopen(request, timeout=40) as response:
            content = response.read(1024 * 1024 + 1)
        if len(content) > 1024 * 1024:
            raise RuntimeError('Unexpected dependency size')
    if hashlib.sha256(content).hexdigest() != lock['sha256']:
        raise RuntimeError('Xposed API checksum mismatch; refusing to compile')
    destination.parent.mkdir(parents=True, exist_ok=True)
    if not destination.is_file():
        destination.write_bytes(content)
    with zipfile.ZipFile(destination) as jar:
        if 'de/robv/android/xposed/XposedBridge.class' not in jar.namelist():
            raise RuntimeError('Xposed API archive is incomplete')
    return destination


if __name__ == '__main__':
    print(ensure_dependency(Path(__file__).resolve().parents[1]))
