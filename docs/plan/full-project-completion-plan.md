# AICloud 大而全项目补全计划

> 目标：以 `ruoyi-vue-pro/yudao-cloud` 的模块化结构为主线，吸收 Pig/SpringBlade/JeecgBoot 的网关、OAuth2/SSO、低代码/平台治理思路，先做“大而全可运行后端”，后续可按模块裁剪或独立部署。

## 总体要求核对

| 原始要求 | 当前状态 | 补全标准 |
| --- | --- | --- |
| 项目结构以 ruoyi-vue-pro 为主 | 已按 dependencies/framework/api/module/server/gateway 拆分 | 模块内继续按 controller/service/mapper/entity/model/config 分层，业务模块可独立启停 |
| README、技术说明、账号、MIT | 已有 | 后续每批功能补接口清单和验收脚本 |
| JDK21、Spring Boot 3.5、Spring Cloud 2025 | 已有 | 持续编译验证 |
| MySQL + MyBatis Plus | 已有 | 所有新增业务必须有 SQL、实体、Mapper、种子数据 |
| Docker | 已有 | 每批功能必须可通过 docker compose 运行 |
| Nacos 注册中心 | 已有 | 新模块/新接口通过网关与 Knife4j 聚合可见 |
| 数据库 aicloud 完整脚本 | 已有基础脚本 | 每批补表、索引、演示数据和幂等初始化 |
| 多终端、多用户、SSO | 已有基础认证 | 补 OAuth2 客户端、会话、终端策略、第三方应用授权闭环 |
| 动态菜单、按钮权限 | 已有系统菜单/权限 | 新业务模块补菜单、按钮权限、角色授权 SQL |
| 微服务按功能拆分，大而全 | 已拆分但部分模块薄 | 每个核心模块至少覆盖管理端 CRUD、用户端/开放端必要接口、统计/状态接口 |
| Java 后端，多端接口与网关鉴权 | 已有基础网关 | 补 APP/WEB/MP/OpenAPI 分端接口、签名、限流、审计 |
| Swagger/Knife4j | 已聚合 | 新接口必须能在网关 Knife4j 调试 |

## 阶段计划表

| 阶段 | 优先级 | 模块 | 要补的核心能力 | 验收接口示例 |
| --- | --- | --- | --- | --- |
| P0 | 最高 | Auth/Gateway/System | SSO/OAuth2 客户端、终端策略、权限规则、审计日志、菜单按钮 SQL | `/auth/oauth2/*`, `/system/menu/tree`, `/gateway/audit/*` |
| P0 | 最高 | Infra | 配置中心、文件管理、定时任务、站内信/通知、代码生成元数据 | `/infra/config/*`, `/infra/file/*`, `/infra/job/*`, `/infra/notice/*` |
| P1 | 高 | CRM | 客户、联系人、跟进、商机、合同、回款、客户公海、转移、统计 | `/crm/customer/*`, `/crm/opportunity/*`, `/crm/contract/*` |
| P1 | 高 | ERP | 仓库、库存、锁库、出入库、盘点、供应商、采购、库存预警 | `/erp/inventory/*`, `/erp/stock-record/*`, `/erp/purchase/*` |
| P1 | 高 | Merchant | 商户资料、入驻审核、商户账号、结算账户、费率、门店 | `/merchant/profile/*`, `/merchant/audit/*`, `/merchant/settlement/*` |
| P1 | 高 | Report | 经营看板、销售、支付、会员、CRM、库存、导出任务 | `/report/dashboard/*`, `/report/sales/*`, `/report/export/*` |
| P2 | 中高 | Product | 分类、SPU、SKU、品牌、属性、上下架、库存联动 | `/product/spu/*`, `/product/sku/*`, `/product/brand/*` |
| P2 | 中高 | Promotion | 优惠券、领取、核销、活动、满减、折扣、秒杀基础 | `/promotion/coupon/*`, `/promotion/activity/*` |
| P2 | 中高 | Trade | 购物车、下单、订单、物流、售后、退款申请、评价 | `/trade/order/*`, `/trade/after-sale/*`, `/trade/cart/*` |
| P2 | 中高 | Pay | 支付单、退款单、渠道、回调、对账、转账 | `/pay/order/*`, `/pay/refund/*`, `/pay/channel/*` |
| P3 | 中 | Member | 会员资料、等级、积分、余额、地址、成长值、签到 | `/app/member/*`, `/web/member/*` |
| P3 | 中 | MP | 小程序绑定、素材、菜单、订阅消息、二维码、用户标签 | `/mp/user/*`, `/mp/material/*`, `/mp/menu/*` |
| P3 | 中 | OpenAPI | 应用管理、密钥、签名验签、接口额度、调用日志、Webhook | `/openapi/app/*`, `/openapi/log/*`, `/openapi/webhook/*` |
| P4 | 中 | BPM | 流程定义、实例、任务、审批、抄送、表单、模型导入导出 | `/bpm/process-*`, `/bpm/task/*`, `/bpm/form/*` |
| P4 | 中 | LowCode/AI 扩展 | 代码生成、在线表单、AI 配置、知识库预留 | `/infra/codegen/*`, `/ai/*` |

## 当前执行批次

1. 第一批：CRM、ERP、Merchant、Report 补强为真实后台模块。
2. 第二批：Product、Promotion、Trade、Pay 打通商品-营销-订单-支付-退款闭环。
3. 第三批：Infra、OpenAPI、MP 补平台治理和三方/小程序生态能力。
4. 第四批：Auth/Gateway/System/BPM 深化 SSO、权限、流程、审计与导出。
5. 每批完成后执行：`mvn -q -DskipTests package`、`./scripts/init-db.sh`、`./scripts/smoke-test.sh`。
