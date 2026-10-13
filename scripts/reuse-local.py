#!/usr/bin/env python3
"""Read credentials from existing LOCAL Docker metadata without printing them."""
import json,os,re,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
context=json.loads(subprocess.check_output(['docker','context','inspect'],text=True))[0]
if not context['Endpoints']['docker']['Host'].startswith(('unix://','npipe://')):
    raise SystemExit('Remote Docker is forbidden.')
config=json.loads(subprocess.check_output(['docker','inspect','redis'],text=True))[0]['Config']
cmd=config.get('Cmd') or []
password=cmd[cmd.index('--requirepass')+1] if '--requirepass' in cmd else ''
if any(c in password for c in "'\n\r"):
    raise SystemExit('Configure the existing Redis password manually in .env.')
p=ROOT/'.env'
s=p.read_text()
if re.search(r'^REDIS_PASSWORD=\s*$',s,re.M) and password:
    p.write_text(re.sub(r'^REDIS_PASSWORD=\s*$',lambda _:"REDIS_PASSWORD='"+password+"'",s,flags=re.M))
    os.chmod(p,0o600)
    print('Reused existing Redis credential in local .env (hidden). No container/configuration was changed.')
else:
    print('Kept local Redis configuration.')
