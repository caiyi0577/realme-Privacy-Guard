"""Publish the already signed APK; signing material never enters the runner."""
from pathlib import Path
import hashlib,json,os,urllib.error,urllib.parse,urllib.request

ROOT=Path(__file__).resolve().parents[1]
REPOSITORY='caiyi0577/realme-Privacy-Guard'

def main():
    if os.environ.get('GITHUB_REPOSITORY')!=REPOSITORY:raise RuntimeError('Release destination is restricted')
    if os.environ.get('GITHUB_REF')!='refs/heads/lsposed-release':raise RuntimeError('Release branch is required')
    token=os.environ['RELEASE_TOKEN']
    metadata=json.loads((ROOT/'release/manifest.json').read_text('utf-8'))
    apk=(ROOT/metadata['apk']).resolve()
    if not apk.is_relative_to(ROOT/'release-assets'):raise RuntimeError('Unexpected APK path')
    content=apk.read_bytes()
    if hashlib.sha256(content).hexdigest()!=metadata['sha256']:raise RuntimeError('APK integrity check failed')
    def request(method,path,value=None,binary=None):
        url=path if path.startswith('https://uploads.github.com/') else 'https://api.github.com/repos/'+REPOSITORY+path
        headers={'Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','X-GitHub-Api-Version':'2026-03-10','User-Agent':'RealmePrivacyGuard-release'}
        data=None
        if value is not None:data=json.dumps(value).encode();headers['Content-Type']='application/json'
        if binary is not None:data=binary;headers['Content-Type']='application/vnd.android.package-archive'
        try:
            with urllib.request.urlopen(urllib.request.Request(url,data=data,headers=headers,method=method),timeout=40) as response:
                return json.load(response)
        except urllib.error.HTTPError as error:
            if error.code==404 and method=='GET':return None
            raise RuntimeError('GitHub release request failed: HTTP '+str(error.code)) from None
    tag=metadata['tag']
    release=request('GET','/releases/tags/'+urllib.parse.quote(tag,safe=''))
    if release is None:
        release=request('POST','/releases',{'tag_name':tag,'target_commitish':os.environ['GITHUB_SHA'],
          'name':metadata['version'],'body':(ROOT/'release/CHANGELOG.md').read_text('utf-8'),'draft':True,'prerelease':False})
    assets=request('GET','/releases/'+str(release['id'])+'/assets')
    asset=next((item for item in assets if item['name']==apk.name),None)
    if asset is None:
        destination='https://uploads.github.com/repos/'+REPOSITORY+'/releases/'+str(release['id'])+'/assets?name='+urllib.parse.quote(apk.name,safe='')
        asset=request('POST',destination,binary=content)
    if asset.get('digest')!='sha256:'+metadata['sha256'] or asset['size']!=len(content):raise RuntimeError('Uploaded APK verification failed')
    if release.get('draft'):release=request('PATCH','/releases/'+str(release['id']),{'draft':False})
    print(json.dumps({'release':release['html_url'],'apk':asset['browser_download_url'],'sha256':metadata['sha256']}))

if __name__=='__main__':main()
