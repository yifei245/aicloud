#!/usr/bin/env python3
"""Run the complete SQL in an empty temporary schema on the existing LOCAL MySQL.

Only this newly-created temporary schema is removed after verification. The
aicloud database and all middleware containers/data volumes are preserved.
"""
import json,re,subprocess,time
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def mysql(sql):
    result=subprocess.run(['docker','exec','-i','mysql','sh','-c',
        'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -N'],
        input=sql,text=True,capture_output=True)
    if result.returncode:raise RuntimeError(result.stderr)
    return result.stdout
context=json.loads(subprocess.check_output(['docker','context','inspect'],text=True))[0]
if not context['Endpoints']['docker']['Host'].startswith(('unix://','npipe://')):
    raise SystemExit('Only a local Docker/MySQL endpoint is permitted')
scratch='aicloud_schema_check_'+str(time.time_ns())
assert re.fullmatch(r'aicloud_schema_check_\d+',scratch)
original=mysql("SELECT table_name FROM information_schema.tables WHERE table_schema='aicloud';")
assert mysql(f"SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='{scratch}';").strip()=='0'
sql=(ROOT/'sql/mysql/aicloud.sql').read_text()
sql=sql.replace('CREATE DATABASE IF NOT EXISTS aicloud ',f'CREATE DATABASE IF NOT EXISTS {scratch} ')
sql=sql.replace('USE aicloud;',f'USE {scratch};')
try:
    mysql(sql)
    count=int(mysql(f"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='{scratch}';").strip())
    expected=len(set(re.findall(r'CREATE TABLE IF NOT EXISTS `([^`]+)`',sql)))
    assert count==expected,(count,expected)
    assert mysql(f"USE {scratch}; SELECT COUNT(*) FROM system_users WHERE username IN ('admin','viewer');").strip()=='2'
    assert mysql(f"USE {scratch}; SELECT COUNT(*) FROM member_user WHERE mobile='13800000000';").strip()=='1'
    assert mysql(f"USE {scratch}; SELECT COUNT(*) FROM fms_subject_template WHERE deleted=0;").strip()=='113'
    assert mysql(f"USE {scratch}; SELECT COUNT(*) FROM fms_report_template WHERE deleted=0;").strip()=='135'
    assert mysql(f"SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='{scratch}' AND table_name='infra_file_record' AND column_name='private_file';").strip()=='1'
    assert mysql(f"USE {scratch}; SELECT COUNT(*) FROM mes_md_auto_code_rule WHERE tenant_id=1 AND code IN ('WM_BATCH_CODE','PRO_TASK_CODE') AND deleted=0;").strip()=='2'
    assert mysql(f"USE {scratch}; SELECT COUNT(*) FROM mes_md_auto_code_part p JOIN mes_md_auto_code_rule r ON r.id=p.rule_id AND r.tenant_id=p.tenant_id WHERE r.tenant_id=1 AND r.code IN ('WM_BATCH_CODE','PRO_TASK_CODE') AND r.deleted=0 AND p.deleted=0;").strip()=='6'
    assert mysql(f"SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='{scratch}' AND ((table_name='wms_inventory' AND column_name='putaway_quantity') OR (table_name='wms_integration_movement' AND column_name IN ('position_id','allocation_only')));").strip()=='3'
    assert original==mysql("SELECT table_name FROM information_schema.tables WHERE table_schema='aicloud';")
    report={'freshSchemaVerified':True,'applicationTables':count,'demoAccountsVerified':3,
            'financeSubjectsVerified':113,'financeReportDefinitionsVerified':135,'privateFileColumnVerified':True,
            'mesIdentifierRulesVerified':2,'wmsPositionColumnsVerified':3,
            'existingAicloudSchemaPreserved':True,'middlewareInstalledOrRemoved':False}
    (ROOT/'docs/schema-execution-verification.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report))
finally:
    mysql(f'DROP DATABASE IF EXISTS {scratch};')
