#!/usr/bin/env bash
set -euo pipefail

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

for file in sql/migrations/*.sql; do
  [ -e "$file" ] || continue
  echo "Applying $file"
  run_mysql_file "$file"
done
