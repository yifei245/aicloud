#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:48080}"
APP_KEY="${APP_KEY:-demo_app_key}"
APP_SECRET="${APP_SECRET:-demo_app_secret}"
PING_PATH="${PING_PATH:-/openapi/v1/ping}"
SUMMARY_PATH="${SUMMARY_PATH:-/openapi/v1/member/summary}"
SUMMARY_QUERY="${SUMMARY_QUERY:-memberId=3}"
RATE_REQUESTS="${RATE_REQUESTS:-160}"
SLEEP_BETWEEN="${SLEEP_BETWEEN:-0.02}"

if ! command -v openssl >/dev/null 2>&1; then
  echo "openssl 未安装，无法生成 HMAC 签名"
  exit 1
fi

sign() {
  local method="$1"
  local path="$2"
  local query="$3"
  local ts="$4"
  local nonce="$5"
  local payload="${method}"$'\n'"${path}"$'\n'"${query}"$'\n'"${ts}"$'\n'"${nonce}"
  printf "%s" "${payload}" | openssl dgst -sha256 -hmac "${APP_SECRET}" | awk '{print $2}'
}

request_once() {
  local method="$1"
  local path="$2"
  local query="$3"
  local ts="$4"
  local nonce="$5"
  local signature
  signature="$(sign "${method}" "${path}" "${query}" "${ts}" "${nonce}")"

  local url="${BASE_URL}${path}"
  if [[ -n "${query}" ]]; then
    url="${url}?${query}"
  fi

  curl -sS -D - -o /tmp/aicloud_openapi_body.$$ \
    -X "${method}" "${url}" \
    -H "X-App-Key: ${APP_KEY}" \
    -H "X-Timestamp: ${ts}" \
    -H "X-Nonce: ${nonce}" \
    -H "X-Sign: ${signature}"
}

header_value() {
  local key="$1"
  awk -F': ' -v k="${key}" 'tolower($1)==tolower(k){gsub("\r","",$2); print $2}' || true
}

extract_status() {
  awk 'NR==1{print $2}'
}

echo "== 1) 单次连通性测试 =="
ts="$(date +%s)"
nonce="n$(date +%s%N)"
headers="$(request_once "GET" "${PING_PATH}" "" "${ts}" "${nonce}")"
code="$(printf "%s\n" "${headers}" | extract_status)"
limit="$(printf "%s\n" "${headers}" | header_value "X-RateLimit-Limit")"
remaining="$(printf "%s\n" "${headers}" | header_value "X-RateLimit-Remaining")"
reset="$(printf "%s\n" "${headers}" | header_value "X-RateLimit-Reset")"
echo "status=${code} limit=${limit:-N/A} remaining=${remaining:-N/A} reset=${reset:-N/A}"
cat /tmp/aicloud_openapi_body.$$ | sed -n '1,2p'

echo
echo "== 2) 防重放测试（同 nonce 重放） =="
ts="$(date +%s)"
replay_nonce="replay$(date +%s%N)"
headers_1="$(request_once "GET" "${SUMMARY_PATH}" "${SUMMARY_QUERY}" "${ts}" "${replay_nonce}")"
code_1="$(printf "%s\n" "${headers_1}" | extract_status)"
headers_2="$(request_once "GET" "${SUMMARY_PATH}" "${SUMMARY_QUERY}" "${ts}" "${replay_nonce}")"
code_2="$(printf "%s\n" "${headers_2}" | extract_status)"
echo "first_status=${code_1} second_status=${code_2} (预期 second 非 200)"
cat /tmp/aicloud_openapi_body.$$ | sed -n '1,2p'

echo
echo "== 3) 限流测试（${RATE_REQUESTS} 次） =="
ok_count=0
too_many_count=0
other_count=0
for i in $(seq 1 "${RATE_REQUESTS}"); do
  ts="$(date +%s)"
  nonce="rate${i}$(date +%s%N)"
  headers="$(request_once "GET" "${PING_PATH}" "" "${ts}" "${nonce}")"
  code="$(printf "%s\n" "${headers}" | extract_status)"
  if [[ "${code}" == "200" ]]; then
    ok_count=$((ok_count + 1))
  elif [[ "${code}" == "429" ]]; then
    too_many_count=$((too_many_count + 1))
  else
    other_count=$((other_count + 1))
  fi
  sleep "${SLEEP_BETWEEN}"
done
echo "200=${ok_count} 429=${too_many_count} other=${other_count}"
echo "说明：若 429>0，表示分布式限流已生效。"

rm -f /tmp/aicloud_openapi_body.$$
