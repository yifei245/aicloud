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
| 11 | ~~版本控制~~ | 已解决：2026-10-13 推送到 GitHub `yifei245/aicloud`（公开仓库）。注意 AI 模块测试代码里有上游写死的 `sk-...` 密钥。 |
| 12 | CI | 没有 `.github` 等流水线，构建、单测、SQL 校验都靠手动执行。 |
| 13 | 上游残留 | 231 个文件仍含 `yudao/iocoder`，336 处 `芋艿` 注释；每个 `*Application` 启动时打印 iocoder 文档链接，网关 `BannerApplicationRunner` 还打印知识星球推广链接。 |
| 14 | 上游 TODO | 337 处 TODO/FIXME（mall 66、system 22、bpm 20 等）。大部分是上游遗留，需要筛出真正影响功能的（如 IM 搜群/扫码/链接进群、CoAP 下行不支持）。 |
| 15 | README 表述 | “提供管理端、手机 App、微信小程序、Web/H5 用户端”实际指的是对应的**接口**，仓库里没有前端；infra 写的“定时任务”也没有对应的管理接口和执行器。 |

## 功能缺口（第二轮，2026-10-13）

依据：各 `*-server/pom.xml` 的跨模块 API 依赖、控制器清单和配置。

### 模块各自独立，业务没有打通

各服务只依赖 system/infra，业务模块之间几乎没有调用：

```text
erp  -> system            wms -> system          mes -> system
fms  -> infra system      hrm -> bpm system      oa  -> bpm infra system
crm  -> bpm infra system  trade -> member pay product promotion system
```

| # | 缺口 | 现状 | 后果 |
|---|---|---|---|
| 16 | 四套商品/库存主数据互不相通 | 商城 `product` SKU 库存、ERP `ErpProduct/ErpWarehouse/ErpStock*`、WMS `WmsItem/WmsWarehouse/WmsInventory`、MES `MesWm*` 各管各的，彼此没有任何 API 依赖 | 商城下单不动 ERP/WMS 库存；ERP 采购入库不进 WMS；MES 领料、成品入库不影响 WMS。同一件商品要在四处分别维护 |
| 17 | 财务没有业务凭证来源 | FMS 只依赖 infra/system，凭证只能手工录入 | ERP 收付款、商城收入、HRM 薪资、OA 报销审批通过后都不会生成凭证，财务报表反映不了业务 |
| 18 | OA 与 HRM 功能重复且不联动 | OA 有请假、加班、转正、离职审批和考勤；HRM 有员工档案、假期、8 个考勤控制器、14 个薪资控制器。OA 不依赖 hrm-api | OA 离职、转正审批通过后，HRM 员工状态不变；请假不扣假期余额；考勤存在两套数据 |
| 19 | CRM 与 ERP 不通 | CRM 合同、回款与 ERP 销售单、收款单没有关联 | 签约后要在 ERP 重新录一遍销售单，回款对账靠人工 |
| 20 | 审批方式不统一 | ERP/WMS/MES/PMS/FMS 都用各自的审核状态，没接 BPM | 无法统一配置审批流和待办，影响较小 |

### 通知与实时通信

| # | 缺口 | 现状 |
|---|---|---|
| 21 | **审批没有任何通知** | `BpmMessageServiceImpl` 的待办、通过、驳回、超时通知**只发短信**，本地默认 `BPM_SMS_ENABLED=false`，也没有站内信兜底。OA、CRM、HRM 的审批都走 BPM，审批人收不到任何提醒。建议加一条站内信通道（`NotifyMessageSendApi`，HRM/OA 已经在用） |
| 22 | WebSocket 只支持单实例 | infra `websocket.sender-type: local`，多实例部署时，连在其他实例上的用户收不到 IM 消息和公告推送。改为 `redis` 即可，现有 Redis 就能用 |
| 23 | IM 音视频不可用 | 依赖 LiveKit SFU，项目没有部署，配置里默认是 `devkey` 和明文默认密钥；搜群、扫码、链接进群三种入群方式未实现（`ImGroupAddSourceEnum` 里有 TODO） |

### 对外能力

| # | 缺口 | 现状 |
|---|---|---|
| 24 | 开放 API 只有一个接口 | `/open-api/**` 只有 `OpenApiController` 的字典查询（`dictionary.read`）。OAuth2 客户端凭证和 scope 机制已经完整，但没有可用的业务接口（订单、会员、库存查询、事件回调/Webhook 等） |
| 25 | 报表没有设计和打印 | 报表模块只有 GoView 大屏的后端（2 个控制器），积木报表按设计被移除（`docs/03-local-business-guide.md`）。单据打印、自定义报表都没有 |
| 26 | 员工移动端没有专用接口 | `app-api` 只覆盖会员、商城、支付、system、infra。OA、HRM、BPM、IM、CRM 都没有用户端接口；员工手机审批、打卡只能直接调用管理端接口 |

### 安全与依赖组件

| # | 缺口 | 现状 |
|---|---|---|
| 27 | XSS 过滤全部关闭 | 23 个服务都是 `aicloud.xss.enable: false`。OA 公告、CRM 跟进、IM 消息等富文本存在存储型 XSS 风险 |
| 28 | 以下能力依赖尚未部署的组件 | AI 知识库默认用本地简单向量库，Milvus/Qdrant/Redis 的依赖都在但没部署；IoT 历史数据写 MySQL，设备量大了会有瓶颈，没有接时序库；快递查询默认 `NOT_PROVIDE`，需要快递鸟或快递 100 凭据 |
| 29 | 没有国际化 | 没有 `messages*.properties`，错误码和提示只有中文 |

## 已确认不算缺口

- 外部支付、微信、短信、真实邮箱、AI 模型等待用户凭据，属于已约定的延期。
- 仅提供后端是 `AGENTS.md` 规定的范围。
- 积木报表是有意移除的（但打印和报表设计的需求仍要单独评估，见 #25）。

## 建议顺序

1. 恢复定时任务调度（#1），同时给 BPM 加站内信通知（#21）。这两项不做，核心流程跑不完整。
2. 用 mock/wallet 渠道补商城下单、支付、售后全链路验收和回归单测（#2）。
3. **先定业务边界**：库存以哪套为准（#16）、OA 和 HRM 谁负责人事数据（#18）、哪些业务要自动出凭证（#17）。这一步需要产品层面拍板，再决定要写多少集成代码。
4. 逐个补 mes/wms/hrm/im/pms/ai 的写入链路验收（#3）。
5. 做生产化：profile、凭据、验证码/锁定、XSS、网关限流、WebSocket 改 redis、数据库迁移（#4–#8、#22、#27）。
6. 按需求补开放 API、打印报表、员工移动端（#24–#26）。
7. 最后做 CI 和上游残留清理（#12–#15）。
