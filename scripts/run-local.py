#!/usr/bin/env python3
"""Start/stop only aicloud Java processes; reuse existing local middleware."""
import argparse,json,os,signal,socket,subprocess,time,urllib.request
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser()
parser.add_argument('action',choices=['start','stop','status'])
parser.add_argument('services',nargs='*')
args=parser.parse_args()
apps=json.loads((ROOT/'docs/services.json').read_text())
core=['system','infra','member','gateway']
selected=args.services or ['full']
if 'full' in selected: selected=[a['profile'] for a in apps]
elif 'core' in selected: selected=list(dict.fromkeys(core+[p for p in selected if p!='core']))
unknown=set(selected)-{a['profile'] for a in apps}
if unknown: parser.error(f'Unknown services: {sorted(unknown)}')
order=core+['bpm','wms','erp','pay','product','promotion','trade','statistics','crm','report',
            'iot','iot-gateway','mes','im','hrm','fms','pms','oa','mp','ai']
selected=[p for p in order if p in selected]
if args.action=='stop':selected.reverse()
env=os.environ.copy()
if not (ROOT/'.env').exists(): raise SystemExit('Run python3 scripts/init-db.py first, or configure .env manually.')
for line in (ROOT/'.env').read_text().splitlines():
    if line and not line.startswith('#') and '=' in line:
        k,v=line.split('=',1);env.setdefault(k,v.strip().strip('"\''))
env['SPRING_CLOUD_NACOS_DISCOVERY_IP']='127.0.0.1'
env['SERVER_ADDRESS']='127.0.0.1'
env['MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED']='true'
data=ROOT/'data/local';data.mkdir(parents=True,exist_ok=True)
java=env.get('AICLOUD_JAVA', '/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin/java')
if not Path(java).exists(): java='java'

def wait_ready(app,pid):
    deadline=time.monotonic()+120
    while time.monotonic()<deadline:
        try:
            if app['profile']=='iot-gateway':
                with socket.create_connection(('127.0.0.1',app['port']),timeout=2):pass
                ready=True
            else:
                with urllib.request.urlopen(f'http://127.0.0.1:{app["port"]}/actuator/health/readiness',timeout=3) as response:
                    ready=json.load(response).get('status')=='UP'
            if ready:
                print(f'{app["profile"]}: UP (port {app["port"]})',flush=True)
                return
        except (OSError,ValueError):pass
        state=subprocess.run(['ps','-p',str(pid),'-o','stat='],capture_output=True,text=True)
        if state.returncode or state.stdout.strip().startswith('Z'):break
        time.sleep(1)
    raise SystemExit(f'{app["profile"]}: failed health check; inspect data/local/{app["profile"]}.log. Existing middleware was preserved.')

for profile in selected:
    app=next(a for a in apps if a['profile']==profile)
    pidfile=data/f'{profile}.pid'
    pid=int(pidfile.read_text()) if pidfile.exists() else None
    if args.action=='stop':
        if pid:
            try:
                command=subprocess.check_output(['ps','-p',str(pid),'-o','command='],text=True)
                if str(ROOT/app['directory']) not in command:
                    raise SystemExit(f'{profile}: PID no longer belongs to this application; refusing to stop it.')
                os.kill(pid,signal.SIGTERM)
                deadline=time.monotonic()+20
                while time.monotonic()<deadline:
                    state=subprocess.run(['ps','-p',str(pid),'-o','stat='],capture_output=True,text=True)
                    if state.returncode or state.stdout.strip().startswith('Z'):break
                    time.sleep(0.25)
            except (ProcessLookupError,subprocess.CalledProcessError): pass
            pidfile.unlink(missing_ok=True)
        print(f'{profile}: stopped')
        continue
    if args.action=='status':
        try:
            if profile == 'iot-gateway':
                with socket.create_connection(('127.0.0.1',app['port']),timeout=3): pass
                print(f'{profile}: UP (MQTT port {app["port"]})')
                continue
            response=urllib.request.urlopen(f'http://127.0.0.1:{app["port"]}/actuator/health',timeout=3)
            health=json.loads(response.read())
            print(f'{profile}: {health.get("status", "unknown")} (port {app["port"]})')
        except Exception: print(f'{profile}: unavailable (port {app["port"]})')
        continue
    if pid:
        state=subprocess.run(['ps','-p',str(pid),'-o','stat=,command='],capture_output=True,text=True)
        if state.returncode==0 and not state.stdout.lstrip().startswith('Z'):
            if str(ROOT/app['directory']) not in state.stdout:
                raise SystemExit(f'{profile}: recorded PID belongs to another application; refusing to use it.')
            print(f'{profile}: already started (PID {pid})',flush=True)
            wait_ready(app,pid)
            continue
        pidfile.unlink()
    jar=ROOT/app['directory']/'target'/f'{app["artifact"]}.jar'
    if not jar.exists(): raise SystemExit(f'Missing {jar}; run Maven package first.')
    try:
        with socket.create_connection(('127.0.0.1',app['port']),timeout=1):pass
    except OSError:pass
    else:raise SystemExit(f'{profile}: port {app["port"]} is already in use by an unrecorded process. Stop the other aicloud runtime before starting this one.')
    with (data/f'{profile}.log').open('a') as log:
        heap=env.get('AICLOUD_JAVA_XMX','768m' if profile in ['bpm','ai','mes'] else '512m')
        process=subprocess.Popen([java,'-Xms64m',f'-Xmx{heap}','-Djava.awt.headless=true','-Dfile.encoding=UTF-8','-jar',str(jar)],
                    cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
    pidfile.write_text(str(process.pid))
    print(f'{profile}: starting (PID {process.pid}, port {app["port"]})',flush=True)
    wait_ready(app,process.pid)
print('Application logs: data/local/*.log. No middleware is installed, removed or stopped.')
