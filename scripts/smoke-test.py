#!/usr/bin/env python3
"""Real local HTTP verification; stores only outcomes, never credentials/tokens."""
import base64,hashlib,json,time,urllib.request,urllib.parse,urllib.error,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
BASE='http://127.0.0.1:18080'
results=[]

def call(path,method='GET',data=None,token=None,headers=None,base=BASE,form=False,basic=None):
    h={'tenant-id':'1',**(headers or {})}
    if token:h['Authorization']='Bearer '+token
    if basic:h['Authorization']='Basic '+base64.b64encode(basic.encode()).decode()
    if data is not None:
        data=(urllib.parse.urlencode(data).encode() if form else json.dumps(data).encode())
        h['Content-Type']='application/x-www-form-urlencoded' if form else 'application/json'
    req=urllib.request.Request(base+path,method=method,data=data,headers=h)
    try:r=urllib.request.urlopen(req,timeout=25)
    except urllib.error.HTTPError as e:r=e
    body=r.read().decode()
    try:return json.loads(body)
    except ValueError:return {'httpStatus':r.status,'html':body.startswith('<')}

def check(name,body,code=0):
    passed=body.get('code')==code
    results.append({'check':name,'passed':passed,'code':body.get('code')})
    print(('PASS' if passed else 'FAIL')+' '+name,flush=True)
    if not passed:raise AssertionError(f'{name}: {body.get("msg",body.get("status","unexpected response"))}')
    return body.get('data')

def authorize(client,redirect,token,challenge=None):
    data={'response_type':'code','client_id':client,'redirect_uri':redirect,'state':'local-smoke-state',
          'auto_approve':'false','scope':json.dumps({'user.read':True})}
    if challenge:data.update(code_challenge=challenge,code_challenge_method='S256')
    url=check(f'authorize {client}',call('/admin-api/system/oauth2/authorize','POST',data,token,form=True))
    return urllib.parse.parse_qs(urllib.parse.urlparse(url).query)['code'][0]

def main():
    for port in [48081,48082,48087,18080]:
        start=time.monotonic()
        while time.monotonic()-start<90:
            try:
                body=call('/actuator/health',base=f'http://127.0.0.1:{port}')
                if body.get('status')=='UP':break
            except Exception:pass
            time.sleep(2)
        else:raise AssertionError(f'Service on port {port} is not healthy')
        results.append({'check':f'health {port}','passed':True});print(f'PASS health {port}',flush=True)

    bad=call('/admin-api/system/auth/login','POST',{'username':'admin','password':'wrong-password'})
    assert bad.get('code')!=0;results.append({'check':'wrong password rejected','passed':True})
    admin=check('admin login',call('/admin-api/system/auth/login','POST',{'username':'admin','password':'Aicloud@123'}))
    token=admin['accessToken'];refresh=admin['refreshToken']
    permissions=check('dynamic role menus',call('/admin-api/system/auth/get-permission-info',token=token))
    assert permissions.get('menus') and permissions.get('permissions')
    check('anonymous admin denied',call('/admin-api/system/user/page?pageNo=1&pageSize=10'),401)
    check('forged identity denied',call('/admin-api/system/user/page?pageNo=1&pageSize=10',headers={'login-user':'{"id":1,"userType":2,"tenantId":1}'}),401)
    check('refresh bearer denied',call('/admin-api/system/user/page?pageNo=1&pageSize=10',token=refresh),401)
    check('cross tenant denied',call('/admin-api/system/user/page?pageNo=1&pageSize=10',token=token,headers={'tenant-id':'2'}),403)
    viewer=check('viewer login',call('/admin-api/system/auth/login','POST',{'username':'viewer','password':'Aicloud@123'}))
    check('viewer can query users',call('/admin-api/system/user/page?pageNo=1&pageSize=10',token=viewer['accessToken']))
    check('viewer cannot delete admin',call('/admin-api/system/user/delete?id=1','DELETE',token=viewer['accessToken']),403)
    check('administrator cannot use member identity',call('/app-api/member/user/get',token=token),403)
    for terminal in [0,10,11,20,31]:
        member=check(f'member terminal {terminal} login',call('/app-api/member/auth/login','POST',
                     {'mobile':'13800000000','password':'Aicloud@123'},headers={'terminal':str(terminal)}))
        check(f'member terminal {terminal} profile',call('/app-api/member/user/get',token=member['accessToken']))
        check(f'member terminal {terminal} cannot access admin',call('/admin-api/system/user/page?pageNo=1&pageSize=10',token=member['accessToken']),403)
    check('public RPC denied',call('/rpc-api/system/oauth2/check-access-token?accessToken=fake',base='http://127.0.0.1:48081'),401)

    for client,secret,redirect in [('sso-web','aicloud_dev_sso_secret','http://localhost:3001/callback'),
                                   ('sso-mobile','aicloud_dev_mobile_secret','http://localhost:3002/callback')]:
        code=authorize(client,redirect,token)
        data={'grant_type':'authorization_code','code':code,'redirect_uri':redirect,'state':'local-smoke-state'}
        check(f'SSO code redeem {client}',call('/admin-api/system/oauth2/token','POST',data,form=True,basic=client+':'+secret))
        replay=call('/admin-api/system/oauth2/token','POST',data,form=True,basic=client+':'+secret)
        assert replay.get('code')!=0;results.append({'check':f'SSO replay denied {client}','passed':True})
    invalid=call('/admin-api/system/oauth2/authorize','POST',{'response_type':'code','client_id':'sso-web',
                 'redirect_uri':'http://evil.invalid/cb','state':'state','auto_approve':'false','scope':'{}'},token,form=True)
    assert invalid.get('code')!=0;results.append({'check':'SSO invalid redirect denied','passed':True})
    verifier='dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk'
    challenge=base64.urlsafe_b64encode(hashlib.sha256(verifier.encode()).digest()).decode().rstrip('=')
    code=authorize('sso-public','http://localhost:3003/callback',token,challenge)
    pkce=check('public client S256 PKCE',call('/admin-api/system/oauth2/token','POST',{'client_id':'sso-public',
               'grant_type':'authorization_code','code':code,'redirect_uri':'http://localhost:3003/callback',
               'state':'local-smoke-state','code_verifier':verifier},form=True))
    code=authorize('sso-public','http://localhost:3003/callback',token,challenge)
    badpkce=call('/admin-api/system/oauth2/token','POST',{'client_id':'sso-public','grant_type':'authorization_code',
                'code':code,'redirect_uri':'http://localhost:3003/callback','state':'local-smoke-state','code_verifier':'x'*43},form=True)
    assert badpkce.get('code')!=0;results.append({'check':'invalid PKCE denied','passed':True})
    app=check('third party client credentials',call('/admin-api/system/oauth2/token','POST',
              {'grant_type':'client_credentials','scope':'dictionary.read'},form=True,basic='third-party:aicloud_dev_api_secret'))
    check('third party scoped API',call('/open-api/v1/system/dictionaries/system_user_sex',token=app['access_token']))
    check('third party cannot use admin',call('/admin-api/system/user/page?pageNo=1&pageSize=10',token=app['access_token']),403)
    check('admin token cannot impersonate application',call('/open-api/v1/system/dictionaries/system_user_sex',token=token),403)
    noscope=check('third party empty scope token',call('/admin-api/system/oauth2/token','POST',
                  {'grant_type':'client_credentials'},form=True,basic='third-party:aicloud_dev_api_secret'))
    check('application without scope denied',call('/open-api/v1/system/dictionaries/system_user_sex',token=noscope['access_token']),403)

    dept=check('persist department create',call('/admin-api/system/dept/create','POST',
               {'name':'aicloud smoke '+str(int(time.time())),'parentId':0,'sort':999,'status':0},token))
    check('persist department read',call('/admin-api/system/dept/get?id='+str(dept),token=token))
    check('persist department delete',call('/admin-api/system/dept/delete?id='+str(dept),'DELETE',token=token))
    docs=call('/admin-api/system/v3/api-docs',token=token)
    assert docs.get('openapi') and docs.get('paths');results.append({'check':'OpenAPI schema through gateway','passed':True,'paths':len(docs['paths'])})
    html=call('/doc.html');assert html.get('html');results.append({'check':'Knife4j UI','passed':True})
    check('global SSO logout',call('/admin-api/system/oauth2/logout','POST',token=token))
    check('SSO tokens revoked',call('/admin-api/system/user/page?pageNo=1&pageSize=10',token=pkce['access_token']),401)
    print(f'PASS {len(results)} real local HTTP checks',flush=True)

if __name__=='__main__':
    try:main()
    except Exception as error:
        results.append({'check':'verification completed','passed':False,'error':str(error)})
        print('FAIL '+str(error),flush=True)
        sys.exitcode=1
    else:sys.exitcode=0
    (ROOT/'docs/core-http-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n')
    sys.exit(sys.exitcode)
