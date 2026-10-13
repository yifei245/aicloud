#!/usr/bin/env python3
"""Verify local business services against their published OpenAPI contracts.

Core services must already be running. Starts and stops only the selected
aicloud applications; queries never leave localhost and no middleware changes.
"""
import argparse,importlib.util,json,socket,subprocess,sys,time,urllib.parse
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('aicloud_smoke',ROOT/'scripts/smoke-test.py')
http=importlib.util.module_from_spec(spec);spec.loader.exec_module(http)
parser=argparse.ArgumentParser()
parser.add_argument('profiles',nargs='*')
args=parser.parse_args()
apps=json.loads((ROOT/'docs/services.json').read_text())
selected=args.profiles or ['pay','product','promotion','trade','statistics','bpm','crm','erp',
                         'mp','report','iot','iot-gateway','mes','wms','im','hrm','fms','pms','oa','ai']
if set(selected)-{app['profile'] for app in apps}:parser.error('Unknown service profile')
report_path=ROOT/'docs/business-http-verification.json'
report=json.loads(report_path.read_text()) if report_path.exists() else {}
deadline=time.monotonic()+90
while True:
    login=http.call('/admin-api/system/auth/login','POST',{'username':'admin','password':'Aicloud@123'})
    if login.get('code')==0 or time.monotonic()>=deadline:break
    time.sleep(2)
if login.get('code')!=0:raise SystemExit('Core services must be healthy; administrator login failed')
token=login['data']['accessToken']
failed=False
def already_running(profile):
    pidfile=ROOT/'data/local'/f'{profile}.pid'
    if not pidfile.exists():return False
    app=next(a for a in apps if a['profile']==profile)
    state=subprocess.run(['ps','-p',pidfile.read_text().strip(),'-o','stat=,command='],capture_output=True,text=True)
    return state.returncode==0 and not state.stdout.lstrip().startswith('Z') and str(ROOT/app['directory']) in state.stdout

for profile in selected:
    app=next(app for app in apps if app['profile']==profile)
    base=f'http://127.0.0.1:{app["port"]}'
    outcome={'service':app['name'],'port':app['port'],'healthy':False,'queries':[],'skipped':[]}
    print(f'START {profile}',flush=True)
    started_dependencies=[]
    for dependency in (['product'] if profile=='statistics' else []):
        dep=next(a for a in apps if a['profile']==dependency)
        existed=already_running(dependency)
        subprocess.check_call([sys.executable,str(ROOT/'scripts/run-local.py'),'start',dependency])
        if not existed:started_dependencies.append(dependency)
        deadline=time.monotonic()+90
        while time.monotonic()<deadline:
            try:
                if http.call('/actuator/health',base=f'http://127.0.0.1:{dep["port"]}').get('status')=='UP':break
            except Exception:pass
            time.sleep(2)
        else:raise SystemExit('Dependency failed health check: '+dependency)
    existed=already_running(profile)
    subprocess.check_call([sys.executable,str(ROOT/'scripts/run-local.py'),'start',profile])
    try:
        deadline=time.monotonic()+100
        while time.monotonic()<deadline:
            try:
                if profile=='iot-gateway':
                    with socket.create_connection(('127.0.0.1',app['port']),timeout=2):pass
                    outcome['healthy']=True;break
                health=http.call('/actuator/health',base=base)
                if health.get('status')=='UP':outcome['healthy']=True;break
            except Exception:pass
            pidfile=ROOT/'data/local'/f'{profile}.pid'
            if pidfile.exists():
                state=subprocess.run(['ps','-p',pidfile.read_text().strip(),'-o','stat='],capture_output=True,text=True)
                if state.returncode or state.stdout.strip().startswith('Z'):break
            time.sleep(2)
        if not outcome['healthy']:raise AssertionError('Service failed its health check; inspect data/local/'+profile+'.log')
        if profile=='iot-gateway':
            outcome['protocol']='MQTT TCP listener'
        else:
            docs=http.call('/v3/api-docs',token=token,base=base)
            if not docs.get('openapi'):raise AssertionError('OpenAPI schema is unavailable')
            outcome['openapiPaths']=len(docs.get('paths',{}))
            for path,methods in sorted(docs.get('paths',{}).items()):
                operation=methods.get('get')
                if not operation or not path.startswith('/admin-api/') or not path.endswith(('page','/simple-list','/list')):continue
                params=operation.get('parameters',[])
                # Context-specific endpoints require a real parent record.
                required=[p['name'] for p in params if p.get('required') and p['in']!='header'
                          and p['name'] not in ('pageNo','pageSize')]
                if required:
                    outcome['skipped'].append({'path':path,'requires':required});continue
                query=urllib.parse.urlencode({'pageNo':1,'pageSize':10})
                try:
                    response=http.call(path+'?'+query,token=token,base=base)
                    passed=response.get('code')==0
                    item={'path':path,'passed':passed,'code':response.get('code')}
                    if not passed:item['error']=response.get('msg','Unexpected response')
                except Exception as error:item={'path':path,'passed':False,'error':str(error)}
                outcome['queries'].append(item)
                if not item['passed']:print('FAIL '+profile+' '+path+' '+item.get('error',''),flush=True)
            eligible=[q for q in outcome['queries'] if q['passed']]
            if eligible:
                path=eligible[0]['path']+'?pageNo=1&pageSize=10'
                routed=http.call(path,token=token)
                outcome['gatewayRoute']=routed.get('code')==0
                if not outcome['gatewayRoute']:raise AssertionError('Gateway/Nacos route failed')
        outcome['passed']=outcome['healthy'] and all(q['passed'] for q in outcome['queries'])
        print(('PASS' if outcome['passed'] else 'FAIL')+f' {profile}: {len(outcome["queries"])} database queries, '
              f'{outcome.get("openapiPaths",0)} OpenAPI paths',flush=True)
    except Exception as error:
        outcome['passed']=False;outcome['error']=str(error);print('FAIL '+profile+': '+str(error),flush=True)
    finally:
        if not existed:
            subprocess.check_call([sys.executable,str(ROOT/'scripts/run-local.py'),'stop',profile])
        for dependency in started_dependencies:
            subprocess.check_call([sys.executable,str(ROOT/'scripts/run-local.py'),'stop',dependency])
        report[profile]=outcome
        report_path.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
    failed=failed or not outcome.get('passed',False)
raise SystemExit(1 if failed else 0)
