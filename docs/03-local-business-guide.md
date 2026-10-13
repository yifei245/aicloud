# 本地业务操作与模块依赖

所有 HTTP 操作通过 `http://127.0.0.1:18080`；使用 `tenant-id: 1` 和登录返回的 `Authorization: Bearer <accessToken>`。管理端与会员分别登录，访问令牌不能混用。网关 Knife4j 地址 `/doc.html`，启用业务服务后选择对应接口分组。

## 业务服务组合

核心为 system/infra/gateway；用户端再启用 member。下列为功能运行时的主要依赖，裁剪须以正在使用的业务为准。

| 使用的业务 | 同时启用 |
|---|---|
| 管理员、角色菜单、SSO、开放平台 | system、infra、gateway |
| 会员资料、等级、积分、地址 | 核心、member |
| 商品与促销管理 | 核心、product、promotion、member |
| 订单、售后、钱包付款 | 核心、member、product、promotion、trade、pay |
| 商城统计、商品排名 | 核心、statistics、product；有交易数据时启用 trade |
| 流程审批 | 核心、bpm；关联业务申请还须启用对应业务模块 |
| CRM、ERP、MES、WMS、IM、HRM、FMS、PMS、OA | 核心和对应业务模块；调用审批时启用 bpm |
| GoView 项目及 MySQL 数据查询 | 核心、report |
| IoT 产品、设备、属性历史和规则 | 核心、iot；真实 MQTT 设备接入再启用 iot-gateway |
| AI 管理、知识库、模型调用 | 核心、ai；模型调用需自己的凭据 |
| 微信公众号 | 核心、mp；真实平台调用需自己的凭据 |

本地 `python3 scripts/run-local.py start` 默认启动全部 24 个应用，等待健康检查后返回；仅核心组合使用 `start core`。也可按模块执行 `start erp crm bpm report iot`。Docker 使用同名 profile，例如 `docker compose --profile erp --profile crm up -d`。要构建尚未构建的应用镜像，先对相同 profiles 执行 `docker compose ... build`。完整 Docker 组合使用 `--profile full`。未启用的服务没有 Nacos 实例，其 Knife4j 文档返回 503；浏览全部分组需要完整组合。

## ERP：入库、审批与库存冲回

先创建单位 `/admin-api/erp/product-unit/create`、分类 `/erp/product-category/create`、产品 `/erp/product/create` 和仓库 `/erp/warehouse/create`。产品关联 categoryId、unitId；仓库和产品 status 为 0。

创建 `/admin-api/erp/stock-in/create`，包含 inTime（毫秒时间戳）及 items：warehouseId、productId、productPrice、count。供应商可选。`PUT /erp/stock-in/update-status?id=<入库单>&status=20` 审批后，`GET /erp/stock/get-count?productId=...&warehouseId=...` 可查看增加后的库存；将状态改为 10，会冲回对应库存变化。审批中的单据不能直接修改或删除。

## CRM：客户与跟进

`POST /admin-api/crm/customer/create` 包含 name、ownerUserId。负责人拥有读写权限，读写成员可以读取，只读成员不能写入。跟进 `/crm/follow-up-record/create` 包含 bizType=2、bizId=客户编号、type、content、nextTime（毫秒时间戳）；可通过 get、page 查询相关记录。

## BPM：部署与审批

`POST /admin-api/bpm/model/create` 创建 BPMN 模型，key、name 必须与 XML process id/name 对应。type=10，formType=20 表示业务自定义表单，需设置 formCustomCreatePath/formCustomViewPath；managerUserIds 指定管理用户。用户审批节点配置 candidateStrategy=30、candidateParam=用户编号，并设置 approveType=1。

`POST /bpm/model/deploy?id=...` 部署；从 model/list 获取 processDefinition.id；`POST /bpm/process-instance/create` 发起，包含 processDefinitionId、variables；通过 `/bpm/task/todo-page` 找到任务，然后 `PUT /bpm/task/approve` 提交 id、reason、variables。完成后 process-instance/get 的 status=2。

本地默认 `BPM_SMS_ENABLED=false`，未配置短信不会阻止审批。启用短信前先通过 system 短信渠道/模板接口配置真实账号及 BPM 对应模板，再设置为 true；审批记录、待办和完成状态仍通过本地接口提供。

## GoView：项目保存与真实查询

`POST /admin-api/report/go-view/project/create` 包含 name；update 接口保存 content（JSON 字符串）、status 和 remark。data/get-by-sql 接口接受 `{ "sql": "SELECT ..." }`，只允许一条 SELECT 查询，限定 aicloud 库，自动为含 tenant_id 的表加租户条件，最多返回 1000 行，查询超时 10 秒。该功能应只授权可信报表管理员。data/get-by-http 返回最近 30 天真实 API 请求统计。

本工程提供 GoView 后端和数据接口。报表界面属于后续前端工作；此项目不包含积木报表/JimuBI 依赖或设计器。

## IoT：设备模拟与 MySQL 历史

创建 product-category、product（未传 productKey 时自动生成）、thing-model 属性定义和 device。模拟上报 `/admin-api/iot/device/message/send`：

```json
{"deviceId":1,"method":"thing.property.post","params":{"temperature":26.5}}
```

上报异步经过 Redis 消息总线，属性落入 iot_device_property_history，消息落入 iot_device_message。使用 property/history-list?deviceId=...&identifier=temperature 查询历史，property/get-latest 查询最新值，device/message/page 查询消息。历史值保留 JSON 类型，数值、字符串和结构值均可存储；不依赖 TDengine。

## 验证命令

核心文件服务默认保存到 `data/files`。文件配置的 domain 只填网关域名（例如 `http://localhost:18080`），下载路径由文件客户端自动拼接；Docker 保存到 infra 应用卷。

```bash
python3 scripts/smoke-test.py        # 核心服务先启动；认证、SSO、权限、数据库写入
python3 scripts/verify-services.py   # 逐业务服务启动、OpenAPI、真实 MySQL 查询
python3 scripts/verify-openapi.py    # 完整组合运行时，检查全部 Knife4j 分组
python3 scripts/init-business.py    # BPM 启动后，发布或保留 14 个 OA 默认流程
python3 scripts/verify-oa.py        # OA/BPM/核心服务先运行；审批、协作、用印、云盘与实际提醒
python3 scripts/verify-fms.py       # FMS/核心服务先运行；真实账套初始化及三类财务报表
python3 scripts/verify-flows.py      # ERP、CRM、BPM、GoView、IoT 的写入与状态变更
python3 scripts/verify-schema.py     # 本机 MySQL 临时库完整 SQL 验证
python3 scripts/verify-docker.py     # 核心 Compose 容器先启动；网关和重启持久化
```

核心验收含全局 SSO 退出，不要与其他管理员业务验收并行执行。OA 云盘使用本地/数据库私有存储：上传时可传 `privateFile=true`，文件节点创建还会校验实际上传者与文件大小。READ 权限不返回下载地址，DOWNLOAD 权限返回默认 5 分钟、最长 15 分钟的签名地址；权限撤销后无法取得新地址，已经发出的签名在过期前仍有效。当前私有云盘只接入本地/数据库存储，外部存储的私有桶与原生签名适配尚未接入；服务器会拒绝把外部公开存储标为私有。

验证脚本仅操作 localhost/本地 Docker。数据库脚本验证只清理它自己创建的临时库；业务验证只删除本次创建的记录，可保留完成的审批和库存审计历史。JSON 报告保存于 docs，未通过检查不会计为完成。
