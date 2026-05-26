#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

run_mysql_file() {
  local file="$1"
  if command -v mysql >/dev/null 2>&1; then
    mysql --default-character-set=utf8mb4 -h127.0.0.1 -P3306 -uroot -pAa123456 < "$file"
  else
    docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -pAa123456 < "$file"
  fi
}

run_mysql_file "sql/aicloud-00-schema.sql"
run_mysql_file "sql/aicloud-01-system.sql"
run_mysql_file "sql/aicloud-02-auth.sql"
run_mysql_file "sql/aicloud-03-business.sql"
run_mysql_file "sql/aicloud-99-demo.sql"
if [ -d "sql/migrations" ]; then
  for file in sql/migrations/*.sql; do
    [ -e "$file" ] || continue
    run_mysql_file "$file"
  done
fi
