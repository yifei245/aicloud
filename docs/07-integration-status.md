# 业务集成状态与使用说明

原始审查记录保留在 `05-gap-list.md`。数据归属按 `06-business-integration.md` 实施。以下区分已有实现、实际验证和仍需补齐的业务，接口数量不作为完成标准。

后续按 [逐步交付验收清单](08-delivery-checklist.md) 执行并登记结果；目前项目未完整交付。

## 当前实现

| 范围 | 实现及验收边界 |
|---|---|
| ERP → WMS | 商品与仓库明确映射，ERP 审核入库、反审核、再次审核调用 WMS；ERP 库存查询读取 WMS。已通过本地真实 HTTP 核对两边数量。 |
| CRM → ERP | 明确绑定客户和商品、确认基本单位。合同审批通过后生成关联的待审核 ERP 销售单，保留合同价格、数量与折扣，校验总额。已通过本地真实 HTTP。纯文本合同须先补商品明细。 |
| OA → FMS | 报销通过审批后，按显式账套和科目规则生成借贷平衡的待审核凭证。已通过本地真实 HTTP，未自动审核、过账。其他来源、撤销及冲回尚需逐条业务验收。 |
| BPM 通知 | 站内信为主通道，源事务提交后持久重试，目标按业务键去重；短信为可选附加通道。已验证关闭短信时审批仍收到站内信。 |
| OA → HRM | 请假同步预占，审批扣减，拒绝/取消释放，原生 HRM 与 OA 共用额度和 HRM 请假事实；转正与离职使用 HRM 既有异动服务。回归测试及真实接口结果见下方报告。 |
| 员工接口 | `/employee-api` 使用员工后台身份，复用已有本人服务。会员、应用身份不能进入，OA 员工详情强制本人归属。接口与隔离结果见真实 HTTP 报告。 |
| 开放业务接口 | `/open-api/v1/erp/products` 和 `/open-api/v1/wms/inventory` 提供启用商品与 WMS 可用量查询，分别使用 `catalog.read`、`inventory.read` 和应用身份校验。16 项本地 HTTP 检查通过，覆盖字段裁剪、scope、身份及租户隔离；订单与事件回调尚待扩展。 |
| IM 音视频 | 两个独立用户经网关邀请/接听，以 IM 签发令牌加入本机 LiveKit，双向解码合成音频和视频；拒绝非参与者，结束后主记录及双方参与记录闭合。媒体 11 项及相关回归 17 项通过，见 `rtc-http-verification.json`、`rtc-unit-verification.json`。尚不代表手机真机及外网部署验收。 |
| 本地定时任务 | XXL 执行器关闭时复用既有 Job，Redis 锁加执行轮次记录避免多实例重复；覆盖订单、支付重试、人员异动等状态推进。已通过多实例调度逻辑回归；尚未完成两个真实业务实例的部署验证。 |
| XSS 与访问日志 | 启用富文本过滤，修复数据库字符串数组解析回归；网关只记录路径、状态及耗时，不记录请求/响应内容与认证头。持久化文本及身份隔离检查见 HTTP 报告。 |
| 集成待处理记录 | `/admin-api/infra/integration/pending` 查询本租户失败元数据，`POST /admin-api/infra/integration/retry?id=...` 重试 RETRY 状态；使用既有 `infra:job:query/update` 权限。 |

## 员工接口入口

| 前缀 | 能力 |
|---|---|
| `/employee-api/system/auth` | 登录、刷新和已有认证功能，复用同一员工 SSO 身份 |
| `/employee-api/system/notifications` | 本人通知分页、未读数、已读操作 |
| `/employee-api/bpm/tasks` | 本人待办、已办、通过、驳回；保留既有任务权限和实际办理人校验 |
| `/employee-api/hrm/profile` | 本人档案、字段可见与可编辑规则 |
| `/employee-api/hrm/attendance/clock` | 复用考勤组、定位、Wi-Fi 等规则的本人打卡接口 |
| `/employee-api/hrm/attendance/leave` | 本人 HRM 请假、列表、取消 |
| `/employee-api/hrm/leave-balances?year=2026` | 本人余额、预占和使用量，不接收 employeeId |
| `/employee-api/hrm/salary/slip` | 本人工资条与阅读状态 |
| `/employee-api/oa/leave-apply`、`regular-apply`、`resign-apply`、`reimbursement` | 本人申请的创建、修改、提交、列表、详情 |

需要原业务 permission，员工入口不会隐式授予管理权限。`terminal` 沿用既有 Web/H5/App/小程序终端标识。各服务的 Knife4j 文档同时展示员工入口。真实打卡须先配置考勤组；工资条须先完成薪资业务，不能用空查询代替完整业务验收。

## 业务配置

1. 通过 ERP 创建商品、基本计量单位和仓库。商品基本单位不允许直接更换，已被商品引用的单位不能改名，防止跨模块库存单位改变。
2. WMS `/integration/mappings` 查询主数据投影；`/integration/bind` 明确关联 MES/MALL 商品与仓库，禁止按数字 ID 猜测同一商品。没有映射会拒绝库存业务，不回落到另一套有效库存。
3. CRM `/integration/bind` 关联 CUSTOMER、PRODUCT；PRODUCT 必须明确确认 ERP 基本单位。`/integration/contract-sale` 查询合同生成的销售单。生成单据尚未拆税，审核前由业务人员确认税务参数。
4. FMS `/business/rule` 指定来源、账套、凭证字、借贷科目。配置人必须有账套写权限，已有未撤销凭证时不能将来源改绑其他账套。缺少规则会保留待处理记录，不猜测科目或静默丢弃。
5. HRM 员工必须绑定系统用户。`/attendance/leave-balance/policies` 查询统一假别，`/grant` 设置年度额度；年假 code=1，调休 code=8；原生 HRM 的中文假别映射到同一额度。跨年请拆分申请。余额不足、时间重叠均会拒绝提交。
6. `python3 scripts/init-business.py` 保留现有模型，补齐缺失的 14 个 OA、HRM 请假、CRM 合同模型并部署。已有自定义审批人不被覆盖。

本地调度默认只推进业务与重试。日志、令牌、知识库回收站等清理不默认启用，可通过 `aicloud.job.local-schedules.<XxlJob 名称>` 配置 cron，值 `-` 停用；`aicloud.job.local-enabled=false` 可关闭本地调度。启用 XXL 执行器时本地调度不启动。

## 复验

```bash
python3 scripts/run-local.py start core bpm wms erp crm fms hrm oa
python3 scripts/init-business.py
python3 scripts/verify-integration.py
```

- `integration-http-verification.json`：通过本机网关的实际单据、审批和目标数据结果；包含记录编号。脚本保留审计记录，不覆盖已有财务规则；成功后禁用新建的测试员工账号。
- `integration-unit-verification.json`：本轮 OA、HRM、WMS、消息重试、权限、XSS、调度相关回归；包括跳过数量，不能视为所有业务已验收。
- SQL 使用 `scripts/init-db.py` 安全增量检查，保留历史表与数据；有重复库存维度时拒绝自动合并。消息同一单据按顺序投递，历史无分组的记录回填自己的业务键。

核心 SSO 退出测试与本脚本串行执行，否则会撤销联调中的管理员令牌。

## 仍未完成

- MES 仍有本地批次库存校验，尚未完成从 ERP 入库到 MES 领料、产出的统一库存闭环，不能标为库存整体完成。
- 商城 WMS 预占/释放/发货、退货入库已有实现，但 mock/wallet 支付到售后退款的真实全链路验收尚未完成。
- FMS 其他业务来源、已审核凭证冲回和期间限制尚需完整验收；薪资目前以归档应发额计提，不代表银行实发。
- OA 打卡、加班、出差与 HRM 考勤的完整归并尚未完成；本轮请假事实已转入 HRM。
- 开放 API 的商品和库存查询已通过验收，订单与事件回调仍待扩展；打印与报表模板仍待实现。
- LiveKit 已部署并保留，两个真实 SDK 客户端的本地双向音视频、访问隔离与结束状态已验收；手机真机、外网 NAT/TURN 和正式 HTTPS 尚待对应环境验证。
- Redis WebSocket 已配置，两个真实实例的跨实例收发仍待验收。
- 非本地配置、登录防爆破、限流、版本化数据库迁移、全量容器验证等生产化条目仍见原审查清单。
