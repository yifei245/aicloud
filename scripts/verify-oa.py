#!/usr/bin/env python3
"""Exercise OA through the local gateway, including all 14 approval workflows."""
import argparse, importlib.util, json, os, sys, time, urllib.parse, urllib.request
from datetime import datetime, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('aicloud_http', ROOT / 'scripts/smoke-test.py')
http = importlib.util.module_from_spec(spec); spec.loader.exec_module(http)
parser = argparse.ArgumentParser()
parser.add_argument('groups', nargs='*')
args = parser.parse_args()
results = {}; checks = []; token = None; docs = {}; actor = None
suffix = str(time.time_ns()); name = 'aicloud OA verify ' + suffix
start = int((datetime.now() + timedelta(days=1)).replace(hour=9, minute=0, second=0, microsecond=0).timestamp()*1000)
end = start + 8*3600000

def request(label, path, method='GET', data=None, expected=0, bearer=None):
    result = http.call('/admin-api/' + path, method, data, token=bearer or token)
    passed = result.get('code') == expected
    checks.append({'check':label, 'passed':passed, 'code':result.get('code')})
    print(('PASS ' if passed else 'FAIL ') + label, flush=True)
    if not passed: raise AssertionError(label + ': ' + result.get('msg', '接口不可用'))
    return result.get('data')

def assert_value(label, condition):
    checks.append({'check':label, 'passed':bool(condition)})
    print(('PASS ' if condition else 'FAIL ') + label, flush=True)
    if not condition: raise AssertionError(label)

def op(resource, action, id=None, data=None, label=None, bearer=None):
    path = 'oa/' + resource + '/' + action
    operations = docs['paths']['/admin-api/' + path]
    method = next(key for key in operations if key in ['get','post','put','delete'])
    if 'requestBody' in operations[method]:
        body = dict(data or {})
        if id is not None: body['id'] = id
    else:
        body = None
        params = dict(data or {})
        if id is not None: params['id'] = id
        if params: path += '?' + urllib.parse.urlencode(params, doseq=True)
    return request(label or resource + ' ' + action, path, method.upper(), body, bearer=bearer)

def local_actor():
    global actor
    if actor: return actor
    actor_name='OA验收'+suffix[-14:]
    dept=request('create isolated OA test department','system/dept/create','POST',{'name':actor_name,'parentId':1,'leaderUserId':1,'sort':999,'status':0})
    user=request('create isolated OA test user','system/user/create','POST',{'username':'oa'+suffix[-14:],'nickname':actor_name,
                  'password':'Aicloud@123','deptId':dept,'postIds':[],'sex':0})
    role=request('create isolated OA test role','system/role/create','POST',{'name':actor_name,'code':'oa_verify_'+suffix[-14:],'sort':999,'status':0})
    menus=request('read actual OA permission buttons','system/menu/list')
    permissions={'oa:announcement:query','oa:file:query','oa:file:update','oa:file:create','oa:file:delete','oa:note:query','oa:task:query'}
    request('grant explicit test role permissions','system/permission/assign-role-menu','POST',
            {'roleId':role,'menuIds':[menu['id'] for menu in menus if menu.get('permission') in permissions]})
    request('assign isolated test role','system/permission/assign-user-role','POST',{'userId':user,'roleIds':[role]})
    login=http.call('/admin-api/system/auth/login','POST',{'username':'oa'+suffix[-14:],'password':'Aicloud@123'})
    if login.get('code')!=0: raise AssertionError('验证用户登录失败')
    actor={'userId':user,'roleId':role,'deptId':dept,'token':login['data']['accessToken']}
    return actor

def dictionary(type):
    rows = request('dictionary ' + type, 'system/dict-data/simple-list')
    return int(next(row['value'] for row in rows if row['dictType'] == type))

def approval(resource, data, status=2):
    id = op(resource, 'create', data=data)
    saved = op(resource, 'get', id)
    assert_value(resource + ' persists draft', saved['status'] == -1)
    op(resource, 'submit', id)
    saved = op(resource, 'get', id)
    instance = saved['processInstanceId']
    assert_value(resource + ' awaits actual approval', saved['status'] == 1 and instance)
    deadline = time.monotonic() + 15; task = None
    while time.monotonic() < deadline:
        page = http.call('/admin-api/bpm/task/todo-page?pageNo=1&pageSize=100', token=token)
        if page.get('code') != 0: raise AssertionError('审批待办读取失败')
        task = next((item for item in page['data']['list'] if item['processInstanceId'] == instance), None)
        if task: break
        time.sleep(.25)
    assert_value(resource + ' has approval task', task is not None)
    request(resource + (' approve' if status == 2 else ' reject'), 'bpm/task/' + ('approve' if status == 2 else 'reject'),
            'PUT', {'id':task['id'], 'reason':'Local OA verification', 'variables':{}})
    deadline = time.monotonic() + 15
    while time.monotonic() < deadline:
        saved = http.call('/admin-api/oa/' + resource + '/get?id=' + str(id), token=token)
        if saved.get('code') == 0 and saved['data']['status'] == status: break
        time.sleep(.25)
    assert_value(resource + ' callback persists business outcome', saved.get('code') == 0 and saved['data']['status'] == status)
    return id, saved['data']

def applications():
    urgency = dictionary('oa_apply_urgency')
    common = {'title':name,'urgency':urgency,'startTime':start,'endTime':end,'reason':'Local business verification'}
    leave_id, leave = approval('leave-apply', {**common,'type':dictionary('oa_leave_type'),'fileUrls':[]})
    attendance = request('leave reads generated attendance','oa/attendance/my-page?pageNo=1&pageSize=100')
    assert_value('leave approval creates attendance', any(row['type']==3 and row['attendanceTime']==start for row in attendance['list']))
    approval('overtime-apply', {**common,'type':dictionary('oa_overtime_type')})
    approval('regular-apply', {**common,**{field:'Local verification' for field in ['experience','understanding','growth','deficiency','improvement','suggestion']}})
    approval('resign-apply', {**common,'handoverUserId':1,'unfinishedWork':'No actual employment change','hasPendingReimbursement':False})
    id, saved = approval('reimbursement', {'title':name,'urgency':urgency,'witnessUserId':1,'customerName':'Local verification',
            'paymentMethod':dictionary('oa_reimbursement_payment_method'),'fileUrls':[],'reason':'Local expense',
            'items':[{'expenseTime':start,'expenseType':dictionary('oa_expense_type'),'description':name,'invoiceCount':1,'price':12.34}]})
    assert_value('expense total computed from persisted lines', saved['totalPrice']==12.34)
    # Rejection must update the OA record as well as the BPM history.
    approval('leave-apply', {**common,'title':name+' reject','type':dictionary('oa_leave_type')}, status=3)

def travel():
    id, saved = approval('travel-apply', {'reason':name,'startTime':start,'endTime':end,'estimatedPrice':100,'fileUrls':[],
            'items':[{'departureAreaId':110100,'arrivalAreaId':310100,'startTime':start,'endTime':end,'transportType':dictionary('oa_transport_type')}]})
    receipt=upload_content(b'Local OA travel reimbursement receipt for verification', 'receipt-'+suffix+'.txt')
    reimbursement, saved = approval('travel-reimbursement', {'reason':name,'startTime':start,'endTime':end,'travelApplyId':id,'fileUrls':[receipt],
            'items':[{'expenseType':dictionary('oa_expense_type'),'expenseTime':start,'departureCity':'北京','arrivalCity':'上海','price':45.67,'description':name}]})
    assert_value('travel reimbursement persists actual line total', saved['totalPrice']==45.67)
    saved = op('travel-apply','get',id)
    assert_value('travel reimbursement updates linked application', saved['reimburseStatus']==True)

def supplies():
    item = op('supply-item','create',data={'deptId':1,'name':name,'category':5,'manageType':2,'unit':'台',
            'stockQuantity':5,'minStockQuantity':1,'status':0,'sort':999})
    id, saved = approval('supply-apply', {'applyTime':start,'useType':2,'pickupMethod':1,'reason':name,'fileUrls':[],
            'items':[{'itemId':item,'applyQuantity':2}]})
    detail = saved['items'][0]['id']
    op('supply-issue','issue',detail,{'issuedQuantity':2,'issueRemark':name})
    saved = op('supply-item','get',item)
    assert_value('supply issuing deducts inventory', saved['stockQuantity']==3)
    op('supply-issue','return',detail,{'quantity':1,'returnRemark':name})
    assert_value('partial return restores one inventory unit',op('supply-item','get',item)['stockQuantity']==4)
    op('supply-issue','return',detail,{'quantity':1,'returnRemark':name})
    assert_value('full return restores original inventory',op('supply-item','get',item)['stockQuantity']==5)
    saved = op('supply-apply','get',id)
    assert_value('supply return records quantities and final status',saved['items'][0]['returnedQuantity']==2 and saved['items'][0]['status']==3)

def meetings():
    room = op('meeting-room','create',data={'name':name,'location':'Local test room','type':1,'managerUserId':1,'status':0,
            'seatCount':5,'equipments':[],'allowBooking':True,'needApproval':True,'bookingScope':0,'bookingUserIds':[],'sort':999,'fileUrls':[]})
    data = {'roomId':room,'title':name,'startTime':start,'endTime':end,'moderatorUserId':1,'attendeeUserIds':[1],'reminderType':1,'fileUrls':[]}
    id, saved = approval('meeting-room-booking', data)
    assert_value('approved meeting reserves room',saved['useStatus']==0)
    duplicate = op('meeting-room-booking','create',data={**data,'title':name+' conflict'})
    result = http.call('/admin-api/oa/meeting-room-booking/submit?id='+str(duplicate),'PUT',token=token)
    assert_value('overlapping meeting reservation rejected',result.get('code') not in [None,0])
    op('meeting-room-booking','delete',duplicate)
    # Cancellation releases an approved slot without editing the approval history.
    op('meeting-room-booking','cancel',id)
    assert_value('meeting cancellation releases reservation',op('meeting-room-booking','get',id)['useStatus']==3)

def vehicles():
    vehicle = op('vehicle','create',data={'no':'验'+suffix[-10:],'name':name,'deptId':1,'type':'商务车','seatCount':7,'barePrice':100,'status':0,'sort':999})
    id, saved = approval('vehicle-apply', {'vehicleId':vehicle,'startTime':start,'endTime':end,
            'startLocation':'Local A','endLocation':'Local B','reason':name,'fileUrls':[]})
    assert_value('approved vehicle application awaits return',saved['returnStatus']==1)
    now = int(time.time()*1000)
    return_id, returned = approval('vehicle-return', {'applyId':id,'actualStartTime':now-3600000,'startLocation':'Local A','reason':name,
            'actualReturnTime':now-1000,'returnLocation':'Local A','fileUrls':[]})
    assert_value('vehicle return closes linked application',op('vehicle-apply','get',id)['returnStatus']==3)

def seals():
    seal = op('seal','create',data={'deptId':1,'name':name,'type':1,'keeperUserId':1,'keeperDeptId':1,'status':0,'sort':999})
    id, saved = approval('seal-apply', {'sealId':seal,'reason':name,'type':5,'mode':2,'documentTitle':name,'documentCount':1,
            'expectedUseTime':start,'expectedReturnTime':end,'urgent':False,'fileUrls':[]})
    assert_value('approval grants permission without fabricating seal handover',saved['useStatus']==0)
    page=op('seal-apply','register-page',data={'pageNo':1,'pageSize':10,'sealId':seal})
    assert_value('keeper can find approved applications for registration',any(row['id']==id for row in page['list']))
    op('seal-apply','use',id)
    assert_value('keeper handover marks seal in use',op('seal','get',seal)['status']==2)
    op('seal-apply','return',id)
    assert_value('returned seal becomes available',op('seal','get',seal)['status']==0)
    saved=op('seal-apply','get',id)
    assert_value('seal use and return times persisted',saved['useStatus']==3 and saved['actualUseTime'] and saved['actualReturnTime'])

def documents():
    doc_prefix='验字'+suffix
    template = op('officialdoc-template','create',data={'name':name,'authorityName':'aicloud','fontSize':22,'noPrefix':doc_prefix,
            'separatorType':dictionary('oa_official_doc_separator_type'),'status':0,'sort':999})
    secrecy=dictionary('oa_official_doc_secret_level'); urgency=dictionary('oa_official_doc_urgency_level')
    send, saved = approval('officialdoc-send', {'templateId':template,'title':name,'noPrefix':doc_prefix,'year':datetime.now().year,'sequence':1,
            'secrecyLevel':secrecy,'urgencyLevel':urgency,'disclosureType':dictionary('oa_official_doc_public_category'),
            'issueTime':start,'sendDeptId':1,'mainDeptIds':[1],'copyDeptIds':[],'content':name,'fileUrls':[]})
    received=request('official doc receives issued document','oa/officialdoc-receive/page?pageNo=1&pageSize=100')
    linked=next((row for row in received['list'] if row.get('sendId')==send),None)
    assert_value('approved issuing distributes to recipient department',linked is not None)
    receive = op('officialdoc-receive','create',data={'title':name+' external','documentNo':'验收'+suffix,'secrecyLevel':secrecy,
            'urgencyLevel':urgency,'receiveType':0,'receiveTime':start,'receiveDeptId':1,'handlerUserId':1,'summary':name,'fileUrls':[]})
    op('officialdoc-receive','claim',linked['id'])
    assert_value('automatic document claim assigns current owner',op('officialdoc-receive','get',linked['id'])['handleStatus']==1)
    op('officialdoc-receive','submit',receive)
    saved=op('officialdoc-receive','get',receive)
    todo=request('official receive approval task','bpm/task/todo-page?pageNo=1&pageSize=100')
    task=next(row for row in todo['list'] if row['processInstanceId']==saved['processInstanceId'])
    request('approve official receive','bpm/task/approve','PUT',{'id':task['id'],'reason':'Local OA verification','variables':{}})
    deadline=time.monotonic()+15
    while time.monotonic()<deadline:
        saved=http.call('/admin-api/oa/officialdoc-receive/get?id='+str(receive),token=token)
        if saved.get('code')==0 and saved['data']['status']==2:break
        time.sleep(.25)
    assert_value('official receive callback completes handling',saved.get('code')==0 and saved['data']['status']==2)

def collaboration():
    colleague=local_actor()
    announcement=op('announcement','create',data={'type':1,'priority':1,'title':name,'content':name,'top':False})
    received=op('announcement','received-page',data={'pageNo':1,'pageSize':100},bearer=colleague['token'])
    assert_value('published announcement delivered to test subordinate',any(row['id']==announcement for row in received['list']))
    op('announcement','update-read-status',announcement,bearer=colleague['token'])
    assert_value('announcement read status persisted',op('announcement','get',announcement,bearer=colleague['token'])['readStatus']==True)
    op('announcement','delete',announcement)
    contact_category=op('contact-category','create',data={'name':name,'sort':999})
    contact=op('contact','create',data={'categoryId':contact_category,'name':name,'mobile':'13900000000','email':'local@example.invalid'})
    assert_value('contact persists category and email',op('contact','get',contact)['categoryId']==contact_category)
    op('contact','delete',contact);op('contact-category','delete',contact_category)
    category=op('note-category','create',data={'name':name,'sort':999})
    note=op('note','create',data={'categoryId':category,'type':1,'priority':1,'title':name,'content':name,'fileUrls':[]})
    op('note','update-favorite',note,{'favorite':True})
    assert_value('note favorite persisted',op('note','get',note)['favorite']==True)
    op('note','delete',note);op('note-category','delete',category)
    plan=op('plan','create',data={'type':1,'status':1,'title':name,'content':name,'startTime':start,'endTime':end,'fileUrls':[]})
    assert_value('work plan persisted',op('plan','get',plan)['title']==name)
    op('plan','delete',plan)
    work=op('work-report','create',data={'type':1,'title':name,'startTime':start,'endTime':end,'summary':name,'plan':name,
            'workItems':[{'content':name,'progress':100}],'planItems':[{'content':name}],'fileUrls':[]})
    op('work-report','submit',work)
    assert_value('submitted work report persisted',op('work-report','get',work)['status']==2)
    discussion=op('discussion','create',data={'type':3,'title':name,'content':name,'fileUrls':[],'voteMultiple':False,
            'voteStartTime':int(time.time()*1000)-3600000,'voteEndTime':int(time.time()*1000)+3600000,
            'voteOptions':[{'title':'A','sort':1},{'title':'B','sort':2}]})
    saved=op('discussion','get',discussion)
    op('discussion-vote','create',data={'discussionId':discussion,'optionIds':[saved['voteOptions'][0]['id']]})
    saved=op('discussion','get',discussion)
    assert_value('discussion vote count persisted',saved['voteOptions'][0]['voteCount']==1)
    op('discussion-like','create',data={'discussionId':discussion})
    assert_value('discussion like persisted',op('discussion','get',discussion)['likeCount']==1)
    op('discussion','delete',discussion)
    task=op('task','create',data={'type':1,'status':1,'top':False,'canceled':False,'title':name,'description':name,
            'startTime':start,'endTime':end,'receiverUserIds':[1]})
    for status in [2,3,4]:op('task','feedback',data={'taskId':task,'publisher':False,'status':status,'content':name})
    op('task','feedback',data={'taskId':task,'publisher':True,'status':5,'content':name})
    saved=op('task','get',task)
    assert_value('task feedback persists completion and history',saved['status']==5 and len(saved['logs'])>=4)
    op('task','delete',task)
    viewer=http.call('/admin-api/system/auth/login','POST',{'username':'viewer','password':'Aicloud@123'})['data']['accessToken']
    request('viewer cannot create OA plan','oa/plan/create','POST',{'type':1,'status':1,'title':name,'content':name,'startTime':start,'endTime':end},expected=403,bearer=viewer)

def upload_content(content,filename,private=False):
    boundary='oa-'+suffix
    payload=(f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{filename}"\r\n'
             'Content-Type: text/plain\r\n\r\n').encode()+content+f'\r\n--{boundary}--\r\n'.encode()
    upload_req=urllib.request.Request(http.BASE+'/admin-api/infra/file/upload?privateFile='+str(private).lower(),data=payload,method='POST',
            headers={'tenant-id':'1','Authorization':'Bearer '+token,'Content-Type':'multipart/form-data; boundary='+boundary})
    upload=json.loads(urllib.request.urlopen(upload_req,timeout=25).read())
    assert_value('actual verification attachment upload',upload.get('code')==0)
    uploaded=upload['data'];url=uploaded['url'] if isinstance(uploaded,dict) else uploaded
    parsed=urllib.parse.urlparse(url)
    assert_value('verification attachment remains on local storage',parsed.hostname in ['localhost','127.0.0.1'])
    return url

def cloud_drive():
    colleague=local_actor()
    content=b'aicloud OA persistent cloud drive verification'
    filename='oa-'+suffix+'.txt'
    url=upload_content(content,filename,True)
    try:
        urllib.request.urlopen(url,timeout=25)
        anonymous_code=200
    except urllib.error.HTTPError as error:anonymous_code=error.code
    assert_value('cloud drive raw private file rejects anonymous access',anonymous_code==403)
    folder=op('file-node','create',data={'parentId':0,'type':0,'name':name})
    file=op('file-node','create',data={'parentId':folder,'type':1,'name':filename,'url':url,'size':len(content)})
    saved=op('file-node','get',file)
    assert_value('cloud drive MySQL stores real file metadata',saved['size']==len(content) and saved['parentId']==folder)
    blocked=http.call('/admin-api/oa/file-node/get?id='+str(file),token=colleague['token'])
    assert_value('cloud drive private node denied to other user',blocked.get('code') not in [None,0])
    grant=op('file-permission','save',data={'nodeId':folder,'subjectType':1,'subjectId':colleague['userId'],'level':1,'inherit':True})
    saved=op('file-node','get',file,bearer=colleague['token'])
    assert_value('cloud drive inherited read permission',saved['id']==file and not saved.get('url'))
    denied=http.call('/admin-api/oa/file-node/update-name','PUT',{'id':file,'name':'denied-'+filename},token=colleague['token'])
    assert_value('cloud drive read-only grant cannot rename',denied.get('code') not in [None,0])
    op('file-permission','save',data={'nodeId':folder,'subjectType':1,'subjectId':colleague['userId'],'level':2,'inherit':True})
    saved=op('file-node','get',file,bearer=colleague['token'])
    download=urllib.parse.urlparse(saved['url'])
    assert_value('cloud drive shared download stays local',download.hostname in ['localhost','127.0.0.1'])
    body=urllib.request.urlopen(urllib.request.Request(http.BASE+download.path+'?'+download.query,
         headers={'tenant-id':'1','Authorization':'Bearer '+colleague['token']}),timeout=25).read()
    assert_value('cloud drive shared file readback',body==content)
    op('file-permission','delete',grant)
    denied=http.call('/admin-api/oa/file-node/get?id='+str(file),token=colleague['token'])
    assert_value('cloud drive grant revocation takes effect',denied.get('code') not in [None,0])
    op('file-favorite','create',data={'nodeId':file})
    copy=op('file-node','copy',file,{'parentId':0})
    assert_value('cloud drive copy preserves metadata',op('file-node','get',copy)['size']==len(content))
    op('file-node','recycle',folder)
    denied=http.call('/admin-api/oa/file-node/get?id='+str(file),token=token)
    assert_value('recycled folder hides child nodes',denied.get('code') not in [None,0])
    op('file-node','restore',folder)
    assert_value('cloud drive restoring folder restores children',op('file-node','get',file)['id']==file)
    for id in [copy,folder]:
        op('file-node','recycle',id);op('file-node','delete',id)
    files=request('locate uploaded verification content','infra/file/page?pageNo=1&pageSize=10&name='+filename)
    for row in files['list']:
        if row['url']==url:request('delete only verification content','infra/file/delete?id='+str(row['id']),'DELETE')

def reminders():
    now=int(time.time()*1000)
    schedule=op('schedule','create',data={'type':1,'priority':1,'title':name,'description':name,'startTime':now+3600000,
            'endTime':now+7200000,'remind':True,'participantUserIds':[1]})
    deadline=time.monotonic()+90;found=False
    while time.monotonic()<deadline:
        messages=http.call('/admin-api/system/notify-message/my-page?pageNo=1&pageSize=100',token=token)
        if messages.get('code')!=0:raise AssertionError('read schedule notification: '+messages.get('msg','Unexpected response'))
        found=any(name in item['templateContent'] for item in messages['data']['list'])
        if found:break
        time.sleep(2)
    assert_value('local scheduler delivers actual schedule notification',found)
    assert_value('schedule reminder is delivered once per recipient',
        sum(name in item['templateContent'] for item in messages['data']['list'])==1)
    op('schedule','delete',schedule)

def mail_configuration():
    provider=op('mail-provider','create',data={'name':name,'status':0,
            'imap':{'host':'127.0.0.1','port':1993,'sslEnable':True,'starttlsEnable':False},
            'smtp':{'host':'127.0.0.1','port':1465,'sslEnable':True,'starttlsEnable':False}})
    assert_value('mail local provider configuration persisted',op('mail-provider','get',provider)['imap']['host']=='127.0.0.1')
    op('mail-provider','delete',provider)

GROUPS={'applications':applications,'travel':travel,'supplies':supplies,'meetings':meetings,'vehicles':vehicles,
        'seals':seals,'documents':documents,'collaboration':collaboration,'cloud-drive':cloud_drive,
        'reminders':reminders,'mail':mail_configuration}
if set(args.groups)-GROUPS.keys():parser.error('Unknown OA group')
selected=args.groups or list(GROUPS)
login=http.call('/admin-api/system/auth/login','POST',{'username':os.environ.get('AICLOUD_INIT_USERNAME','admin'),
               'password':os.environ.get('AICLOUD_INIT_PASSWORD','Aicloud@123')})
if login.get('code')!=0:raise SystemExit('Administrator login failed')
token=login['data']['accessToken']
docs=http.call('/admin-api/oa/v3/api-docs',token=token)
if not docs.get('paths'):raise SystemExit('OA not available through local gateway')
path=ROOT/'docs/oa-business-verification.json'
results=json.loads(path.read_text()) if path.exists() else {}
failed=False
for group in selected:
    checks=[]; outcome={'checks':checks,'passed':False,'runName':name}
    try:
        GROUPS[group]()
        outcome['passed']=all(check['passed'] for check in checks)
    except Exception as error:
        failed=True;outcome['error']=str(error);print('FAIL '+group+': '+str(error),flush=True)
    results[group]=outcome
    path.write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n')
if actor:
    checks=[]
    try:
        request('delete isolated test user','system/user/delete?id='+str(actor['userId']),'DELETE')
        request('delete isolated test role','system/role/delete?id='+str(actor['roleId']),'DELETE')
        request('delete isolated test department','system/dept/delete?id='+str(actor['deptId']),'DELETE')
    except Exception as error:
        failed=True;print('FAIL verification actor cleanup: '+str(error),flush=True)
raise SystemExit(1 if failed else 0)
