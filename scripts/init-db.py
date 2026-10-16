#!/usr/bin/env python3
"""Initialize only the aicloud schema through an existing LOCAL Docker MySQL."""
import json, os, re, secrets, subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
os.chdir(ROOT)

def run(args, data=None):
    result = subprocess.run(args, input=data, text=True, capture_output=True)
    if result.returncode:
        raise SystemExit(result.stderr.strip() or f'Command failed: {args[0]}')
    return result.stdout

context = json.loads(run(['docker','context','inspect']))[0]
endpoint = context['Endpoints']['docker']['Host']
if not endpoint.startswith(('unix://','npipe://')):
    raise SystemExit('Only a local Docker socket is allowed; remote Docker/database connections are forbidden.')
container = os.environ.get('AICLOUD_MYSQL_CONTAINER','mysql')
if not re.fullmatch(r'[\w.-]+',container): raise SystemExit('Invalid container name')

def mysql(sql):
    return run(['docker','exec','-i',container,'sh','-c',
                'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -N'],sql)

env_path = ROOT/'.env'
env = {}
if env_path.exists():
    for line in env_path.read_text().splitlines():
        if line.strip() and not line.startswith('#') and '=' in line:
            key,value = line.split('=',1); env[key] = value.strip().strip('"\'')
else:
    env = {'MYSQL_USER':'aicloud_backend','MYSQL_PASSWORD':secrets.token_urlsafe(24),
           'REDIS_DATABASE':'12','REDIS_PASSWORD':'','NACOS_USERNAME':'nacos','NACOS_PASSWORD':'nacos',
           'AICLOUD_INTERNAL_SECRET':secrets.token_urlsafe(48),'GATEWAY_PORT':'18080'}
    if mysql("SELECT COUNT(*) FROM mysql.user WHERE user='aicloud_backend';").strip() != '0':
        raise SystemExit('aicloud_backend already exists. Configure its credentials in .env before initialization.')
    fd = os.open(env_path, os.O_WRONLY|os.O_CREAT|os.O_EXCL, 0o600)
    with os.fdopen(fd,'w') as f: f.write('\n'.join(f'{k}={v}' for k,v in env.items())+'\n')

before = set(mysql("SELECT table_name FROM information_schema.tables WHERE table_schema='aicloud';").split())
pending_columns=[]
safe_columns={('infra_file_record','private_file'): "bit(1) NOT NULL DEFAULT b'0' COMMENT '私有附件，仅限签名访问'",
              ('wms_inventory','reserved_quantity'): "decimal(24,8) NOT NULL DEFAULT 0 COMMENT '业务预占库存'",
              ('integration_outbox','aggregate_key'): "varchar(200) NOT NULL DEFAULT ''",
              ('oa_leave_apply','integration_key'): "varchar(160) DEFAULT NULL"}
schema = (ROOT/'sql/mysql/aicloud.sql').read_text()
tables = dict(re.findall(r'CREATE TABLE IF NOT EXISTS `([^`]+)`\s*(\(.*?\)\s*ENGINE.*?;)',schema,re.S))
for name,ddl in tables.items():
    if name not in before: continue
    expected = set(re.findall(r'^\s*`([^`]+)`\s+\w+',ddl,re.M))
    actual = set(mysql(f"SELECT column_name FROM information_schema.columns WHERE table_schema='aicloud' AND table_name='{name}';").split())
    missing=expected-actual
    if missing and all((name,column) in safe_columns for column in missing):
        pending_columns.extend((name,column) for column in missing)
        continue
    if missing:
        raise SystemExit(f'Existing incompatible table {name}; missing columns {sorted(expected-actual)}. No data was changed.')

add_stock_index=False
if 'wms_inventory' in before:
    indexed=mysql("SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema='aicloud' AND table_name='wms_inventory' AND index_name='uk_stock_dimension';").strip()
    if indexed=='0':
        duplicates=mysql("SELECT COUNT(*) FROM (SELECT tenant_id,sku_id,warehouse_id,deleted FROM aicloud.wms_inventory GROUP BY tenant_id,sku_id,warehouse_id,deleted HAVING COUNT(*)>1) d;").strip()
        if duplicates!='0':raise SystemExit('Existing WMS inventory has duplicate dimensions; preserved data. Reconcile duplicates before this migration.')
        add_stock_index=True
for name,column in pending_columns:
    mysql(f"ALTER TABLE aicloud.`{name}` ADD COLUMN `{column}` {safe_columns[(name,column)]};")
if add_stock_index:
    mysql("ALTER TABLE aicloud.wms_inventory ADD UNIQUE KEY uk_stock_dimension(tenant_id,sku_id,warehouse_id,deleted);")

auxiliary=[]
for name in ['04-flowable.sql','05-quartz.sql']:
    block=re.search(r'-- BEGIN '+re.escape(name)+r'(.*?)-- END '+re.escape(name),schema,re.S)
    if block:
        auxiliary.append(block[1])
        schema=schema[:block.start()]+schema[block.end():]
mysql(schema)
for block in auxiliary:
    # Resume interrupted initialization by skipping existing indexes/constraints.
    indexes=set(mysql("SELECT CONCAT(table_name,':',index_name) FROM information_schema.statistics WHERE table_schema='aicloud';").split())
    constraints=set(mysql("SELECT CONCAT(table_name,':',constraint_name) FROM information_schema.table_constraints WHERE table_schema='aicloud';").split())
    sql=re.sub(r'--[^\n]*','',block)
    for statement in sql.split(';'):
        statement=statement.strip()
        if not statement: continue
        idx=re.match(r'create\s+(?:unique\s+)?index\s+(\w+)\s+on\s+(\w+)',statement,re.I)
        fk=re.match(r'alter\s+table\s+(\w+)\s+add\s+constraint\s+(\w+)',statement,re.I)
        if idx and f'{idx[2]}:{idx[1]}' in indexes: continue
        if fk and f'{fk[1]}:{fk[2]}' in constraints: continue
        if re.match(r'insert\s+into\s+ac_ACT_GE_PROPERTY',statement,re.I):
            statement+=' ON DUPLICATE KEY UPDATE NAME_=NAME_'
        mysql('USE aicloud;\n'+statement+';')
user,password = env.get('MYSQL_USER',''),env.get('MYSQL_PASSWORD','')
if not re.fullmatch(r'[\w-]+',user) or not password or any(c in password for c in "'\\\n"):
    raise SystemExit('Use a valid local account/password in .env; schema is initialized but no account was created.')
if user != 'root':
    mysql(f"CREATE USER IF NOT EXISTS '{user}'@'%' IDENTIFIED BY '{password}'; GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,INDEX,REFERENCES ON aicloud.* TO '{user}'@'%';")
after = set(mysql("SELECT table_name FROM information_schema.tables WHERE table_schema='aicloud';").split())
if not before <= after: raise SystemExit('Unexpected missing original tables')
report = {'database':'aicloud','existingTablesPreserved':len(before),'applicationTables':len(tables),
          'totalTablesAfterInit':len(after),'createdTables':len(after-before)}
baseline_path=ROOT/'docs/local-database-baseline.json'
if baseline_path.exists():
    original=set(json.loads(baseline_path.read_text())['originalTables'])
    missing=original-after
    if missing:raise SystemExit(f'Original baseline tables missing: {sorted(missing)}')
    report['baselineOriginalTablesPreserved']=len(original)
    report['baselineMissingTables']=[]
(ROOT/'docs/local-database-verification.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,ensure_ascii=False))
print('Local .env is ready (credentials hidden); existing middleware and data were preserved.')
