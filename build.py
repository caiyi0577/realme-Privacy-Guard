#!/usr/bin/env python3
"""Build the module with Android SDK tools on Windows or Linux. No Gradle required."""
from pathlib import Path
import argparse
import datetime
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT / 'tools'))
from fetch_xposed_api import ensure_dependency


def run(arguments, env=None):
    result = subprocess.run([str(value) for value in arguments], cwd=ROOT, env=env,
                            capture_output=True, text=True, encoding='utf-8', errors='replace')
    if result.returncode:
        raise RuntimeError(Path(arguments[0]).name + ' failed\n' + result.stdout[-6000:] + result.stderr[-6000:])
    return result.stdout + result.stderr


def executable(name):
    java_home = os.environ.get('JAVA_HOME')
    if java_home:
        candidate = Path(java_home) / 'bin' / (name + ('.exe' if os.name == 'nt' else ''))
        if candidate.is_file():
            return candidate
    candidate = shutil.which(name)
    if not candidate:
        raise RuntimeError('JDK 17 is required: set JAVA_HOME or add ' + name + ' to PATH')
    return Path(candidate)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    modes = parser.add_mutually_exclusive_group()
    modes.add_argument('--unsigned', action='store_true', help='Build a verification APK without signing (default)')
    modes.add_argument('--signing-config', type=Path, help='Private signing JSON kept outside the repository')
    parser.add_argument('--out', type=Path, help='Build output directory')
    args = parser.parse_args()
    sdk_location = os.environ.get('ANDROID_SDK_ROOT') or os.environ.get('ANDROID_HOME')
    if not sdk_location and os.name == 'nt' and os.environ.get('LOCALAPPDATA'):
        sdk_location = str(Path(os.environ['LOCALAPPDATA']) / 'Android' / 'Sdk')
    if not sdk_location:
        raise RuntimeError('Set ANDROID_SDK_ROOT or ANDROID_HOME')
    sdk = Path(sdk_location)
    tools = sdk / 'build-tools' / '34.0.0'
    platform = sdk / 'platforms' / 'android-34' / 'android.jar'
    suffix = '.exe' if os.name == 'nt' else ''
    for item in [platform, tools / ('aapt2' + suffix), tools / ('aapt' + suffix),
                 tools / ('zipalign' + suffix), tools / 'lib' / 'd8.jar', tools / 'lib' / 'apksigner.jar']:
        if not item.is_file():
            raise RuntimeError('Missing SDK component: ' + str(item))
    java, javac = executable('java'), executable('javac')
    dependency = ensure_dependency(ROOT)
    manifest_root = ET.parse(ROOT / 'AndroidManifest.xml').getroot()
    android = '{http://schemas.android.com/apk/res/android}'
    version = manifest_root.get(android + 'versionName')
    code = int(manifest_root.get(android + 'versionCode'))
    output = (args.out or ROOT / 'build' / datetime.datetime.now().strftime('%Y%m%d-%H%M%S')).resolve()
    for folder in ['generated', 'classes', 'tests', 'dex']:
        (output / folder).mkdir(parents=True, exist_ok=True)
    print('Compiling Android resources', flush=True)
    run([tools / ('aapt2' + suffix), 'compile', '--dir', ROOT / 'res', '-o', output / 'resources.zip'])
    run([tools / ('aapt2' + suffix), 'link', '-o', output / 'base.apk', '-I', platform,
         '--manifest', ROOT / 'AndroidManifest.xml', '-R', output / 'resources.zip', '-A', ROOT / 'assets',
         '--java', output / 'generated', '--min-sdk-version', '29', '--target-sdk-version', '34', '--auto-add-overlay'])
    sources = sorted((ROOT / 'src').rglob('*.java')) + sorted((output / 'generated').rglob('*.java'))
    compile_path = os.pathsep.join(map(str, [platform, dependency]))
    print('Compiling Java and running policy checks', flush=True)
    run([javac, '-proc:none', '-nowarn', '-encoding', 'UTF-8', '--release', '8', '-classpath', compile_path,
         '-d', output / 'classes', *sources])
    test_path = os.pathsep.join(map(str, [output / 'classes', platform, dependency]))
    run([javac, '-proc:none', '-encoding', 'UTF-8', '--release', '8', '-classpath', test_path,
         '-d', output / 'tests', *sorted((ROOT / 'tests').glob('*.java'))])
    test_output = run([java, '-cp', os.pathsep.join(map(str, [output / 'tests', output / 'classes', platform, dependency])), 'PolicyTest'])
    (output / 'test-results.txt').write_text(test_output, 'utf-8')
    print(test_output.strip(), flush=True)
    with zipfile.ZipFile(output / 'classes.jar', 'w', zipfile.ZIP_DEFLATED) as jar:
        for source in sorted((output / 'classes').rglob('*.class')):
            name = source.relative_to(output / 'classes').as_posix()
            if not name.startswith('io/github/caiyi0577/rpg/'):
                raise RuntimeError('Unexpected packaged class: ' + name)
            jar.write(source, name)
    print('Generating DEX and aligning APK', flush=True)
    run([java, '-cp', tools / 'lib' / 'd8.jar', 'com.android.tools.r8.D8', '--min-api', '29',
         '--lib', platform, '--classpath', dependency, '--output', output / 'dex', output / 'classes.jar'])
    shutil.copy2(output / 'base.apk', output / 'package.apk')
    with zipfile.ZipFile(output / 'package.apk', 'a', zipfile.ZIP_DEFLATED) as apk:
        apk.write(output / 'dex' / 'classes.dex', 'classes.dex')
    unsigned = output / ('realme-privacy-guard-' + version + '-unsigned.apk')
    run([tools / ('zipalign' + suffix), '-f', '4', output / 'package.apk', unsigned])
    run([tools / ('zipalign' + suffix), '-c', '4', unsigned])
    artifact = unsigned
    digest = None
    if args.signing_config:
        config = json.loads(args.signing_config.read_text('utf-8'))
        environment = os.environ.copy()
        store_env = config.get('store_password_env', 'RPG_STORE_PASS')
        key_env = config.get('key_password_env', 'RPG_KEY_PASS')
        store_pass = config.get('password') or environment.get(store_env)
        key_pass = config.get('key_password') or environment.get(key_env) or store_pass
        if not store_pass or not key_pass:
            raise RuntimeError('Signing password environment variables are not set')
        environment['RPG_BUILD_STORE_PASS'] = store_pass
        environment['RPG_BUILD_KEY_PASS'] = key_pass
        artifact = output / ('realme-privacy-guard-' + version + '-signed.apk')
        signer = [java, '-jar', tools / 'lib' / 'apksigner.jar']
        run([*signer, 'sign', '--ks', config['keystore'], '--ks-key-alias', config['alias'],
             '--ks-pass', 'env:RPG_BUILD_STORE_PASS', '--key-pass', 'env:RPG_BUILD_KEY_PASS',
             '--out', artifact, unsigned], env=environment)
        verification = run([*signer, 'verify', '--verbose', '--print-certs', artifact])
        digest = re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)', verification).group(1)
        expected = config.get('expected_certificate_sha256')
        if expected and digest != expected.lower().replace(':', ''):
            raise RuntimeError('Signing certificate does not match expected_certificate_sha256')
        (output / 'signing-verification.txt').write_text(verification, 'utf-8')
    badging = run([tools / ('aapt' + suffix), 'dump', 'badging', artifact])
    (output / 'manifest.txt').write_text(badging, 'utf-8')
    information = {
        'version': version, 'version_code': code, 'package': manifest_root.get('package'),
        'artifact': artifact.name, 'signed': bool(args.signing_config), 'certificate_sha256': digest,
        'apk_sha256': hashlib.sha256(artifact.read_bytes()).hexdigest(),
        'xposed_api_sha256': hashlib.sha256(dependency.read_bytes()).hexdigest(),
        'policy_checks': int(re.search(r'PASS: (\d+)', test_output).group(1)),
        'compile_sdk': 34, 'target_sdk': 34, 'min_sdk': 29,
    }
    (output / 'build-info.json').write_text(json.dumps(information, ensure_ascii=False, indent=2) + '\n', 'utf-8')
    print(json.dumps(information, ensure_ascii=False), flush=True)


if __name__ == '__main__':
    main()
