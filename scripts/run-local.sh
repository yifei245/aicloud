#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ "${SKIP_BUILD:-false}" != "true" ]]; then
  echo "[run-local] Running mvn clean package -DskipTests ..."
  mvn -T 1C clean package -DskipTests
fi

cd docker

INFRA_SERVICES=()
APP_SERVICES=(gateway auth system member mp openapi infra product trade pay promotion merchant crm erp bpm report)

if [[ "${#INFRA_SERVICES[@]}" -gt 0 ]]; then
  docker compose up -d --build "${INFRA_SERVICES[@]}"
fi

echo "[run-local] Waiting for local MySQL(:3306) to be ready..."
for i in {1..60}; do
  if docker exec mysql mysqladmin ping -uroot -pAa123456 --silent >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" == "60" ]]; then
    echo "[run-local] MySQL is not ready after timeout."
    exit 1
  fi
  sleep 2
done

echo "[run-local] Waiting for local Nacos(:8848) to be ready..."
for i in {1..60}; do
  if curl -fsS http://127.0.0.1:8848/nacos/actuator/health >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" == "60" ]]; then
    echo "[run-local] Local Nacos(:8848) is not ready after timeout."
    exit 1
  fi
  sleep 2
done

echo "[run-local] Waiting for local Redis(:6379) to be ready..."
for i in {1..60}; do
  if python3 - <<'PYREDIS'
import socket, sys
s = socket.socket(); s.settimeout(1)
try:
    s.connect(('127.0.0.1', 6379))
    sys.exit(0)
except Exception:
    sys.exit(1)
finally:
    s.close()
PYREDIS
  then
    break
  fi
  if [[ "$i" == "60" ]]; then
    echo "[run-local] Local Redis(:6379) is not ready after timeout."
    exit 1
  fi
  sleep 2
done

docker compose up -d --build "${APP_SERVICES[@]}"
