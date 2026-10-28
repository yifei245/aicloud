#!/usr/bin/env python3
"""Local gateway business verification. Retains isolated documents for audit; never logs credentials."""
import concurrent.futures,importlib.util,json,sys,time
from pathlib import Path
from datetime import datetime
from zoneinfo import ZoneInfo

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('http_local',ROOT/'scripts/smoke-test.py')
http=importlib.util.module_from_spec(spec);spec.loader.exec_module(http)
suffix=str(time.time_ns())[-12:];name='MES库存验收'+suffix;now=int(time.time()*1000)
report={'checkedAt':datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),'passed':False,'checks':[],'records':{}}
token=None

def check(label,condition):
    report['checks'].append({'check':label,'passed':bool(condition)})
    print(('PASS ' if condition else 'FAIL ')+label,flush=True)
    if not condition:raise AssertionError(label)

def request(path,method='GET',data=None):
    value=http.call('/admin-api/'+path,method,data,token=token)
    if value.get('code')!=0:raise AssertionError(path.split('?')[0]+': '+str(value.get('code'))+' '+str(value.get('msg')))
    return value.get('data')

def eventually(label,read,condition):
    deadline=time.monotonic()+30;last=None
    while time.monotonic()<deadline:
        last=read()
        if condition(last):check(label,True);return last
        time.sleep(.4)
    report['lastObserved']=last;check(label,False)

def put(resource,id,action):return request(f'mes/wm/{resource}/{action}?id={id}','PUT')

def mes_location(code,warehouse=None,location=None):
    if location is not None:
        return request('mes/wm/warehouse-area/create','POST',{'code':code,'name':name,'locationId':location,'status':0,'frozen':False,'allowItemMixing':True,'allowBatchMixing':True})
    if warehouse is not None:
        return request('mes/wm/warehouse-location/create','POST',{'code':code,'name':name,'warehouseId':warehouse,'frozen':False})
    return request('mes/wm/warehouse/create','POST',{'code':code,'name':name,'frozen':False})

def mapping(kind,source,source_id):
    return next((m['target_id'] for m in request(f'wms/integration/mappings?kind={kind}&source={source}') if m['source_id']==source_id),None)

def canonical(sku,warehouse):
    rows=request(f'wms/inventory/page?pageNo=1&pageSize=100&type=warehouse&skuId={sku}&warehouseId={warehouse}')['list']
    return sum(row['quantity'] for row in rows)

def stock(item,warehouse):
    return request(f'mes/wm/material-stock/page?pageNo=1&pageSize=100&itemId={item}&warehouseId={warehouse}')['list']

def create_issue(item,order,selected,quantity,tag):
    issue=request('mes/wm/product-issue/create','POST',{'code':tag+suffix,'name':name,'workOrderId':order,'requiredTime':now})
    line=request('mes/wm/product-issue-line/create','POST',{'issueId':issue,'itemId':item,'quantity':quantity})
    detail={key:selected.get(key) for key in ['batchId','batchCode','warehouseId','locationId','areaId']}
    detail.update(issueId=issue,lineId=line,itemId=item,materialStockId=selected['id'],quantity=quantity)
    put('product-issue',issue,'submit')
    request('mes/wm/product-issue-detail/create','POST',detail)
    put('product-issue',issue,'stock')
    return issue

try:
    token=request('system/auth/login','POST',{'username':'admin','password':'Aicloud@123'})['accessToken']
    unit_name='件'+suffix[-8:]
    unit=request('erp/product-unit/create','POST',{'name':unit_name,'status':0})
    category=request('erp/product-category/create','POST',{'parentId':0,'name':name,'code':suffix,'sort':1,'status':0})
    product=request('erp/product/create','POST',{'name':name,'barCode':suffix,'categoryId':category,'unitId':unit,'status':0,'purchasePrice':10,'salePrice':20,'minPrice':1})
    warehouse=request('erp/warehouse/create','POST',{'name':name,'sort':999,'status':0,'warehousePrice':0,'truckagePrice':0})
    inbound=request('erp/stock-in/create','POST',{'inTime':now,'remark':name,'items':[{'warehouseId':warehouse,'productId':product,'productPrice':10,'count':10}]})
    request(f'erp/stock-in/update-status?id={inbound}&status=20','PUT')
    eventually('ERP receipt creates ten authoritative WMS units',lambda:request(f'erp/stock/get-count?productId={product}&warehouseId={warehouse}'),lambda q:q==10)
    sku=mapping('PRODUCT','ERP',product);target=mapping('WAREHOUSE','ERP',warehouse)
    mes_unit=request('mes/md/unit-measure/create','POST',{'code':'U'+suffix,'name':unit_name,'primaryFlag':True,'changeRate':1,'status':0})
    item_type=request('mes/md/item-type/create','POST',{'code':'T'+suffix,'name':name,'parentId':0,'itemOrProduct':'ITEM','sort':999,'status':0})
    item=request('mes/md/item/create','POST',{'code':'I'+suffix,'name':name,'unitMeasureId':mes_unit,'itemTypeId':item_type,'safeStockFlag':False,'batchFlag':True})
    flags=['produceDateFlag','expireDateFlag','receiptDateFlag','vendorFlag','clientFlag','salesOrderCodeFlag','purchaseOrderCodeFlag','workorderFlag','taskFlag','workstationFlag','toolFlag','moldFlag','lotNumberFlag','qualityStatusFlag']
    request('mes/md/item-batch-config/save','POST',{'itemId':item,**{flag:False for flag in flags},'lotNumberFlag':True})
    request(f'mes/md/item/update-status?id={item}&status=0','PUT')
    mes_wh=mes_location('WH'+suffix);loc=mes_location('L'+suffix,warehouse=mes_wh);area=mes_location('A'+suffix,location=loc)
    request('wms/integration/bind','POST',{'kind':'PRODUCT','source':'MES','sourceId':item,'targetId':sku})
    request('wms/integration/bind','POST',{'kind':'WAREHOUSE','source':'MES','sourceId':mes_wh,'targetId':target})
    workshop=request('mes/md-workshop/create','POST',{'code':'WS'+suffix,'name':name,'status':0})
    process=request('mes/pro/process/create','POST',{'code':'P'+suffix,'name':name,'status':0})
    # Existing workstation creation initializes the protected line-side hierarchy.
    workstation=request('mes/md-workstation/create','POST',{'code':'ST'+suffix,'name':name,'workshopId':workshop,'processId':process,'status':0})
    # Preserve existing shared line-side metadata and mappings.
    virtual=next((w for w in request('mes/wm/warehouse/simple-list') if w['code']=='WIP_VIRTUAL_WAREHOUSE'),None)
    if virtual is None:raise AssertionError('Workstation did not initialize the line-side warehouse')
    wip=virtual['id']
    locations=request(f'mes/wm/warehouse-location/simple-list?warehouseId={wip}')
    wip_loc=next((x['id'] for x in locations if x['code']=='WIP_VIRTUAL_LOCATION'),None) or mes_location('WIP_VIRTUAL_LOCATION',warehouse=wip)
    areas=request(f'mes/wm/warehouse-area/simple-list?locationId={wip_loc}')
    wip_area=next((x['id'] for x in areas if x['code']=='WIP_VIRTUAL_AREA'),None) or mes_location('WIP_VIRTUAL_AREA',location=wip_loc)
    wip_target=mapping('WAREHOUSE','MES',wip)
    if not wip_target:
        wip_target=request('wms/warehouse/create','POST',{'code':'WIP'+suffix,'name':'线边'+name,'sort':999})
        request('wms/integration/bind','POST',{'kind':'WAREHOUSE','source':'MES','sourceId':wip,'targetId':wip_target})
    report['records'].update(erpProduct=product,erpWarehouse=warehouse,erpReceipt=inbound,wmsSku=sku,wmsWarehouse=target,mesItem=item,mesWarehouse=mes_wh,wipWarehouse=wip,wipWmsWarehouse=wip_target,process=process,workstation=workstation)
    allocation={'businessKey':'verify-'+suffix,'itemId':item,'warehouseId':mes_wh,'locationId':loc,'areaId':area,'quantity':10,'batch':{'itemId':item,'lotNumber':'RAW'+suffix}}
    position=request('mes/wm/material-stock/allocate','POST',allocation)
    eventually('MES putaway exposes ten units in the selected WMS batch/bin',lambda:request(f'mes/wm/material-stock/get?id={position}'),lambda s:s['quantity']==10 and s['availableQuantity']==10)
    check('Putaway does not create extra physical stock',canonical(sku,target)==10)
    check('Replayed putaway returns the original position',request('mes/wm/material-stock/allocate','POST',allocation)==position)
    check('Replayed putaway does not duplicate allocated quantity',request(f'mes/wm/material-stock/get?id={position}')['quantity']==10)
    denied=http.call('/admin-api/mes/wm/material-stock/allocate','POST',{**allocation,'businessKey':'over-'+suffix,'quantity':1},token=token)
    check('Already allocated stock cannot be allocated again',denied.get('code') not in [0,None])
    finished_product=request('erp/product/create','POST',{'name':'成品'+name,'barCode':'F'+suffix,'categoryId':category,'unitId':unit,'status':0,'purchasePrice':10,'salePrice':30,'minPrice':1})
    finished_sku=eventually('Finished product is projected from ERP master data',lambda:mapping('PRODUCT','ERP',finished_product),bool)
    product_type=request('mes/md/item-type/create','POST',{'code':'PT'+suffix,'name':'成品'+name,'parentId':0,'itemOrProduct':'PRODUCT','sort':999,'status':0})
    finished_item=request('mes/md/item/create','POST',{'code':'FI'+suffix,'name':'成品'+name,'unitMeasureId':mes_unit,'itemTypeId':product_type,'safeStockFlag':False,'batchFlag':False})
    request('wms/integration/bind','POST',{'kind':'PRODUCT','source':'MES','sourceId':finished_item,'targetId':finished_sku})
    request('mes/md/product-bom/create','POST',{'itemId':finished_item,'bomItemId':item,'quantity':1,'status':0})
    request(f'mes/md/item/update-status?id={finished_item}&status=0','PUT')
    order=request('mes/pro/work-order/create','POST',{'code':'WO'+suffix,'name':name,'type':1,'orderSourceType':1,'productId':finished_item,'quantity':2,'requestDate':now+86400000})
    request(f'mes/pro/work-order/confirm?id={order}','PUT')
    selected=request(f'mes/wm/material-stock/get?id={position}')
    issue=create_issue(item,order,selected,4,'ISS')
    put('product-issue',issue,'finish')
    eventually('MES issue reduces the same ERP/WMS physical warehouse to six',lambda:canonical(sku,target),lambda q:q==6)
    wip_stock=eventually('MES issue creates four units in the mapped line-side warehouse',lambda:stock(item,wip),lambda rows:sum(s['quantity'] for s in rows)==4)[0]
    check('Issue preserves exact batch identity',wip_stock['batchId']==selected['batchId'] and wip_stock['batchCode']==selected['batchCode'])
    replay=http.call(f'/admin-api/mes/wm/product-issue/finish?id={issue}','PUT',token=token)
    check('Duplicate finished issue cannot deduct stock twice',replay.get('code') not in [0,None] and canonical(sku,target)==6)
    returned=request('mes/wm/return-issue/create','POST',{'code':'RET'+suffix,'name':name,'workOrderId':order,'type':1,'returnDate':now})
    line=request('mes/wm/return-issue-line/create','POST',{'issueId':returned,'itemId':item,'quantity':2,'materialStockId':wip_stock['id'],'batchId':wip_stock['batchId'],'batchCode':wip_stock['batchCode'],'rqcCheckFlag':False})
    request('mes/wm/return-issue-detail/create','POST',{'issueId':returned,'lineId':line,'itemId':item,'quantity':2,'materialStockId':wip_stock['id'],'batchId':wip_stock['batchId'],'batchCode':wip_stock['batchCode'],'warehouseId':mes_wh,'locationId':loc,'areaId':area})
    for action in ['submit','stock','finish']:put('return-issue',returned,action)
    eventually('Return material restores physical warehouse stock to eight',lambda:canonical(sku,target),lambda q:q==8)
    eventually('Return material reduces line-side physical stock to two',lambda:canonical(sku,wip_target),lambda q:q==2)
    report['records'].update(mesPosition=position,batchId=selected['batchId'],batchCode=selected['batchCode'],workOrder=order,issue=issue,returnIssue=returned)
    # Actual route/BOM feedback consumes the remaining raw WIP and creates a distinct finished product.
    route=request('mes/pro/route/create','POST',{'code':'R'+suffix,'name':name})
    request('mes/pro/route-process/create','POST',{'routeId':route,'processId':process,'sort':1,'keyFlag':True,'checkFlag':False,'linkType':0,'prepareTime':0,'waitTime':0,'colorCode':'#0099cc'})
    request('mes/pro/route-product/create','POST',{'routeId':route,'itemId':finished_item,'quantity':1,'productionTime':1,'timeUnitType':'HOUR'})
    request('mes/pro/route-product-bom/create','POST',{'routeId':route,'processId':process,'productId':finished_item,'itemId':item,'quantity':1})
    request(f'mes/pro/route/update-status?id={route}&status=0','PUT')
    production_order=order
    task=request('mes/pro/task/create','POST',{'workOrderId':production_order,'workstationId':workstation,'routeId':route,'processId':process,'itemId':finished_item,'quantity':2,'startTime':now,'endTime':now+86400000,'duration':1,'colorCode':'#0099cc'})
    feedback=request('mes/pro/feedback/create','POST',{'code':'FB'+suffix,'type':1,'workOrderId':production_order,'workstationId':workstation,'routeId':route,'processId':process,'itemId':finished_item,'taskId':task,'scheduledQuantity':2,'feedbackQuantity':2,'qualifiedQuantity':2,'unqualifiedQuantity':0,'uncheckQuantity':0,'feedbackUserId':1,'approveUserId':1,'feedbackTime':now})
    request(f'mes/pro/feedback/submit?id={feedback}','PUT');request(f'mes/pro/feedback/approve?id={feedback}','PUT')
    eventually('Approved production feedback consumes two raw-material WIP units',lambda:canonical(sku,wip_target),lambda q:q==0)
    finished_stock=eventually('Production output creates two finished WIP units',lambda:stock(finished_item,wip),lambda rows:sum(s['quantity'] for s in rows)==2)[0]
    receipt=request('mes/wm/product-receipt/create','POST',{'code':'FG'+suffix,'name':'成品'+name,'workOrderId':production_order,'receiptDate':now})
    receipt_line=request('mes/wm/product-receipt-line/create','POST',{'receiptId':receipt,'itemId':finished_item,'materialStockId':finished_stock['id'],'quantity':2})
    request('mes/wm/product-receipt-detail/create','POST',{'receiptId':receipt,'lineId':receipt_line,'itemId':finished_item,'quantity':2,'warehouseId':mes_wh,'locationId':loc,'areaId':area})
    for action in ['submit','stock','finish']:put('product-receipt',receipt,action)
    eventually('Finished receipt transfers two units to the physical WMS warehouse',lambda:canonical(finished_sku,target),lambda q:q==2)
    eventually('Finished receipt empties finished WIP without creating extra units',lambda:canonical(finished_sku,wip_target),lambda q:q==0)
    check('ERP sees the finished goods produced by MES',request(f'erp/stock/get-count?productId={finished_product}&warehouseId={warehouse}')==2)
    report['records'].update(finishedErpProduct=finished_product,finishedMesItem=finished_item,productionWorkOrder=production_order,productionTask=task,productionFeedback=feedback,finishedReceipt=receipt)
    # A generic ERP outbound must consume allocated stock and immediately appear in MES.
    customer=request('erp/customer/create','POST',{'name':name,'status':0,'sort':999})
    outbound=request('erp/stock-out/create','POST',{'customerId':customer,'outTime':now,'remark':name,'items':[{'warehouseId':warehouse,'productId':product,'productPrice':10,'count':3}]})
    request(f'erp/stock-out/update-status?id={outbound}&status=20','PUT')
    eventually('ERP outbound is reflected in MES batch stock without resync',lambda:request(f'mes/wm/material-stock/get?id={position}')['quantity'],lambda q:q==5)
    check('MES and ERP now read the same remaining warehouse stock',request(f'erp/stock/get-count?productId={product}&warehouseId={warehouse}')==5)
    report['records']['erpOutbound']=outbound
    # Cancellation changes no physical inventory; competing issues cannot oversell one batch.
    selected=request(f'mes/wm/material-stock/get?id={position}')
    order=request('mes/pro/work-order/create','POST',{'code':'NEXT'+suffix,'name':'后续'+name,'type':1,'orderSourceType':1,'productId':finished_item,'quantity':3,'requestDate':now+86400000})
    request(f'mes/pro/work-order/confirm?id={order}','PUT')
    cancelled=create_issue(item,order,selected,1,'CANCEL')
    put('product-issue',cancelled,'cancel')
    check('Cancelled unexecuted issue leaves warehouse and batch stock unchanged',canonical(sku,target)==5 and request(f'mes/wm/material-stock/get?id={position}')['quantity']==5)
    competitors=[create_issue(item,order,selected,3,tag) for tag in ['RACEA','RACEB']]
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
        responses=list(executor.map(lambda id:http.call(f'/admin-api/mes/wm/product-issue/finish?id={id}','PUT',token=token),competitors))
    check('Only one of two competing three-unit issues can succeed against five units',sum(r.get('code')==0 for r in responses)==1)
    eventually('Concurrent issues deduct exactly three physical units',lambda:canonical(sku,target),lambda q:q==2)
    eventually('Concurrent failure leaves no phantom batch reservation',lambda:request(f'mes/wm/material-stock/get?id={position}'),lambda s:s['quantity']==2 and s['reservedQuantity']==0 and s['availableQuantity']==2)
    report['records'].update(cancelledIssue=cancelled,competingIssues=competitors)
    report['passed']=all(c['passed'] for c in report['checks'])
except Exception as error:
    report['error']=str(error);print('FAIL '+str(error),flush=True)
finally:
    (ROOT/'docs/mes-stock-http-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
sys.exit(0 if report['passed'] else 1)
