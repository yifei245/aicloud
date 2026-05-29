#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

MODULE="${1:-aicloud-auth/aicloud-auth-server}"

echo "[aot-check] Installing reactor dependencies for ${MODULE} ..."
mvn -q -pl "${MODULE}" -am install -DskipTests

echo "[aot-check] Running Spring AOT for ${MODULE} with external registries disabled ..."
mvn -q -pl "${MODULE}" -P aot spring-boot:process-aot -DskipTests \
  -Dspring.cloud.nacos.config.enabled=false \
  -Dspring.cloud.nacos.discovery.enabled=false \
  -Dspring.cloud.nacos.config.import-check.enabled=false
