#!/usr/bin/env python3
"""Build the complete MySQL bundle, including isolated Flowable/Quartz schemas."""
from pathlib import Path
import re,zipfile
ROOT=Path(__file__).resolve().parents[1]
cache=Path.home()/'.m2/repository/org/flowable'
sources=[('flowable-engine-common','org/flowable/common/db/create/flowable.mysql.create.common.sql'),
         ('flowable-engine','org/flowable/db/create/flowable.mysql.create.engine.sql'),
         ('flowable-engine','org/flowable/db/create/flowable.mysql.create.history.sql')]
parts=['-- Flowable 8.0.0, Apache-2.0. Source SQL from published Maven artifacts.\nUSE aicloud;']
raw=[]
for artifact,resource in sources:
    jar=cache/artifact/'8.0.0'/f'{artifact}-8.0.0.jar'
    with zipfile.ZipFile(jar) as z: sql=z.read(resource).decode()
    raw.append((resource,sql))
tokens=set(re.findall(r'(?:create\s+table|alter\s+table|references|create\s+(?:unique\s+)?index|add\s+constraint)\s+([\w]+)','\n'.join(sql for _,sql in raw),re.I))
for resource,sql in raw:
    sql=re.sub(r'\b('+'|'.join(sorted(tokens,key=len,reverse=True))+r')\b',lambda m:'ac_'+m[0],sql)
    sql=re.sub(r'create table (\w+)',r'CREATE TABLE IF NOT EXISTS `\1`',sql,flags=re.I)
    parts.append(f'-- Original resource: {resource}\n'+sql)
(ROOT/'sql/mysql/04-flowable.sql').write_text('\n'.join(parts))
quartz=Path.home()/'.m2/repository/org/quartz-scheduler/quartz/2.5.0/quartz-2.5.0.jar'
with zipfile.ZipFile(quartz) as z:
    sql=z.read('org/quartz/impl/jdbcjobstore/tables_mysql_innodb.sql').decode()
# Keep the published InnoDB schema, omit destructive DROP statements, isolate names.
sql=re.sub(r'DROP TABLE IF EXISTS [^;]+;','',sql,flags=re.I)
sql=sql.replace('QRTZ_', 'ac_QRTZ_')
sql=re.sub(r'CREATE TABLE\s+(\w+)',r'CREATE TABLE IF NOT EXISTS `\1`',sql,flags=re.I)
(ROOT/'sql/mysql/05-quartz.sql').write_text('-- Quartz 2.5.0, Apache-2.0; published MySQL InnoDB schema.\nUSE aicloud;\n'+sql)
out=['-- aicloud complete MySQL 8.4 initialization; do not use on incompatible tables.\n-- Application source: MIT; Flowable schema: Apache-2.0 (see LICENSES).']
for name in ['01-core.sql','02-business.sql','03-seed.sql','04-flowable.sql','05-quartz.sql','06-iot-history.sql','07-permission-menu.sql','08-business-config.sql','09-fms-subjects.sql','10-integration.sql','upgrade/20260926-fms-report-template.sql','upgrade/20261001-private-file.sql']:
    out.append(f'-- BEGIN {name}\n'+(ROOT/'sql/mysql'/name).read_text()+f'\n-- END {name}')
(ROOT/'sql/mysql/aicloud.sql').write_text('\n\n'.join(out)+'\n')
print('Generated sql/mysql/aicloud.sql (core, all business modules, seed data, isolated Flowable tables).')
