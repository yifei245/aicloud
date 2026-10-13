#!/usr/bin/env python3
"""Verify every actual Knife4j group through the local aicloud gateway."""
import json,sys,urllib.error,urllib.parse,urllib.request
from datetime import datetime,timezone
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
BASE='http://127.0.0.1:18080'
METHODS={'get','post','put','delete','patch','head','options'}

def read(path):
    url=urllib.parse.urljoin(BASE,path)
    parsed=urllib.parse.urlsplit(url)
    if (parsed.scheme,parsed.hostname,parsed.port)!=('http','127.0.0.1',18080):
        raise ValueError('Documentation URL must point to the local gateway')
    with urllib.request.urlopen(url,timeout=45) as response:
        return json.load(response)

apps=json.loads((ROOT/'docs/services.json').read_text())
expected={a['name'] for a in apps if a['profile'] not in {'gateway','iot-gateway'}}
report={'checkedAt':datetime.now(timezone.utc).isoformat(),'gateway':BASE,'groups':[]}
try:
    groups=read('/v3/api-docs/swagger-config').get('urls',[])
    names=[g.get('name') for g in groups]
    report['missingGroups']=sorted(expected-set(names))
    report['unexpectedGroups']=sorted(set(names)-expected)
    report['duplicateGroups']=sorted({n for n in names if names.count(n)>1})
    for group in groups:
        item={'name':group.get('name'),'url':group.get('url'),'passed':False}
        try:
            doc=read(group['url'])
            if not str(doc.get('openapi','')).startswith('3.'):
                raise ValueError(doc.get('msg','Response is not an OpenAPI 3 document'))
            paths=doc.get('paths',{})
            item['paths']=len(paths)
            item['operations']=sum(method in METHODS for methods in paths.values() for method in methods)
            item['adminOperations']=sum(method in METHODS for p,methods in paths.items()
                                        if p.startswith('/admin-api/') for method in methods)
            item['appOperations']=sum(method in METHODS for p,methods in paths.items()
                                      if p.startswith('/app-api/') for method in methods)
            item['openOperations']=sum(method in METHODS for p,methods in paths.items()
                                       if p.startswith('/open-api/') for method in methods)
            if not item['operations']:raise ValueError('OpenAPI document has no operations')
            item['passed']=True
        except (OSError,ValueError,KeyError) as error:
            item['error']=str(error)
        report['groups'].append(item)
        print(('PASS ' if item['passed'] else 'FAIL ')+str(item['name'])+': '+
              (f'{item["paths"]} paths / {item["operations"]} operations' if item['passed'] else item['error']),flush=True)
    report['passed']=bool(groups) and all(g['passed'] for g in report['groups']) and not any(
        report[k] for k in ['missingGroups','unexpectedGroups','duplicateGroups'])
except (OSError,ValueError) as error:
    report['passed']=False
    report['error']=str(error)
(ROOT/'docs/knife4j-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(('PASS' if report['passed'] else 'FAIL')+f' Knife4j: {len(report["groups"])} / {len(expected)} groups')
sys.exit(0 if report['passed'] else 1)
