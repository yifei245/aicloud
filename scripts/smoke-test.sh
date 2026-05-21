#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:48080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-123456}"
APP_USERNAME="${APP_USERNAME:-appuser}"
APP_PASSWORD="${APP_PASSWORD:-123456}"
OPENAPI_APP_KEY="${OPENAPI_APP_KEY:-demo_app_key}"
OPENAPI_APP_SECRET="${OPENAPI_APP_SECRET:-demo_app_secret}"

pass_count=0
fail_count=0

print_step() {
  printf '\n== %s ==\n' "$1"
}

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local token="${4:-}"
  local tmp
  tmp="$(mktemp)"

  local args=(-sS -o "$tmp" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "Content-Type: application/json")
  if [[ -n "$token" ]]; then
    args+=(-H "Authorization: Bearer $token")
  fi
  if [[ -n "$body" ]]; then
    args+=(-d "$body")
  fi

  local code
  code="$(curl "${args[@]}")"
  printf "%s %s -> %s\n" "$method" "$path" "$code"
  if [[ "$code" =~ ^2 ]]; then
    pass_count=$((pass_count + 1))
  else
    fail_count=$((fail_count + 1))
    sed -n '1,8p' "$tmp"
  fi
  rm -f "$tmp"
}

openapi_request() {
  local method="$1"
  local path_with_query="$2"
  local path="${path_with_query%%\?*}"
  local query=""
  if [[ "$path_with_query" == *\?* ]]; then
    query="${path_with_query#*\?}"
  fi
  local timestamp nonce sign tmp code
  timestamp="$(date +%s)"
  nonce="$(date +%s%N)"
  sign="$(python3 - "$method" "$path" "$query" "$timestamp" "$nonce" "$OPENAPI_APP_SECRET" <<'PY'
import hashlib, hmac, sys
method, path, query, timestamp, nonce, secret = sys.argv[1:]
if query:
    pairs = []
    for item in query.split('&'):
        key = item.split('=', 1)[0]
        if key.lower() not in ('sign', 'x-sign'):
            pairs.append(item)
    query = '&'.join(sorted(pairs))
text = '\n'.join([method, path, query, timestamp, nonce])
print(hmac.new(secret.encode(), text.encode(), hashlib.sha256).hexdigest())
PY
)"
  tmp="$(mktemp)"
  code="$(curl -sS -o "$tmp" -w "%{http_code}" -X "$method" "$BASE_URL$path_with_query" \
    -H "Content-Type: application/json" \
    -H "X-App-Key: $OPENAPI_APP_KEY" \
    -H "X-Timestamp: $timestamp" \
    -H "X-Nonce: $nonce" \
    -H "X-Sign: $sign")"
  printf "%s %s -> %s\n" "$method" "$path_with_query" "$code"
  if [[ "$code" =~ ^2 ]]; then
    pass_count=$((pass_count + 1))
  else
    fail_count=$((fail_count + 1))
    sed -n '1,8p' "$tmp"
  fi
  rm -f "$tmp"
}

login() {
  local username="$1"
  local password="$2"
  local terminal="$3"
  local tmp
  tmp="$(mktemp)"

  local code
  code="$(curl -sS -o "$tmp" -w "%{http_code}" \
    -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\",\"terminal\":\"$terminal\"}")"
  if [[ ! "$code" =~ ^2 ]]; then
    printf "login %s/%s -> %s\n" "$username" "$terminal" "$code" >&2
    sed -n '1,8p' "$tmp" >&2
    rm -f "$tmp"
    return 1
  fi

  python3 - "$tmp" <<'PY'
import json, sys
payload = json.load(open(sys.argv[1]))
data = payload.get("data") or {}
print(data.get("accessToken") or data.get("token") or "")
PY
  rm -f "$tmp"
}

print_step "登录"
ADMIN_TOKEN="$(login "$ADMIN_USERNAME" "$ADMIN_PASSWORD" "ADMIN")"
APP_TOKEN="$(login "$APP_USERNAME" "$APP_PASSWORD" "APP")"
if [[ -z "$ADMIN_TOKEN" || -z "$APP_TOKEN" ]]; then
  echo "登录未拿到 token，请检查账号密码或 /auth/login 返回结构。" >&2
  exit 1
fi
printf "admin token: %s...\n" "${ADMIN_TOKEN:0:16}"
printf "app token: %s...\n" "${APP_TOKEN:0:16}"

print_step "Knife4j / OpenAPI"
request GET /doc.html ""
request GET /v3/api-docs/swagger-config ""
request GET /v3/api-docs/infra ""
request GET /v3/api-docs/bpm ""
request GET /v3/api-docs/openapi ""
request GET /v3/api-docs/mp ""

print_step "管理端核心接口"
request GET /system/menu/tree "" "$ADMIN_TOKEN"
request GET /system/permission/buttons "" "$ADMIN_TOKEN"
request GET /infra/config/status "" "$ADMIN_TOKEN"
request GET /infra/config/list "" "$ADMIN_TOKEN"
request GET /infra/file/list "" "$ADMIN_TOKEN"
request GET /infra/job/list "" "$ADMIN_TOKEN"
request GET /infra/notice/list "" "$ADMIN_TOKEN"
request GET /bpm/process-definition/list "" "$ADMIN_TOKEN"
request GET /bpm/task/todo?assignee=admin "" "$ADMIN_TOKEN"
request GET /report/dashboard/operation "" "$ADMIN_TOKEN"
request GET /report/sales/overview "" "$ADMIN_TOKEN"
request GET /report/pay/overview "" "$ADMIN_TOKEN"
request GET /report/member/overview "" "$ADMIN_TOKEN"
request GET /report/crm/overview "" "$ADMIN_TOKEN"
request GET /report/merchant/overview "" "$ADMIN_TOKEN"
request GET /report/inventory/overview "" "$ADMIN_TOKEN"
request GET /report/dashboard/funnel "" "$ADMIN_TOKEN"

print_step "用户端与业务接口"
request GET /app/member/profile "" "$APP_TOKEN"
request GET /app/product/spu/list "" "$APP_TOKEN"
request GET /product/category/tree "" "$ADMIN_TOKEN"
request GET /product/spu/list?keyword=AI "" "$ADMIN_TOKEN"
request GET /promotion/coupon/template/list "" "$APP_TOKEN"
request GET /promotion/coupon/template/1 "" "$ADMIN_TOKEN"
request GET /promotion/coupon/user/list "" "$ADMIN_TOKEN"
request GET /trade/order/list "" "$APP_TOKEN"
request GET /trade/app/cart/list "" "$APP_TOKEN"
request GET /trade/delivery/list "" "$ADMIN_TOKEN"
request GET /trade/after-sale/list "" "$ADMIN_TOKEN"
request GET /pay/order/list "" "$ADMIN_TOKEN"
request GET /pay/channel/list "" "$ADMIN_TOKEN"
request GET /pay/refund/list "" "$ADMIN_TOKEN"
request GET /merchant/overview "" "$ADMIN_TOKEN"
request GET /merchant/profile/list "" "$ADMIN_TOKEN"
request GET /merchant/account/list "" "$ADMIN_TOKEN"
request GET /crm/overview "" "$ADMIN_TOKEN"
request GET /crm/customer/list "" "$ADMIN_TOKEN"
request GET /crm/opportunity/list "" "$ADMIN_TOKEN"
request GET /erp/inventory/list "" "$ADMIN_TOKEN"
request GET /erp/inventory/summary "" "$ADMIN_TOKEN"
request GET /erp/inventory/low-stock "" "$ADMIN_TOKEN"
request GET /erp/stock-record/list "" "$ADMIN_TOKEN"
request GET /erp/stock-check/list "" "$ADMIN_TOKEN"

print_step "开放平台与小程序"
request GET /openapi/app/list "" "$ADMIN_TOKEN"
request GET /openapi/log/list "" "$ADMIN_TOKEN"
request GET /openapi/webhook/list "" "$ADMIN_TOKEN"
openapi_request GET /openapi/v1/ping
openapi_request GET /openapi/v1/member/summary?memberId=3
request GET /mp/menu/list "" "$ADMIN_TOKEN"
request GET /mp/material/list "" "$ADMIN_TOKEN"
request GET /mp/message/template/list "" "$ADMIN_TOKEN"
request GET /mp/message/log/list "" "$ADMIN_TOKEN"

printf '\n烟测完成：通过 %d，失败 %d\n' "$pass_count" "$fail_count"
if [[ "$fail_count" -gt 0 ]]; then
  exit 1
fi
