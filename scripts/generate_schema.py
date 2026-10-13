#!/usr/bin/env python3
"""Derive aicloud business schema from the MIT Java model; no restricted SQL inputs.
Regenerate before release, review SQL and validate against MySQL. Core schema is hand maintained.
"""
from pathlib import Path
import re,json
ROOT=Path(__file__).resolve().parents[1]
def snake(n): return re.sub(r'(?<!^)(?=[A-Z])','_',n).lower()
classes={}
for p in ROOT.glob('aicloud-*/**/src/main/java/**/*.java'):
 s=p.read_text();m=re.search(r'public (?:abstract )?class (\w+)(?:\s+extends\s+(\w+))?',s)
 if m:classes[m[1]]=(p,s,m[2])
def fields(name,seen=None):
 if name not in classes:return {}
 p,s,parent=classes[name]; result=fields(parent) if parent else {}
 # Fields must be at outer-class indentation, never static/nested.
 last=0
 for m in re.finditer(r'^    (?:private|protected) (?!static\b)([\w<>?, .\[\]]+)\s+(\w+)\s*(?:=[^;]*)?;',s,re.M):
  typ,n=m[1].strip(),m[2];before=s[last:m.start()];last=m.end()
  # Only annotations/comment since the previous outer field, excluding previous methods.
  annotation=before[before.rfind('/**'):] if '/**' in before else before
  if re.search(r'@TableField\([^)]*exist\s*=\s*false',annotation):continue
  col=re.search(r'@TableField\(\s*(?:value\s*=\s*)?"([^"]+)"',annotation)
  result[col[1] if col else snake(n)]=(typ,annotation)
 return result
core=(ROOT/'sql/mysql/01-core.sql').read_text() if (ROOT/'sql/mysql/01-core.sql').exists() else ''
existing=set(re.findall(r'CREATE TABLE(?: IF NOT EXISTS)? `([^`]+)`',core))
out=['-- aicloud business schema, independently derived from public MIT entity mappings.','USE aicloud;','SET NAMES utf8mb4;']
inventory=[]
for name,(p,s,_) in sorted(classes.items()):
 m=re.search(r'@TableName\(\s*(?:value\s*=\s*)?"([^"]+)"',s)
 if not m:continue
 table=m[1]
 fs=fields(name)
 if "tenant_id" not in fs and "@TenantIgnore" not in s: fs["tenant_id"]=("Long", "")
 inventory.append({'table':table,'entity':str(p.relative_to(ROOT)),'columns':list(fs)})
 if table in existing:continue
 cols=[]
 for n,(t,a) in fs.items():
  if n=='id':decl='bigint NOT NULL AUTO_INCREMENT' if t=='Long' else ('varchar(128) NOT NULL' if t=='String' else 'bigint NOT NULL AUTO_INCREMENT')
  elif n in ['create_time','update_time']:decl='datetime NOT NULL DEFAULT CURRENT_TIMESTAMP'+(' ON UPDATE CURRENT_TIMESTAMP' if n=='update_time' else '')
  elif n=='deleted':decl="bit(1) NOT NULL DEFAULT b'0'"
  elif n=='tenant_id':decl='bigint NOT NULL DEFAULT 0'
  elif n in ['creator','updater']:decl="varchar(64) DEFAULT ''"
  elif t in ['Long','long']:decl='bigint DEFAULT 0'
  elif t in ['Integer','int','Short','short','Byte','byte']:decl='int DEFAULT 0'
  elif t in ['Boolean','boolean']:decl="bit(1) DEFAULT b'0'"
  elif t=='BigDecimal':decl='decimal(24,8) DEFAULT 0'
  elif t in ['Double','double','Float','float']:decl='double DEFAULT 0'
  elif t in ['LocalDateTime','Date','Instant']:decl='datetime DEFAULT NULL'
  elif t=='LocalDate':decl='date DEFAULT NULL'
  elif t=='LocalTime':decl='time DEFAULT NULL'
  elif t=='byte[]':decl='longblob'
  elif t=='String':decl='varchar(255) DEFAULT NULL' if n.endswith(('_no','_code','_name')) or n in ['name','code','mobile','email','password','username','title','status','path'] else 'text'
  else:decl='json DEFAULT NULL' if 'JacksonTypeHandler' in a else 'text'
  cols.append('  `'+n+'` '+decl)
 if 'id' in fs:cols.append('  PRIMARY KEY (`id`)')
 for n in fs:
  if n.endswith('_id') and fs[n][0] in ['Long','Integer'] and n!='id':cols.append(f'  KEY `idx_{n}` (`{n}`)')
 out.append('CREATE TABLE IF NOT EXISTS `'+table+'` (\n'+',\n'.join(cols)+'\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n')
(ROOT/'sql/mysql/02-business.sql').write_text('\n'.join(out))
(ROOT/'docs/schema-inventory.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2)+'\n')
print(f'{len(inventory)} entity tables; {len(existing)} core schema tables; {len(out)-3} business tables')
