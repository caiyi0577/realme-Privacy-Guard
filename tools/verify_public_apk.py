"""Verify the locally signed publication APK against a clean CI build."""
from pathlib import Path
import argparse,hashlib,json,os,re,subprocess,zipfile

ROOT=Path(__file__).resolve().parents[1]

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--unsigned',type=Path,required=True)
    args=parser.parse_args()
    metadata=json.loads((ROOT/'release/manifest.json').read_text('utf-8'))
    signed=ROOT/metadata['apk']
    assert signed.resolve().is_relative_to(ROOT)
    assert hashlib.sha256(signed.read_bytes()).hexdigest()==metadata['sha256'],'Publication APK hash mismatch'
    with zipfile.ZipFile(signed) as published,zipfile.ZipFile(args.unsigned) as compiled:
        for entry in ['classes.dex','AndroidManifest.xml','resources.arsc','assets/xposed_init']:
            assert published.read(entry)==compiled.read(entry),'Published APK does not match this source build: '+entry
    sdk=Path(os.environ.get('ANDROID_SDK_ROOT') or os.environ['ANDROID_HOME'])
    home=Path(os.environ['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java')
    result=subprocess.run([str(home),'-jar',str(sdk/'build-tools/34.0.0/lib/apksigner.jar'),'verify','--print-certs',str(signed)],capture_output=True,text=True,check=True)
    digest=re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)',result.stdout).group(1)
    assert digest==metadata['certificate_sha256'],'Unexpected signer'
    print('PASS: publication APK matches compiled DEX/resources/entry point and expected signing certificate')

if __name__=='__main__':main()
