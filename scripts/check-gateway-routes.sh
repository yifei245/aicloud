#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ROUTE_FILE="$ROOT_DIR/aicloud-gateway/aicloud-gateway-server/src/main/resources/application.yml"

if grep -Eq 'Path=/app/\*\*|Path=/web/\*\*|Path=.*/app/\*\*|Path=.*/web/\*\*' "$ROUTE_FILE"; then
  echo "ERROR: gateway route must not use broad /app/** or /web/** catch-all predicates." >&2
  echo "Use explicit module routes, for example /app/member/**, /app/product/**, /app/promotion/**." >&2
  exit 1
fi

for path in '/app/product/**' '/app/promotion/**' '/app/member/**' '/web/product/**' '/web/promotion/**' '/web/member/**'; do
  if ! grep -Fq "$path" "$ROUTE_FILE"; then
    echo "ERROR: missing explicit gateway route predicate: $path" >&2
    exit 1
  fi
done

echo "Gateway route predicates are explicit; no broad app/web catch-all route found."
