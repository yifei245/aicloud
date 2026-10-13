# 缺口清单（2026-10-13 核查）

依据：README、`docs/01~04`、各验证 JSON、源码与配置的静态核查。未启动服务，未连数据库。

## P0：影响业务正确性

### 1. 30 个定时任务全部不会执行

- 所有 24 个服务的 `application-local.yaml` 都是 `xxl.job.enabled: false`，Compose 和脚本里也没有 XXL-Job Admin。
- 业务 Job 全部只用 `@XxlJob` 注册。目前只有 OA 写了本地兜底 `OaLocalScheduler`，框架里的 Redis MQ 重投/清理用 `@Scheduled`，其余都不跑。
- 受影响的业务：

| 模块 | Job | 不跑的后果 |
|---|---|---|
| trade | TradeOrderAutoCancelJob / AutoReceiveJob / AutoCommentJob | 未支付订单不取消、库存不回滚；不自动收货、不自动好评 |
| trade | BrokerageRecordUnfreezeJob | 分销佣金一直冻结 |
| promotion | CouponExpireJob / CombinationRecordExpireJob | 优惠券不过期，拼团不超时 |
| pay | PayOrderExpireJob / PayOrderSyncJob / PayRefundSyncJob / PayTransferSyncJob | 支付单不过期，丢失回调时状态不补偿 |
| pay | PayNotifyJob | 首次通知在提交后立即发出；失败后不重试 |
| statistics | TradeStatisticsJob / ProductStatisticsJob | 统计报表没有数据 |
| system / infra | TokenCleanJob / AccessLogCleanJob / ErrorLogCleanJob | 令牌表、日志表无限增长 |
| crm | CrmCustomerAutoPutPoolJob | 客户不自动回收到公海 |
| hrm | HrmEmployeeChangeJob / HrmSalaryChangeJob / HrmPerformanceAppealTimeoutJob | 定时生效的调岗、调薪和申诉超时不处理 |
| iot | IotDeviceOfflineCheckJob / IotOtaUpgradeJob | 设备离线检测和 OTA 推进失效 |
| im | ImRtcCallCleanupJob / ImRtcParticipantTimeoutJob | 音视频通话残留 |
| pms / ai | PmsKnowledgeRecycleCleanJob、AiSunoSyncJob、AiMidjourneySyncJob | 回收站不清理，异步生成结果不同步 |

- 可选方案：① 仿照 `OaLocalScheduler`，给每个服务加一个可关闭的 `@Scheduled` 兜底（多实例时需要加分布式锁）；② 复用或新装 XXL-Job Admin（新装中间件需先确认）。

### 2. 商城、支付、会员主链路没有业务验收

- `business-http-verification.json` 里 product/promotion/trade/statistics/pay/member/mp 只验证了**分页查询**。
- `business-flow-verification.json` 只覆盖 erp/crm/bpm/report/iot。
- mall（842 个源文件）、member、erp、mp、gateway 的**单测数为 0**。
- 支付模块自带 `mock`（模拟支付）和 `wallet`（钱包）渠道，**无需外部凭据**就能在本地跑通「上架商品 → 加购 → 下单 → 支付 → 回调 → 发货 → 收货 → 售后退款」。文档目前把整个支付都归为“待凭据”，这个说法不准确。

### 3. 其他模块只有查询验收

mes、wms、hrm、im、pms、ai 只有查询和单测，没有通过网关执行写入与跨服务状态流转。按 `04-business-completion.md` 自己的标准，这些模块还不能算完成。

## P1：上线前必须补

| # | 缺口 | 现状 |
|---|---|---|
| 4 | 没有非本地环境的配置 | 只有 `application.yaml` 和 `application-local.yaml`，没有 dev/test/prod profile；Nacos 配置中心关闭（`config.enabled: false`）。 |
| 5 | 登录防爆破 | 24 个服务都是 `captcha.enable: false`，也找不到登录失败锁定逻辑。演示密码统一为 `Aicloud@123`。 |
| 6 | 配置中写死的默认凭据 | 例如 `MYSQL_PASSWORD:aicloud_dev_password`、Nacos `nacos/nacos`，开发 OAuth 客户端密钥写在种子 SQL 里。环境变量缺失时会静默回退到这些值，应改为缺失即启动失败。 |
| 7 | 网关防护 | 没有限流和熔断（Sentinel 已关闭）；`AccessLogFilter` 中有 `TODO 暂未实现`。 |
| 8 | 数据库版本管理 | 升级 SQL 只是被拼进 `aicloud.sql`（`bundle-sql.py`），没有 Flyway/Liquibase 或版本表。已有库升级依赖人工判断。 |
| 9 | Docker 只验证了核心 | `docker-verification.json` 只覆盖 4 个核心容器，`full` profile 的 24 个服务没有容器化验收。 |
| 10 | 可观测性 | SkyWalking/OTel/Spring Boot Admin 依赖都在，本地全部关闭；只有文件日志，没有集中日志、指标和告警方案。 |

## P2：工程化与清理

| # | 缺口 | 现状 |
|---|---|---|
| 11 | 版本控制 | `main` 分支**没有任何提交**，8759 个文件只是暂存；没有 remote。建议尽快提交首个基线并推送到私有远端。 |
| 12 | CI | 没有 `.github` 等流水线，构建、单测、SQL 校验都靠手动执行。 |
| 13 | 上游残留 | 231 个文件仍含 `yudao/iocoder`，336 处 `芋艿` 注释；每个 `*Application` 启动时打印 iocoder 文档链接，网关 `BannerApplicationRunner` 还打印知识星球推广链接。 |
| 14 | 上游 TODO | 337 处 TODO/FIXME（mall 66、system 22、bpm 20 等）。大部分是上游遗留，需要筛出真正影响功能的（如 IM 搜群/扫码/链接进群、CoAP 下行不支持）。 |
| 15 | README 表述 | “提供管理端、手机 App、微信小程序、Web/H5 用户端”实际指的是对应的**接口**，仓库里没有前端；infra 写的“定时任务”也没有对应的管理接口和执行器。 |

## 已确认不算缺口

- 外部支付、微信、短信、真实邮箱、AI 模型等待用户凭据，属于已约定的延期。
- 仅提供后端是 `AGENTS.md` 规定的范围。

## 建议顺序

1. 提交 Git 基线（#11）。
2. 恢复定时任务调度（#1）。
3. 用 mock/wallet 渠道补商城下单、支付、售后全链路验收和回归单测（#2）。
4. 逐个补 mes/wms/hrm/im/pms/ai 的写入链路验收（#3）。
5. 做生产化：profile、凭据、验证码/锁定、网关限流、数据库迁移（#4–#8）。
6. 最后做 CI 和上游残留清理（#12–#15）。
