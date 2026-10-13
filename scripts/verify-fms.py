#!/usr/bin/env python3
"""Verify local FMS account initialization and actual MySQL-backed reports."""
import importlib.util,json,os,sys,time,urllib.parse
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('aicloud_http',ROOT/'scripts/smoke-test.py')
http=importlib.util.module_from_spec(spec);spec.loader.exec_module(http)
report={'passed':False,'checks':[]}
token=None

def check(label,condition):
    report['checks'].append({'check':label,'passed':bool(condition)})
    print(('PASS ' if condition else 'FAIL ')+label,flush=True)
    if not condition:raise AssertionError(label)

def request(label,path,method='GET',data=None):
    result=http.call('/admin-api/fms/'+path,method,data,token=token)
    check(label,result.get('code')==0)
    return result.get('data')

try:
    login=http.call('/admin-api/system/auth/login','POST',{'username':os.environ.get('AICLOUD_INIT_USERNAME','admin'),
                    'password':os.environ.get('AICLOUD_INIT_PASSWORD','Aicloud@123')})
    check('FMS administrator login',login.get('code')==0)
    token=login['data']['accessToken']
    suffix=str(time.time_ns());now=datetime.now(ZoneInfo('Asia/Shanghai'))
    month=now.strftime('%Y-%m')
    account=request('create actual verification account','config/account-set/create','POST',
            {'companyCode':'verify'+suffix,'companyName':'aicloud verify '+suffix})
    report['accountSetId']=account
    before=request('read uninitialized account','config/account-set/get?id='+str(account))
    check('new account begins uninitialized',before['initialized'] is False)
    settings={'accountSetId':account,'currencyCode':'RMB','startTime':int(now.replace(day=1,hour=0,minute=0,second=0,microsecond=0).timestamp()*1000),
              'standard':1,'level':4,'subjectCodeRule':'4-2-2-2','ledgerBalanceMode':1}
    request('initialize subjects, currency and closing templates','config/account-set/initialize','PUT',settings)
    saved=request('read initialized account','config/account-set/get?id='+str(account))
    check('initialization persists state and base currency',saved['initialized'] is True and saved['currencyId'])
    subjects=request('read actual account subjects','config/subject/list?accountSetId='+str(account))
    report['subjects']=len(subjects)
    codes={s['code'] for s in subjects}
    check('default hierarchy includes necessary finance accounts',len(subjects)>=113 and {'1001','1002','560210'}<=codes)
    templates=request('read closing templates','closing/template/list?accountSetId='+str(account))
    report['closingTemplates']=len(templates)
    check('all 21 default closing templates were initialized',len(templates)==21)
    withdrawal=next(t for t in templates if t['presetCode']=='daily-cash-withdrawal')
    check('cash withdrawal uses cash debit and bank credit',
          [(s['subjectCode'],s['direction']) for s in withdrawal['subjects']]==[('1001',1),('1002',2)])
    entertainment=next(t for t in templates if t['presetCode']=='daily-business-entertainment')
    check('business entertainment uses expense debit and cash credit',
          [(s['subjectCode'],s['direction']) for s in entertainment['subjects']]==[('560210',1),('1001',2)])
    query=urllib.parse.urlencode({'accountSetId':account,'startMonth':month,'endMonth':month})
    report['reportRows']={}
    for kind in ['balance-sheet','income-statement','cash-flow-statement']:
        rows=request('read '+kind,'report/'+kind+'/get?'+query)
        check(kind+' has persisted report definitions',isinstance(rows,list) and len(rows)>0)
        report['reportRows'][kind]=len(rows)
    adjustment=request('read cash flow adjustment definitions','report/cash-flow-statement/adjustment/list?'+query)
    check('cash flow includes all 21 adjustment definitions',len(adjustment)==21)
    repeated=http.call('/admin-api/fms/config/account-set/initialize','PUT',settings,token=token)
    check('repeat initialization rejects reset',repeated.get('code') not in [None,0])
    retained=request('read protected initialized account','config/account-set/get?id='+str(account))
    check('repeat initialization preserves account',retained['initialized'] and retained['currencyId']==saved['currencyId'])
    report['passed']=all(c['passed'] for c in report['checks'])
except Exception as error:
    report['error']=str(error)
    print('FAIL FMS: '+str(error),flush=True)
(ROOT/'docs/fms-business-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
sys.exit(0 if report['passed'] else 1)
