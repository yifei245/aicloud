#!/usr/bin/env python3
"""Exercise real local business writes, transitions and persistent reads.

Creates uniquely named verification records. Deletes only records created by
this run where the business API supports deletion; retains completed audit
history and zero-balance stock history. Never changes shared middleware.
"""
import argparse, importlib.util, json, subprocess, sys, time, urllib.parse
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('aicloud_http',ROOT/'scripts/smoke-test.py')
http=importlib.util.module_from_spec(spec);spec.loader.exec_module(http)
parser=argparse.ArgumentParser()
parser.add_argument('profiles',nargs='*')
args=parser.parse_args()
selected=args.profiles or ['erp','crm','bpm','report','iot']
if set(selected)-{'erp','crm','bpm','report','iot'}:parser.error('Unknown flow profile')
apps=json.loads((ROOT/'docs/services.json').read_text())
path=ROOT/'docs/business-flow-verification.json'
report=json.loads(path.read_text()) if path.exists() else {}
token=None;steps=[];suffix=str(time.time_ns());prefix='aicloud verify '+suffix

def request(label,path,method='GET',data=None,expected=0):
    body=http.call('/admin-api/'+path,method,data,token=token)
    passed=body.get('code')==expected
    steps.append({'check':label,'passed':passed,'code':body.get('code')})
    print(('PASS ' if passed else 'FAIL ')+label,flush=True)
    if not passed:raise AssertionError(label+': '+body.get('msg','Unexpected response'))
    return body.get('data')

def assert_value(label,condition):
    steps.append({'check':label,'passed':bool(condition)})
    print(('PASS ' if condition else 'FAIL ')+label,flush=True)
    if not condition:raise AssertionError(label)

def erp():
    unit=request('ERP create unit','erp/product-unit/create','POST',{'name':prefix,'status':0})
    category=request('ERP create category','erp/product-category/create','POST',
                     {'parentId':0,'name':prefix,'code':'verify'+suffix,'sort':1,'status':0})
    product=request('ERP create product','erp/product/create','POST',
                    {'name':prefix,'barCode':suffix,'categoryId':category,'unitId':unit,'status':0,
                     'purchasePrice':10,'salePrice':20,'minPrice':1})
    warehouse=request('ERP create warehouse','erp/warehouse/create','POST',
                      {'name':prefix,'sort':999,'status':0,'warehousePrice':0,'truckagePrice':0})
    stock=request('ERP create stock-in','erp/stock-in/create','POST',
                  {'inTime':int(time.time()*1000),'remark':prefix,
                   'items':[{'warehouseId':warehouse,'productId':product,'productPrice':10,'count':5}]})
    request('ERP approve stock-in',f'erp/stock-in/update-status?id={stock}&status=20','PUT')
    count=request('ERP read inventory',f'erp/stock/get-count?productId={product}&warehouseId={warehouse}')
    assert_value('ERP approval adds five inventory units',count==5)
    order=request('ERP read stock-in',f'erp/stock-in/get?id={stock}')
    assert_value('ERP persists approval and item amount',order['status']==20 and order['items'][0]['count']==5)
    request('ERP unapprove stock-in',f'erp/stock-in/update-status?id={stock}&status=10','PUT')
    count=request('ERP read reversed inventory',f'erp/stock/get-count?productId={product}&warehouseId={warehouse}')
    assert_value('ERP reversal returns inventory to zero',count==0)
    request('ERP delete verification stock-in',f'erp/stock-in/delete?ids={stock}','DELETE')
    for resource,id in [('product',product),('warehouse',warehouse),('product-category',category),('product-unit',unit)]:
        request('ERP delete verification '+resource,f'erp/{resource}/delete?id={id}','DELETE')

def crm():
    customer=request('CRM create customer','crm/customer/create','POST',
                     {'name':prefix,'ownerUserId':1,'remark':'Local integration verification'})
    saved=request('CRM read customer',f'crm/customer/get?id={customer}')
    assert_value('CRM persists customer ownership',saved['name']==prefix and saved['ownerUserId']==1)
    follow=request('CRM create follow-up','crm/follow-up-record/create','POST',
                   {'bizType':2,'bizId':customer,'type':1,'content':prefix,
                    'nextTime':int((time.time()+86400)*1000),'businessIds':[],'contactIds':[],'picUrls':[],'fileUrls':[]})
    saved=request('CRM read follow-up',f'crm/follow-up-record/get?id={follow}')
    assert_value('CRM persists related follow-up',saved['bizId']==customer and saved['content']==prefix)
    request('CRM delete verification follow-up',f'crm/follow-up-record/delete?id={follow}','DELETE')
    request('CRM delete verification customer',f'crm/customer/delete?id={customer}','DELETE')

def bpm():
    key='aicloud_verify_'+suffix
    xml=f'''<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
 xmlns:flowable="http://flowable.org/bpmn" targetNamespace="http://aicloud.local/bpm">
 <process id="{key}" name="{prefix}" isExecutable="true">
  <startEvent id="start"/><sequenceFlow id="flow1" sourceRef="start" targetRef="review"/>
  <userTask id="review" name="Approve" flowable:candidateStrategy="30" flowable:candidateParam="1">
   <extensionElements><flowable:approveType>1</flowable:approveType></extensionElements>
  </userTask><sequenceFlow id="flow2" sourceRef="review" targetRef="end"/><endEvent id="end"/>
 </process></definitions>'''
    model=request('BPM create model','bpm/model/create','POST',
                  {'key':key,'name':prefix,'type':10,'formType':20,'visible':True,'bpmnXml':xml,
                   'managerUserIds':[1],'startUserIds':[1],'startDeptIds':[],
                   'formCustomCreatePath':'/bpm/verify/create','formCustomViewPath':'/bpm/verify/view',
                   'autoApprovalType':0,'allowCancelRunningProcess':True,'allowWithdrawTask':False})
    request('BPM deploy model',f'bpm/model/deploy?id={model}','POST')
    models=request('BPM read deployed models','bpm/model/list')
    definition=next(m['processDefinition']['id'] for m in models if m['id']==model)
    instance=request('BPM start instance','bpm/process-instance/create','POST',
                     {'processDefinitionId':definition,'variables':{'verification':suffix},'startUserSelectAssignees':{}})
    tasks=request('BPM read pending approval','bpm/task/todo-page?pageNo=1&pageSize=100')
    task=next(t for t in tasks['list'] if t['processInstanceId']==instance)
    request('BPM approve task','bpm/task/approve','PUT',{'id':task['id'],'reason':'Local verification passed','variables':{}})
    saved=request('BPM read completed instance',f'bpm/process-instance/get?id={instance}')
    assert_value('BPM completes and persists approval',saved['status']==2)
    request('BPM delete verification model',f'bpm/model/delete?id={model}','DELETE')

def report_flow():
    project=request('Report create project','report/go-view/project/create','POST',{'name':prefix})
    content=json.dumps({'components':[{'type':'table','dataSource':'mysql'}]})
    request('Report save dashboard','report/go-view/project/update','PUT',
            {'id':project,'status':0,'content':content,'remark':'Local verification'})
    saved=request('Report read project',f'report/go-view/project/get?id={project}')
    assert_value('Report persists dashboard content',saved['content']==content)
    data=request('Report query real tenant data','report/go-view/data/get-by-sql','POST',
                 {'sql':'SELECT username, tenant_id FROM system_users WHERE deleted=0 ORDER BY id'})
    assert_value('Report SQL restricts tenant rows',data['source'] and all(r['tenant_id']==1 for r in data['source']))
    for label,sql in [('reject write','DELETE FROM system_users'),('reject multi-statement','SELECT 1; DELETE FROM system_users'),
                      ('reject foreign database','SELECT * FROM mysql.user')]:
        request('Report '+label,'report/go-view/data/get-by-sql','POST',{'sql':sql},expected=400)
    stats=request('Report query actual API statistics','report/go-view/data/get-by-http','POST')
    assert_value('Report returns actual request counts',stats['source'] and all(r['requests']>0 for r in stats['source']))
    request('Report delete verification project',f'report/go-view/project/delete?id={project}','DELETE')

def iot():
    category=request('IoT create category','iot/product-category/create','POST',{'name':prefix,'sort':999,'status':0})
    product=request('IoT create product','iot/product/create','POST',
                    {'name':prefix,'categoryId':category,'deviceType':0,'netType':0,'protocolType':'mqtt',
                     'serializeType':'json','registerEnabled':False})
    saved=request('IoT read product',f'iot/product/get?id={product}')
    model=request('IoT create property definition','iot/thing-model/create','POST',
                  {'productId':product,'productKey':saved['productKey'],'identifier':'temperature','name':'Temperature','type':1,
                   'property':{'identifier':'temperature','name':'Temperature','accessMode':'r','dataType':'double',
                               'required':False,'dataSpecs':{'dataType':'double','min':'-50','max':'150','step':'0.1'}}})
    device=request('IoT create device','iot/device/create','POST',{'productId':product,'deviceName':'verify'+suffix,'nickname':prefix})
    request('IoT simulate property report','iot/device/message/send','POST',
            {'deviceId':device,'method':'thing.property.post','params':{'temperature':26.5}})
    deadline=time.monotonic()+15;history=[]
    while time.monotonic()<deadline:
        body=http.call(f'/admin-api/iot/device/property/history-list?deviceId={device}&identifier=temperature',token=token)
        if body.get('code')!=0:raise AssertionError('IoT property history: '+body.get('msg','Unexpected response'))
        history=body['data']
        if history:break
        time.sleep(0.5)
    assert_value('IoT persists numeric property in MySQL',history and history[0]['value']==26.5)
    logs=request('IoT read stored device messages',f'iot/device/message/page?deviceId={device}&pageNo=1&pageSize=10')
    assert_value('IoT persists property message log',any(m['method']=='thing.property.post' for m in logs['list']))
    for resource,id in [('device',device),('thing-model',model),('product',product),('product-category',category)]:
        request('IoT delete verification '+resource,f'iot/{resource}/delete?id={id}','DELETE')

failed=False
for profile in selected:
    steps=[];outcome={'checks':steps,'passed':False}
    app=next(a for a in apps if a['profile']==profile)
    pidfile=ROOT/'data/local'/f'{profile}.pid'
    existed=False
    if pidfile.exists():
        state=subprocess.run(['ps','-p',pidfile.read_text().strip(),'-o','stat=,command='],capture_output=True,text=True)
        existed=state.returncode==0 and not state.stdout.lstrip().startswith('Z') and str(ROOT/app['directory']) in state.stdout
    subprocess.check_call([sys.executable,str(ROOT/'scripts/run-local.py'),'start',profile])
    try:
        deadline=time.monotonic()+90
        while time.monotonic()<deadline:
            try:
                if http.call('/actuator/health',base=f'http://127.0.0.1:{app["port"]}').get('status')=='UP':break
            except Exception:pass
            time.sleep(2)
        else:raise AssertionError('Service health check failed')
        login=http.call('/admin-api/system/auth/login','POST',{'username':'admin','password':'Aicloud@123'})
        if login.get('code')!=0:raise AssertionError('Administrator login failed')
        token=login['data']['accessToken']
        # Discovery may lag behind the actuator endpoint becoming available.
        deadline=time.monotonic()+20
        while time.monotonic()<deadline:
            docs=http.call(f'/admin-api/{profile}/v3/api-docs',token=token)
            if docs.get('openapi'):break
            time.sleep(1)
        else:raise AssertionError('Gateway route unavailable')
        {'erp':erp,'crm':crm,'bpm':bpm,'report':report_flow,'iot':iot}[profile]()
        outcome['passed']=all(s['passed'] for s in steps)
    except Exception as error:
        outcome['error']=str(error);failed=True;print('FAIL '+profile+': '+str(error),flush=True)
    finally:
        if not existed:
            subprocess.check_call([sys.executable,str(ROOT/'scripts/run-local.py'),'stop',profile])
        report[profile]=outcome;path.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
raise SystemExit(1 if failed else 0)
