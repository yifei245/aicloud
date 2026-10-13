#!/usr/bin/env python3
"""Verify the existing aicloud core Compose deployment without middleware changes."""
import importlib.util, json, subprocess, sys, time, urllib.error, urllib.parse, urllib.request
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('aicloud_http',ROOT/'scripts/smoke-test.py')
http=importlib.util.module_from_spec(spec);spec.loader.exec_module(http)
checks=[]

def check(label,condition):
    checks.append({'check':label,'passed':bool(condition)})
    print(('PASS ' if condition else 'FAIL ')+label,flush=True)
    if not condition:raise AssertionError(label)

def api(label,path,method='GET',data=None,token=None,expected=0,**kwargs):
    body=http.call(path,method,data,token=token,**kwargs)
    check(label,body.get('code')==expected)
    return body.get('data')

def run(args):
    return subprocess.check_output(args,cwd=ROOT,text=True).strip()

def main():
    context=json.loads(run(['docker','context','inspect']))[0]
    check('Docker uses local socket',context['Endpoints']['docker']['Host'].startswith(('unix://','npipe://')))
    for name in ['system-server','infra-server','member-server','gateway-server']:
        deadline=time.monotonic()+150
        while time.monotonic()<deadline:
            cid=run(['docker','compose','ps','-q',name])
            if cid:
                state=json.loads(run(['docker','inspect','--format','{{json .State}}',cid]))
                if state.get('Health',{}).get('Status')=='healthy':break
            time.sleep(2)
        else:raise AssertionError('Container unhealthy: '+name)
        check('Docker healthy '+name,True)
        user=run(['docker','inspect','--format','{{.Config.User}}',cid])
        check('Docker non-root application user '+name,user not in ('','root','0'))
        mounts=json.loads(run(['docker','inspect','--format','{{json .Mounts}}',cid]))
        check('Docker persistent application volume '+name,any(m['Destination']=='/app/data' and m['Type']=='volume' for m in mounts))
        if name!='gateway-server':
            ports=json.loads(run(['docker','inspect','--format','{{json .HostConfig.PortBindings}}',cid]))
            check('Docker private service ports '+name,not ports)
    # Wait for service registration and the gateway's discovery cache.
    deadline=time.monotonic()+30
    while time.monotonic()<deadline:
        body=http.call('/admin-api/system/auth/login','POST',{'username':'admin','password':'Aicloud@123'})
        if body.get('code')==0:break
        time.sleep(2)
    check('Docker gateway administrator login',body.get('code')==0)
    token=body['data']['accessToken']
    menus=api('Docker dynamic permission menus','/admin-api/system/auth/get-permission-info',token=token)
    check('Docker menu tree and button permissions',menus.get('menus') and menus.get('permissions'))
    api('Docker administrator database query','/admin-api/system/user/page?pageNo=1&pageSize=10',token=token)
    api('Docker anonymous administrator rejected','/admin-api/system/user/page?pageNo=1&pageSize=10',expected=401)
    viewer=api('Docker viewer login','/admin-api/system/auth/login','POST',{'username':'viewer','password':'Aicloud@123'})
    api('Docker viewer write rejected','/admin-api/system/user/delete?id=1','DELETE',token=viewer['accessToken'],expected=403)
    member=api('Docker member login','/app-api/member/auth/login','POST',{'mobile':'13800000000','password':'Aicloud@123'},headers={'terminal':'31'})
    api('Docker member database profile','/app-api/member/user/get',token=member['accessToken'])
    api('Docker member administrator boundary','/admin-api/system/user/page?pageNo=1&pageSize=10',token=member['accessToken'],expected=403)
    app=api('Docker third party credentials','/admin-api/system/oauth2/token','POST',
            {'grant_type':'client_credentials','scope':'dictionary.read'},form=True,basic='third-party:aicloud_dev_api_secret')
    api('Docker third party scoped dictionary API','/open-api/v1/system/dictionaries/system_user_sex',token=app['access_token'])
    docs=http.call('/admin-api/system/v3/api-docs',token=token)
    check('Docker aggregated OpenAPI',bool(docs.get('paths')))
    check('Docker Knife4j UI',http.call('/doc.html').get('html'))
    # Write through the real file API, then read the stored content through the gateway.
    boundary='aicloud-docker-'+str(time.time_ns())
    filename=boundary+'.txt'
    content=b'aicloud Docker persistent file verification'
    payload=(f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{filename}"\r\n'
             'Content-Type: text/plain\r\n\r\n').encode()+content+f'\r\n--{boundary}--\r\n'.encode()
    request=urllib.request.Request(http.BASE+'/admin-api/infra/file/upload',data=payload,method='POST',
              headers={'tenant-id':'1','Authorization':'Bearer '+token,'Content-Type':'multipart/form-data; boundary='+boundary})
    upload=json.loads(urllib.request.urlopen(request,timeout=25).read())
    check('Docker file upload and MySQL metadata',upload.get('code')==0)
    file=upload['data'];url=file['url'] if isinstance(file,dict) else file
    # The seeded storage domain is local; never follow a remote file URL.
    parsed=urllib.parse.urlparse(url)
    check('Docker stored file URL is local',parsed.hostname in ('localhost','127.0.0.1'))
    download_headers={'tenant-id':'1','Authorization':'Bearer '+token}
    response=urllib.request.urlopen(urllib.request.Request(http.BASE+parsed.path,headers=download_headers),timeout=25).read()
    if response!=content:
        try:details=json.loads(response)
        except ValueError:details={}
        raise AssertionError('File readback failed: '+str(details.get('code'))+' '+details.get('msg','Unexpected content'))
    check('Docker file content readback',response==content)
    if not isinstance(file,dict):
        metadata=api('Docker persisted file metadata lookup','/admin-api/infra/file/page?pageNo=1&pageSize=10&name='+filename,token=token)
        file=next(row for row in metadata['list'] if row['url']==url)
    run(['docker','compose','restart','infra-server'])
    deadline=time.monotonic()+90
    while time.monotonic()<deadline:
        try:
            if urllib.request.urlopen(urllib.request.Request(http.BASE+parsed.path,headers=download_headers),timeout=5).read()==content:break
        except Exception:pass
        time.sleep(2)
    else:raise AssertionError('Persistent file read after container restart')
    check('Docker file survives container restart',True)
    if isinstance(file,dict) and file.get('id'):
        api('Docker delete verification file','/admin-api/infra/file/delete?id='+str(file['id']),'DELETE',token=token)

exit_code=0
try:main()
except Exception as error:
    checks.append({'check':'verification completed','passed':False,'error':str(error)})
    print('FAIL '+str(error),flush=True);exit_code=1
finally:
    (ROOT/'docs/docker-verification.json').write_text(json.dumps({'passed':exit_code==0,'checks':checks},ensure_ascii=False,indent=2)+'\n')
raise SystemExit(exit_code)
