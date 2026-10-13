# 实施顺序与验收结果

设计先于实施，架构见 [01-architecture.md](01-architecture.md)。目前完成了源码迁移、基础平台，以及下表记录的业务验证。已补齐 OA 流程初始化、审批回调、用印登记、提醒和云盘附件权限；财务模块已补齐科目与报表种子数据。Knife4j 全部分组已通过实际浏览器切换。验收范围仍以具体记录为准，不能把接口显示正常等同于所有业务逐条验收，详见 [04-business-completion.md](04-business-completion.md)。支付、微信、小程序、短信和 AI 的真实平台调用按用户确认，等待后续配置凭据。仅修改后端，无前端工程。

## 实施顺序

1. 核验参考项目许可，迁移同源 MIT 业务实现，统一 aicloud 结构、Java 21 与稳定依赖。
2. 检查并复用本机 MySQL、Redis、Nacos；生成完整 SQL，隔离旧表、Redis 键与 Nacos 分组。
3. 实现并验证管理端/用户端认证、动态权限菜单、按钮授权、租户与身份边界。
4. 补齐 OAuth2/SSO、S256 PKCE、授权码一次性兑换、客户端认证、scope、单点退出和内部 RPC 边界。
5. 构建业务微服务，逐模块验证 MySQL 查询，再验收写入、审批、库存冲回与历史读取。
6. 构建 Docker 核心应用，验证网关、认证、文件持久化及 Swagger；补齐启动和业务操作文档。

## 已记录的验收

| 项目 | 结果 | 证据 |
|---|---|---|
| 全模块干净构建 | Java 21，87 个 Maven 模块，24 个应用包，6924 个 Java 源文件 | [构建](build-verification.json) |
| 完整 SQL | 全新本机 MySQL 库执行成功，599 张应用表，三个演示账号 | [SQL 执行](schema-execution-verification.json) |
| 保留既有数据结构 | 原有 152 张表全部保留；初始化可重复执行 | [本地数据库](local-database-verification.json)、[原始清单](local-database-baseline.json) |
| 关键单元测试 | 123 项通过：认证/SSO/权限、IoT 类型转换与 MySQL 属性存储、报表 SQL 限制、CRM 权限继承 | [单元测试](unit-test-verification.json) |
| 核心真实 HTTP | 55 项通过：管理登录、五终端会员登录、身份与租户隔离、菜单/按钮、SSO/PKCE、重放拒绝、scope、退出和部门 CRUD | [核心 HTTP](core-http-verification.json) |
| 业务服务 | 20 个可选服务通过；393 次真实数据库查询；MQTT 网关监听检查通过，验证结束后保留已启动服务 | [业务 HTTP](business-http-verification.json) |
| 当前本地运行组合 | 24/24 应用健康；22 个 HTTP 业务服务、HTTP 网关及 MQTT 网关全部运行 | [运行状态](local-runtime-verification.json) |
| Knife4j 文档与界面 | 22/22 分组通过：3443 个路径、3484 个操作；实际逐组切换后均有业务目录 | [网关文档](knife4j-verification.json)、[浏览器](knife4j-browser-verification.json)、[实际接口截图](verification/knife4j-oa.jpg) |
| OA 本地业务 | 11 类业务、293 项检查通过：审批通过/驳回、报销、公文、会议、车辆、用品、用印、协作、云盘权限、实际提醒、邮箱供应商配置 | [OA 业务](oa-business-verification.json)、[14 个流程初始化](business-initialization.json) |
| 财务初始化与报表 | 24 项通过：真实新建账套、113 个科目、21 个结账模板、三类报表和21 项现金流辅助定义、重复初始化保护 | [财务业务](fms-business-verification.json) |
| 业务单元测试 | 本地 Surefire 报告累计 2460 项，无失败：OA/FMS/HRM/IM/PMS/WMS/MES/文件/BPM；本轮重跑审批回调、OA、文件和财务回归 | [业务单测](business-unit-verification.json) |
| 业务数据流 | 60 项通过：ERP 入库审批/冲回，CRM 客户与跟进，BPM 部署/发起/审批，GoView 保存与 SQL 查询，IoT 属性上报/MySQL 历史/消息日志 | [业务流程](business-flow-verification.json) |
| Docker | 36 项通过：四核心容器健康、端口隔离、网关/认证/文档、上传下载与文件服务重启持久化 | [Docker](docker-verification.json) |

上述结果覆盖启动、接口契约、无父记录时可执行的查询及已列出的数据流；没有把全部公开操作与内部 RPC 都标为逐条业务验收。需要真实账号的外部调用尚未执行，也没有使用伪造成功响应。邮箱验收为本地供应商配置 CRUD，真实 SMTP/IMAP 需用户账号。

## 验证命令与运行前提

```bash
mvn -q -s .mvn/settings.xml -DskipTests clean package
python3 scripts/init-db.py
python3 scripts/reuse-local.py
python3 scripts/run-local.py start
python3 scripts/init-business.py
python3 scripts/verify-openapi.py
python3 scripts/smoke-test.py
python3 scripts/verify-services.py
python3 scripts/verify-flows.py
python3 scripts/verify-oa.py
python3 scripts/verify-fms.py
python3 scripts/verify-schema.py
# 核心 Compose 容器启动后
python3 scripts/verify-docker.py
```

认证、SSO、IoT、报表和 CRM 单测使用 Maven `-pl ... -am -Dtest=... -Dsurefire.failIfNoSpecifiedTests=false test` 选择相关模块。构建使用 skipTests；单元测试和 HTTP/业务验证分别执行，不以打包代替测试。

切换到 Docker 前停止本地 aicloud Java 进程，避免同名服务重复注册和端口冲突。只启动、停止本项目应用，不删除共享中间件或数据卷。业务验证记录使用 `aicloud verify` 名称；保留审批、库存和消息审计历史。

以上 HTTP 验证命令按顺序执行。核心验证包含单点退出，会撤销同一管理员的业务令牌，因此不要与 OA 等使用管理员登录的业务验证并行运行。`verify-services.py` 与 `verify-flows.py` 只停止自己新启动的应用。

## 待用户凭据

- 支付：自己的支付商户、应用、渠道密钥与可达通知 URL。
- 微信/小程序：对应 appId、secret 和回调配置。
- 短信/邮件：真实渠道及模板；BPM 本地短信通知默认关闭。
- AI：模型提供方、API key、模型配置；外部模型调用与真实向量检索等待凭据验收。

本地启动与数据库操作不依赖上述凭据。没有安装额外中间件；现有本机实例保持保留。
