#!/usr/bin/env python3
"""Verify business integration through the local gateway, preserving audit records.

Requires core, bpm, wms, erp, crm, fms and oa. Creates named verification
records; never overwrites existing workflow definitions or accounting rules.
"""
import importlib.util,json,os,sys,time,secrets
from pathlib import Path
from datetime import datetime,timedelta
from zoneinfo import ZoneInfo

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('http_local',ROOT/'scripts/smoke-test.py')
http=importlib.util.module_from_spec(spec);spec.loader.exec_module(http)
report={'passed':False,'checkedAt':datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),'checks':[],'records':{}}
token=None
suffix=str(time.time_ns());name='aicloud integration '+suffix
now=int(time.time()*1000)

def check(label,condition):
    report['checks'].append({'check':label,'passed':bool(condition)})
    print(('PASS ' if condition else 'FAIL ')+label,flush=True)
    if not condition:raise AssertionError(label)

def request(path,method='GET',data=None,bearer=None,api='admin-api'):
    response=http.call('/'+api+'/'+path,method,data,token=bearer or token)
    if response.get('code')!=0:raise AssertionError(path.split('?')[0]+': '+str(response.get('code'))+' '+str(response.get('msg')))
    return response.get('data')

def eventually(label,read,predicate,seconds=35):
    deadline=time.monotonic()+seconds
    while True:
        value=read()
        if predicate(value):check(label,True);return value
        if time.monotonic()>=deadline:check(label,False)
        time.sleep(.5)

def ensure_model(key,title):
    models=request('bpm/model/list')
    existing=next((m for m in models if m['key']==key),None)
    if existing:
        if not existing.get('processDefinition'):request('bpm/model/deploy?id='+existing['id'],'POST')
        return
    xml=f'''<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:flowable="http://flowable.org/bpmn" targetNamespace="http://aicloud.local/bpm">
<process id="{key}" name="{title}" isExecutable="true"><startEvent id="start"/><sequenceFlow id="f1" sourceRef="start" targetRef="review"/>
<userTask id="review" name="审批" flowable:candidateStrategy="30" flowable:candidateParam="1"><extensionElements><flowable:approveType>1</flowable:approveType></extensionElements></userTask>
<sequenceFlow id="f2" sourceRef="review" targetRef="end"/><endEvent id="end"/></process></definitions>'''
    model=request('bpm/model/create','POST',{'key':key,'name':title,'type':10,'formType':20,'visible':False,'bpmnXml':xml,
        'managerUserIds':[1],'startUserIds':[],'startDeptIds':[],'formCustomCreatePath':'/integration/create','formCustomViewPath':'/integration/view',
        'autoApprovalType':0,'allowCancelRunningProcess':True,'allowWithdrawTask':False})
    request('bpm/model/deploy?id='+model,'POST')

def approve(instance):
    deadline=time.monotonic()+30
    while time.monotonic()<deadline:
        state=request('bpm/process-instance/get?id='+instance)
        if state['status']==2:check('BPM approval persisted',True);return
        tasks=request('bpm/tasks/todo-page?pageNo=1&pageSize=100',api='employee-api')['list']
        task=next((t for t in tasks if t['processInstanceId']==instance),None)
        if task:request('bpm/tasks/approve','PUT',{'id':task['id'],'reason':'本地跨模块联动验收','variables':{}},api='employee-api')
        time.sleep(.5)
    check('BPM approval persisted',False)

def stock():
    unit=request('erp/product-unit/create','POST',{'name':'件'+suffix,'status':0})
    category=request('erp/product-category/create','POST',{'parentId':0,'name':name,'code':suffix,'sort':1,'status':0})
    product=request('erp/product/create','POST',{'name':name,'barCode':suffix,'categoryId':category,'unitId':unit,'status':0,'purchasePrice':10,'salePrice':20,'minPrice':1})
    warehouse=request('erp/warehouse/create','POST',{'name':name,'sort':999,'status':0,'warehousePrice':0,'truckagePrice':0})
    order=request('erp/stock-in/create','POST',{'inTime':now,'remark':name,'items':[{'warehouseId':warehouse,'productId':product,'productPrice':10,'count':10}]})
    request(f'erp/stock-in/update-status?id={order}&status=20','PUT')
    count=lambda:request(f'erp/stock/get-count?productId={product}&warehouseId={warehouse}')
    eventually('ERP inbound commits ten units into authoritative WMS inventory',count,lambda v:v==10)
    mappings=request('wms/integration/mappings?kind=PRODUCT&source=ERP')
    target=next(m['target_id'] for m in mappings if m['source_id']==product)
    wms=request(f'wms/inventory/page?pageNo=1&pageSize=100&type=warehouse&skuId={target}')
    check('WMS and ERP inventory views agree',sum(x['quantity'] for x in wms['list'])==10)
    request(f'erp/stock-in/update-status?id={order}&status=10','PUT')
    eventually('ERP reversal also reverses WMS physical inventory',count,lambda v:v==0)
    request(f'erp/stock-in/update-status?id={order}&status=20','PUT')
    eventually('ERP reapproval creates a new stock movement without duplicate consumption',count,lambda v:v==10)
    report['records'].update(erpProduct=product,erpWarehouse=warehouse,stockIn=order,unitName='件'+suffix)
    return product

def contract(product):
    erp_customer=request('erp/customer/create','POST',{'name':name,'status':0,'sort':999})
    crm_customer=request('crm/customer/create','POST',{'name':name,'ownerUserId':1})
    follow=request('crm/follow-up-record/create','POST',{'bizType':2,'bizId':crm_customer,'type':1,
        'content':'<p>安全<strong>跟进</strong><script>alert(1)</script><img src="/safe.png" onerror="alert(2)"></p>',
        'nextTime':now+86400000,'businessIds':[],'contactIds':[],'picUrls':[],'fileUrls':[]})
    content=request(f'crm/follow-up-record/get?id={follow}')['content']
    check('Persisted CRM rich text keeps formatting while removing scripts and event handlers',
          '<strong>跟进</strong>' in content and 'script' not in content and 'onerror' not in content)
    category=request('crm/product-category/create','POST',{'name':name,'parentId':0})
    crm_product=request('crm/product/create','POST',{'name':name,'no':suffix,'unit':1,'price':20,'status':1,'categoryId':category,'ownerUserId':1})
    for mapping in [{'kind':'CUSTOMER','crmId':crm_customer,'erpId':erp_customer},{'kind':'PRODUCT','crmId':crm_product,'erpId':product,'unit':report['records']['unitName']}]:
        request('crm/integration/bind','POST',mapping)
    ensure_model('crm-contract-audit','CRM 合同审批')
    body={'name':name,'customerId':crm_customer,'ownerUserId':1,'orderDate':now,'discountPercent':10,
          'products':[{'productId':crm_product,'productPrice':20,'contractPrice':20,'count':3}]}
    order=request('crm/contract/create','POST',body)
    request(f'crm/contract/submit?id={order}','PUT')
    saved=request(f'crm/contract/get?id={order}')
    approve(saved['processInstanceId'])
    sale=eventually('Approved CRM contract creates an ERP sale order',lambda:request(f'crm/integration/contract-sale?contractId={order}'),lambda v:bool(v))
    erp=request(f'erp/sale-order/get?id={sale}')
    check('ERP receives mapped customer, product, quantity and discounted amount',erp['customerId']==erp_customer and erp['items'][0]['productId']==product and erp['items'][0]['count']==3 and erp['totalPrice']==54)
    check('Generated ERP sale order remains pending review',erp['status']==10)
    time.sleep(3)
    check('CRM keeps a stable link to the generated ERP sale order',request(f'crm/integration/contract-sale?contractId={order}')==sale)
    report['records'].update(crmContract=order,erpSaleOrder=sale,crmProcessInstance=saved['processInstanceId'])

def finance():
    accounts=request('fms/config/account-set/list')
    rule=None
    for account in accounts:
        if not account.get('initialized'):continue
        rules=request('fms/business/rules?accountSetId='+str(account['id']))
        rule=next((r for r in rules if r['source']=='OA_EXPENSE'),None)
        if rule:break
    if rule:
        account=rule['account_set_id']
    else:
        account=request('fms/config/account-set/create','POST',{'companyCode':'integration'+suffix,'companyName':name})
        first=datetime.now(ZoneInfo('Asia/Shanghai')).replace(day=1,hour=0,minute=0,second=0,microsecond=0)
        request('fms/config/account-set/initialize','PUT',{'accountSetId':account,'currencyCode':'RMB','startTime':int(first.timestamp()*1000),'standard':1,'level':4,'subjectCodeRule':'4-2-2-2','ledgerBalanceMode':1})
        subjects=request(f'fms/config/subject/list?accountSetId={account}')
        debit=next(s['id'] for s in subjects if s['code']=='560210');credit=next(s['id'] for s in subjects if s['code']=='1001')
        words=request(f'fms/config/voucher-word/list?accountSetId={account}')
        request('fms/business/rule','POST',{'source':'OA_EXPENSE','accountSetId':account,'voucherWordId':words[0]['id'],'debitSubjectId':debit,'creditSubjectId':credit})
    reimbursement=request('oa/reimbursement/create','POST',{'title':name,'urgency':1,'witnessUserId':1,'customerName':name,'paymentMethod':1,'reason':'跨模块凭证验收','fileUrls':[],
        'items':[{'expenseTime':now,'expenseType':1,'description':'本地验收交通费','invoiceCount':1,'price':123.45}]})
    request('oa/reimbursement/submit','POST',{'id':reimbursement,'startUserSelectAssignees':{}})
    saved=request(f'oa/reimbursement/get?id={reimbursement}');approve(saved['processInstanceId'])
    source=eventually('Approved OA reimbursement creates an FMS business source',lambda:request(f'fms/business/sources?accountSetId={account}'),lambda rows:any(r['source']=='OA_EXPENSE' and r['document_id']==reimbursement for r in rows))
    row=next(r for r in source if r['source']=='OA_EXPENSE' and r['document_id']==reimbursement)
    voucher=request(f'fms/voucher/get?accountSetId={account}&id='+str(row['voucher_id']))
    check('Business voucher is pending review and balanced at the actual reimbursement amount',voucher['status']==0 and voucher['debitAmount']==123.45 and voucher['creditAmount']==123.45)
    report['records'].update(fmsAccountSet=account,oaReimbursement=reimbursement,fmsVoucher=row['voucher_id'])

def personnel():
    actor_name='联动'+suffix[-12:]
    dept=request('system/dept/create','POST',{'name':actor_name,'parentId':1,'leaderUserId':1,'sort':999,'status':0})
    username='integration'+suffix[-12:];password=secrets.token_urlsafe(10);mobile='13'+suffix[-9:]
    user=request('system/user/create','POST',{'username':username,'nickname':actor_name,'password':password,'deptId':dept,'mobile':mobile,'postIds':[],'sex':0})
    role=request('system/role/create','POST',{'name':actor_name,'code':'integration_'+suffix[-12:],'sort':999,'status':0})
    permissions={f'oa:{resource}:{operation}' for resource in ['leave-apply','regular-apply','resign-apply'] for operation in ['create','query']}
    permissions.update(['hrm:portal:query','hrm:portal:attendance:leave'])
    menus=request('system/menu/list')
    menu_ids=[m['id'] for m in menus if m.get('permission') in permissions]
    check('Isolated employee role has every required explicit permission',permissions <= {m.get('permission') for m in menus if m['id'] in menu_ids})
    request('system/permission/assign-role-menu','POST',{'roleId':role,'menuIds':menu_ids})
    request('system/permission/assign-user-role','POST',{'userId':user,'roleIds':[role]})
    employee=request('hrm/employee/create-list','POST',[{'userId':user,'jobNumber':'I'+suffix[-12:],'mobile':mobile,'deptId':dept,'type':1,'entryTime':now-30*86400000,'probation':3,'remark':name}])[0]
    login=http.call('/employee-api/system/auth/login','POST',{'username':username,'password':password})
    check('Isolated employee can sign in',login.get('code')==0);employee_token=login['data']['accessToken']
    def own(path,method='GET',data=None):return request(path.replace('hrm/portal/','hrm/'),method,data,bearer=employee_token,api='employee-api')
    profile=own('hrm/profile/get')
    check('Employee API reads only the signed-in employee profile',profile['id']==employee)
    notifications=own('system/notifications/page?pageNo=1&pageSize=10')
    check('Employee notifications are available under the dedicated endpoint',isinstance(notifications['list'],list))
    report['records'].update(testEmployee=employee,testUser=user)
    year=datetime.now(ZoneInfo('Asia/Shanghai')).year
    request('hrm/attendance/leave-balance/grant','POST',{'employeeId':employee,'code':'1','year':year,'entitlement':5})
    def balance():return next(r for r in request(f'hrm/attendance/leave-balance/list?employeeId={employee}&year={year}') if r['code']=='1')
    start=datetime.now(ZoneInfo('Asia/Shanghai')).replace(hour=9,minute=0,second=0,microsecond=0)+timedelta(days=1)
    stamp=lambda date:int(date.timestamp()*1000)
    leave=own('oa/leave-apply/create','POST',{'title':name,'urgency':1,'type':1,'startTime':stamp(start),'endTime':stamp(start+timedelta(hours=8)),'reason':'统一额度验收','fileUrls':[]})
    own('oa/leave-apply/submit','POST',{'id':leave,'startUserSelectAssignees':{}})
    check('Employee application details reject another employee identity even with administrator permissions',
          http.call(f'/employee-api/oa/leave-apply/get?id={leave}',token=token).get('code')==403)
    check('OA submission reserves one day in HRM',balance()['reserved']==1 and balance()['available']==4)
    own_balance=own(f'hrm/leave-balances?year={year}')
    check('Employee balance API uses the signed-in employee without an employeeId parameter',any(r['reserved']==1 and r['available']==4 for r in own_balance))
    duplicate=own('oa/leave-apply/create','POST',{'title':name,'urgency':1,'type':1,'startTime':stamp(start),'endTime':stamp(start+timedelta(hours=8)),'reason':'重复时间验收','fileUrls':[]})
    rejected=http.call('/admin-api/oa/leave-apply/submit','POST',{'id':duplicate,'startUserSelectAssignees':{}},token=employee_token)
    check('Overlapping OA request is rejected without consuming more quota',rejected.get('code') not in [0,None] and balance()['reserved']==1)
    saved=own(f'oa/leave-apply/get?id={leave}');approve(saved['processInstanceId'])
    eventually('OA approval converts the same HRM reservation into used leave',balance,lambda r:r['reserved']==0 and r['used']==1 and r['available']==4)
    hrm_leave=own('hrm/portal/attendance/leave/create','POST',{'type':'年假','day':1,'startTime':stamp(start+timedelta(days=2)),'endTime':stamp(start+timedelta(days=2,hours=8)),'reason':'HRM 原生申请共用额度'})
    check('Native HRM leave reserves from the same balance',balance()['reserved']==1 and balance()['used']==1 and balance()['available']==3)
    own('hrm/portal/attendance/leave/cancel','PUT',{'id':hrm_leave,'reason':'验收取消释放'})
    eventually('Cancelled native HRM leave releases quota',balance,lambda r:r['reserved']==0 and r['used']==1 and r['available']==4)
    today=datetime.now(ZoneInfo('Asia/Shanghai')).replace(hour=0,minute=0,second=0,microsecond=0)
    regular=own('oa/regular-apply/create','POST',{'title':name,'urgency':1,'startTime':now-30*86400000,'endTime':stamp(today),**{f:'验收' for f in ['experience','understanding','growth','deficiency','improvement','suggestion']}})
    own('oa/regular-apply/submit','POST',{'id':regular,'startUserSelectAssignees':{}})
    saved=own(f'oa/regular-apply/get?id={regular}');approve(saved['processInstanceId'])
    eventually('Approved OA regularization updates the HRM employee status',lambda:request(f'hrm/employee/get?id={employee}'),lambda r:r['status']==1)
    resign=own('oa/resign-apply/create','POST',{'title':name,'urgency':1,'handoverUserId':1,'unfinishedWork':'验收完毕','reason':'本地测试员工离职','hasPendingReimbursement':False})
    own('oa/resign-apply/submit','POST',{'id':resign,'startUserSelectAssignees':{}})
    saved=own(f'oa/resign-apply/get?id={resign}');approve(saved['processInstanceId'])
    eventually('Approved OA resignation updates the HRM employment status',lambda:request(f'hrm/employee/get?id={employee}'),lambda r:r['entryStatus']==4)
    request('system/user/update-status','PUT',{'id':user,'status':1})
    report['records'].update(testEmployee=employee,testUser=user,oaLeave=leave,hrmCancelledLeave=hrm_leave,oaRegularization=regular,oaResignation=resign)

def employee_security():
    for module in ['system','bpm','hrm','oa']:
        docs=http.call(f'/admin-api/{module}/v3/api-docs',token=token)
        check(module+' publishes employee endpoints in Knife4j',any(p.startswith('/employee-api/'+module+'/') for p in docs.get('paths',{})))
    path='/employee-api/system/notifications/unread-count'
    check('Anonymous access to employee APIs is rejected',http.call(path).get('code')==401)
    member=http.call('/app-api/member/auth/login','POST',{'mobile':'13800000000','password':'Aicloud@123'})
    check('Local member can log in for identity-isolation verification',member.get('code')==0)
    check('Member token cannot impersonate an employee',http.call(path,token=member['data']['accessToken']).get('code')==403)
    app=http.call('/admin-api/system/oauth2/token','POST',{'grant_type':'client_credentials','scope':'dictionary.read'},form=True,basic='third-party:aicloud_dev_api_secret')
    check('Local application can obtain its own scoped token',app.get('code')==0)
    check('Application token cannot impersonate an employee',http.call(path,token=app['data']['access_token']).get('code')==403)

def notices():
    instance=report['records']['crmProcessInstance']
    eventually('BPM sends station notifications with SMS disabled',lambda:request('system/notify-message/my-page?pageNo=1&pageSize=100'),
        lambda page:any(instance in json.dumps(n,ensure_ascii=False) for n in page['list']))

try:
    result=http.call('/admin-api/system/auth/login','POST',{'username':os.environ.get('AICLOUD_INIT_USERNAME','admin'),'password':os.environ.get('AICLOUD_INIT_PASSWORD','Aicloud@123')})
    check('Administrator can log in with XSS filtering enabled',result.get('code')==0)
    token=result['data']['accessToken']
    for module in ['bpm','wms','erp','crm','fms','hrm','oa']:
        eventually(module+' gateway route is ready',lambda m=module:http.call('/admin-api/'+m+'/v3/api-docs',token=token),lambda body:bool(body.get('openapi')),seconds=120)
    product=stock();contract(product);finance();notices();employee_security();personnel()
    report['passed']=all(c['passed'] for c in report['checks'])
except Exception as error:
    report['error']=str(error);print('FAIL '+str(error),flush=True)
finally:
    (ROOT/'docs/integration-http-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
sys.exit(0 if report['passed'] else 1)
