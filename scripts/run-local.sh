#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ "${SKIP_BUILD:-false}" != "true" ]]; then
  echo "[run-local] Running mvn clean package -DskipTests ..."
  mvn -T 1C clean package -DskipTests
fi

cd docker

INFRA_SERVICES=(mysql nacos redis)
APP_SERVICES=(gateway auth system member mp openapi infra product trade pay promotion merchant crm erp bpm report)

docker compose up -d --build "${INFRA_SERVICES[@]}"

echo "[run-local] Waiting for MySQL to be ready..."
for i in {1..60}; do
  if docker exec aicloud-mysql mysqladmin ping -uroot -proot --silent >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" == "60" ]]; then
    echo "[run-local] MySQL is not ready after timeout."
    exit 1
  fi
  sleep 2
done

echo "[run-local] Waiting for Nacos to be ready..."
for i in {1..60}; do
  if docker exec aicloud-nacos sh -lc "wget -qO- http://127.0.0.1:8848/nacos/actuator/health || curl -fsS http://127.0.0.1:8848/nacos/actuator/health" >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" == "60" ]]; then
    echo "[run-local] Nacos is not ready after timeout."
    exit 1
  fi
  sleep 2
done

docker compose up -d --build "${APP_SERVICES[@]}"
