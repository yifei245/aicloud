# AICloud 验收清单

## 1. 基础环境
- 打开 `http://127.0.0.1:48080/doc.html`
- 确认 Gateway、Auth、System、Member、MP、OpenAPI、Product、Promotion、Trade、Pay、Merchant、CRM、ERP、BPM、Report 文档均可切换

## 2. 登录
- `POST /auth/login`
- 管理端：`admin / 123456 / tenantId=1 / terminal=ADMIN`
- 终端：`appuser / 123456 / tenantId=1 / terminal=APP`

## 3. System
- `GET /system/menu/tree`
- `GET /system/permission/buttons`
- `GET /system/user/list`
- `GET /system/role/list`

## 4. Member / MP / OpenAPI
- `GET /app/member/profile`
- `GET /app/member/address/list`
- `GET /mp/user/profile`
- `GET /openapi/v1/member/detail`

## 5. Product / Promotion
- `GET /product/category/tree`
- `GET /product/spu/list`
- `GET /app/product/spu/list`
- `GET /promotion/coupon/template/list`
- `GET /app/promotion/coupon/list`
- `GET /app/promotion/coupon/preview`

## 6. Trade / Pay
- `POST /trade/app/order/create`
- `POST /pay/app/order/create`
- `POST /pay/order/notify/success`
- `GET /trade/app/order/list`
- `GET /pay/app/order/list`

## 7. Merchant / CRM
- `GET /merchant/profile/list`
- `GET /merchant/account/list`
- `GET /crm/customer/list`
- `GET /crm/follow/list`
- `POST /crm/follow/create`
- `GET /crm/opportunity/list`

## 8. ERP / BPM / Report
- `GET /erp/inventory/list`
- `POST /erp/inventory/adjust`
- `GET /erp/stock-record/list`
- `GET /bpm/process-definition/list`
- `POST /bpm/process/start`
- `GET /bpm/task/todo`
- `GET /report/sales/overview`
- `GET /report/dashboard/operation`
- `GET /report/crm/overview`

## 9. 验收结论
满足以下条件可视为基线通过：
- 所有模块 Swagger 可打开
- 管理端与终端登录成功
- 各业务域至少一条查询链路和一条写入链路成功
- `trade -> pay` 状态联动成功
- `openapi` 签名调用成功
