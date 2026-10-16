#!/usr/bin/env python3
"""Initialize business workflows through real local login; preserve existing models."""
import importlib.util, json, os, sys, urllib.parse
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('aicloud_http', ROOT / 'scripts/smoke-test.py')
http = importlib.util.module_from_spec(spec)
spec.loader.exec_module(http)

def initialize():
    login = http.call('/admin-api/system/auth/login', 'POST', {
        'username': os.environ.get('AICLOUD_INIT_USERNAME', 'admin'),
        'password': os.environ.get('AICLOUD_INIT_PASSWORD', 'Aicloud@123')})
    if login.get('code') != 0:
        raise RuntimeError('初始化登录失败；请设置 AICLOUD_INIT_USERNAME/AICLOUD_INIT_PASSWORD')
    token = login['data']['accessToken']

    def request(path, method='GET', data=None):
        result = http.call('/admin-api/' + path, method, data, token=token)
        if result.get('code') != 0:
            raise RuntimeError(path.split('?')[0] + ': ' + result.get('msg', '接口不可用'))
        return result.get('data')

    identity = request('system/auth/get-permission-info')['user']
    models = {model['key']: model for model in request('bpm/model/list')}
    outcomes = []
    resources = sorted((ROOT / 'aicloud-module-oa/aicloud-module-oa-server/src/main/resources/bpm').glob('oa_*.json'))
    resources += sorted((ROOT / 'aicloud-module-hrm/aicloud-module-hrm-server/src/main/resources/bpm').glob('*.json'))
    resources += sorted((ROOT / 'aicloud-module-crm/aicloud-module-crm-server/src/main/resources/bpm').glob('*.json'))
    for resource in resources:
        template = json.loads(resource.read_text())
        template['managerUserIds'] = [identity['id']]
        model = models.get(template['key'])
        model_id = model['id'] if model else request('bpm/model/create', 'POST', template)
        if model is None or model.get('processDefinition') is None:
            request('bpm/model/deploy?id=' + urllib.parse.quote(model_id), 'POST')
            action = 'created and deployed' if model is None else 'deployed existing draft'
        else:
            action = 'preserved existing deployment'
        definition = request('bpm/process-definition/get?key=' + template['key'])
        if not definition or definition['key'] != template['key'] or definition.get('suspensionState') == 2:
            raise RuntimeError(template['key'] + ': 流程未生效，请检查挂起状态')
        outcomes.append({'key': template['key'], 'action': action, 'passed': True})
        print(template['name'] + ': ' + action, flush=True)
    if len(outcomes) != 16:
        raise RuntimeError('业务流程资源数量不完整')
    (ROOT / 'docs/business-initialization.json').write_text(json.dumps(outcomes, ensure_ascii=False, indent=2) + '\n')
    return outcomes

if __name__ == '__main__':
    try:
        initialize()
    except Exception as error:
        print('FAIL ' + str(error), file=sys.stderr)
        sys.exit(1)
