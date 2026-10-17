#!/usr/bin/env python3
"""Check public business APIs against the records created by verify-integration.py.

Creates and removes only its own OAuth client; no existing client is changed.
"""
import importlib.util,json,secrets,sys,time
from pathlib import Path
from datetime import datetime
from zoneinfo import ZoneInfo
ROOT=Path(__file__).resolve().parents[1]
s=importlib.util.spec_from_file_location('http_local',ROOT/'scripts/smoke-test.py')
http=importlib.util.module_from_spec(s);s.loader.exec_module(http)
report={'checkedAt':datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),'passed':False,'checks':[]}
admin=None;client=None

def check(label,condition):
    report['checks'].append({'check':label,'passed':bool(condition)})
    print(('PASS ' if condition else 'FAIL ')+label,flush=True)
    if not condition:raise AssertionError(label)

def request(path,method='GET',data=None,token=None,expected=0,**kwargs):
    response=http.call(path,method,data,token=token,**kwargs)
    if response.get('code')!=expected:raise AssertionError(path.split('?')[0]+': '+str(response.get('code'))+' '+str(response.get('msg')))
    return response.get('data')

try:
    baseline=json.loads((ROOT/'docs/integration-http-verification.json').read_text())
    check('Business integration fixtures have passed real HTTP verification',baseline['passed'])
    product=baseline['records']['erpProduct'];warehouse=baseline['records']['erpWarehouse']
    report['productId']=product;report['warehouseId']=warehouse
    admin=request('/admin-api/system/auth/login','POST',{'username':'admin','password':'Aicloud@123'})['accessToken']
    client_id='verify-open-'+str(time.time_ns());secret=secrets.token_urlsafe(24)
    client=request('/admin-api/system/oauth2-client/create','POST',{'clientId':client_id,'secret':secret,'name':'本地开放接口验收','logo':'https://example.invalid/logo.png',
        'status':0,'accessTokenValiditySeconds':120,'refreshTokenValiditySeconds':120,'redirectUris':[],'authorizedGrantTypes':['client_credentials'],
        'scopes':['catalog.read','inventory.read'],'autoApproveScopes':[],'authorities':[],'resourceIds':[]},token=admin)
    def grant(scope):return request('/admin-api/system/oauth2/token','POST',{'grant_type':'client_credentials','scope':scope},form=True,basic=client_id+':'+secret)['access_token']
    full=grant('catalog.read inventory.read');catalog=grant('catalog.read')
    path=f'/open-api/v1/erp/products/{product}'
    item=request(path,token=full)
    check('Scoped application reads the actual ERP product with public fields only',item['id']==product and item['unit']==baseline['records']['unitName'] and set(item)=={'id','name','barCode','unit','salePrice'})
    page=request('/open-api/v1/erp/products?pageNo=1&pageSize=10',token=full)
    check('Public product catalog has actual persisted records',page['total']>0 and isinstance(page['list'],list))
    stock_path=f'/open-api/v1/wms/inventory?productId={product}&warehouseId={warehouse}'
    stock=request(stock_path,token=full)
    check('Public inventory comes from authoritative WMS availability',stock['productId']==product and stock['warehouseId']==warehouse and stock['available']==10)
    check('Public inventory excludes reservation sources and internal history',set(stock)=={'productId','warehouseId','unit','available'})
    for label,url,bearer,code,options in [
        ('Anonymous business API access is rejected',path,None,401,{}),
        ('Administrator token cannot impersonate an application',path,admin,403,{}),
        ('Catalog-only scope cannot read inventory',stock_path,catalog,403,{}),
        ('Application cannot escape its token tenant',stock_path,full,403,{'headers':{'tenant-id':'2'}}),
        ('Application cannot forge the tenant-visit permission',stock_path,full,403,{'headers':{'visit-tenant-id':'2'}}),
        ('Unknown product is not reported as zero inventory','/open-api/v1/wms/inventory?productId=9223372036854775800',full,404,{}),
        ('Unmapped warehouse is not reported as available stock',f'/open-api/v1/wms/inventory?productId={product}&warehouseId=9223372036854775800',full,404,{}),
        ('Public catalog forbids unbounded export pagination','/open-api/v1/erp/products?pageSize=-1',full,400,{}),
        ('Unapproved scope cannot access dictionaries','/open-api/v1/system/dictionaries/system_user_sex',full,403,{})]:
        request(url,token=bearer,expected=code,**options);check(label,True)
    for module in ['erp','wms']:
        docs=http.call('/admin-api/'+module+'/v3/api-docs',token=admin)
        check(module+' publishes its public business APIs in Knife4j',any(p.startswith('/open-api/v1/'+module+'/') for p in docs.get('paths',{})))
    report['passed']=all(c['passed'] for c in report['checks'])
except Exception as error:
    report['error']=str(error);print('FAIL '+str(error),flush=True)
finally:
    if client is not None:
        try:request('/admin-api/system/oauth2-client/delete?id='+str(client),'DELETE',token=admin)
        except Exception as error:report['passed']=False;report['cleanupError']=str(error)
    (ROOT/'docs/open-business-http-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
sys.exit(0 if report['passed'] else 1)
