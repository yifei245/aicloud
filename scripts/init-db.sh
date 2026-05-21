#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

run_mysql_file() {
  local file="$1"
  if command -v mysql >/dev/null 2>&1; then
    mysql -h127.0.0.1 -P13306 -uroot -proot < "$file"
  else
    docker exec -i aicloud-mysql mysql -uroot -proot < "$file"
  fi
}

run_mysql_file "sql/aicloud-00-schema.sql"
run_mysql_file "sql/aicloud-01-system.sql"
run_mysql_file "sql/aicloud-02-auth.sql"
run_mysql_file "sql/aicloud-03-business.sql"
run_mysql_file "sql/aicloud-99-demo.sql"
