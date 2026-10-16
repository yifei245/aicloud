# aicloud

Java 21 后端微服务平台。目录以 ruoyi-vue-pro master-jdk17 为主，复用同源 MIT 项目 yudao-cloud 的真实业务实现；提供管理端、手机 App、微信小程序、Web/H5 用户端以及第三方 API。项目只包含后端，Swagger/Knife4j 可直接操作接口。

完整数据库、业务服务和运行脚本见下文。Knife4j 22 个业务分组已做基线检查，跨模块业务正在按统一数据归属补齐。当前联动能力及尚未完成的范围见 [业务集成状态](docs/07-integration-status.md)。模块内既有功能范围见 [实施清单](docs/02-implementation.md) 和验证报告为准；外部支付、微信、短信、AI 模型等使用真实服务调用，需要配置自己的账号。

## 设计与项目结构

[架构设计](docs/01-architecture.md) · [实施顺序](docs/02-implementation.md) · [服务端口清单](docs/services.json) · [数据库实体清单](docs/schema-inventory.json) · [本地业务操作](docs/03-local-business-guide.md)

```text
aicloud
├── aicloud-dependencies       # 核心：BOM 与依赖版本
├── aicloud-framework          # 核心：安全、Web、MyBatis Plus、Redis、租户、数据权限、RPC、日志
├── aicloud-gateway            # 核心：统一入口、Nacos 路由、身份头清理、文档聚合
├── aicloud-module-system      # 核心：账号、部门、角色、动态菜单/按钮、OAuth2/SSO、字典、审计
├── aicloud-module-infra       # 核心：文件、配置、代码生成、定时任务、API 日志
├── aicloud-module-member      # 用户端核心：会员、等级、积分、标签、地址、签到
├── aicloud-module-mall        # 商品、促销、交易、购物车、售后、商城统计，拆为 4 个服务
├── aicloud-module-pay         # 支付、退款、钱包、渠道配置、异步通知
├── aicloud-module-bpm         # Flowable：流程模型、表单、部署、审批、任务、抄送
├── aicloud-module-crm         # 客户、线索、商机、联系人、合同、回款
├── aicloud-module-erp         # 采购、销售、产品、仓库、库存、供应商、收付款
├── aicloud-module-report      # GoView 项目、可视化数据与真实 MySQL 查询
├── aicloud-module-ai          # 模型配置、聊天、知识库、工具、AI 工作流
├── aicloud-module-mp          # 公众号：账号、粉丝、菜单、素材、消息
├── aicloud-module-iot         # 物联网业务服务、设备协议网关
├── aicloud-module-mes         # 制造执行
├── aicloud-module-wms         # 仓库管理
├── aicloud-module-im          # 即时通讯
├── aicloud-module-hrm         # 人事管理
├── aicloud-module-fms         # 财务管理
├── aicloud-module-pms         # 项目管理
├── aicloud-module-oa          # 办公自动化
├── aicloud-server             # 可选：system/infra 单进程调试运行器
├── sql/mysql/aicloud.sql      # 599 张表：业务、菜单字典、账号、Flowable/Quartz、IoT 历史
├── scripts                   # 本地初始化、启动、停止与验收
├── deploy/Dockerfile
└── compose.yaml              # 仅管理 aicloud 应用，复用现有中间件
```

业务模块内部保留 `api/server` 分层，server 使用 `controller/admin`、`controller/app`、`service`、`dal/dataobject`、`dal/mysql`、`convert`。跨服务使用 API/Feign，MyBatis Plus 操作 MySQL。用户端在 `/app-api/**`，后台在 `/admin-api/**`，开放平台在 `/open-api/**`。每个业务服务可独立构建和启动。

## 技术栈

| 技术 | 锁定版本/用途 |
|---|---|
| Java | 21 LTS；Docker 验证使用 Temurin 21.0.12.1 |
| Spring Boot | 3.5.16，用户指定 3.5.x 系列的最新稳定版 |
| Spring Cloud | 2025.0.3，用户指定 2025.0.x 系列 |
| Spring Cloud Alibaba | 2025.0.0.0，Nacos 注册与发现 |
| MyBatis Plus | 3.5.17，MySQL CRUD、分页、逻辑删除 |
| MySQL | 8.4，本地独立 aicloud 数据库 |
| Redis | 复用本机实例，逻辑库 12，aicloud 键前缀 |
| Spring Security | BOM 管理，角色、按钮权限、租户与数据权限 |
| OAuth2 | 授权码、S256 PKCE、刷新、client_credentials、单点退出 |
| OpenAPI | springdoc 2.8.17 + Knife4j 4.5.0 |
| Flowable | 8.0.0，独立 ac_ 表前缀 |
| Spring AI | 1.1.8，模型和知识库 |
| 微信 SDK | 4.8.0 正式版，不使用每日构建或 Beta |
| Docker | Java 21 运行镜像、Compose profiles、健康检查、应用持久化卷 |

版本来源：[Spring Boot 发布](https://github.com/spring-projects/spring-boot/releases/tag/v3.5.16)、[Spring Cloud 发布](https://github.com/spring-cloud/spring-cloud-release/releases/tag/v2025.0.3)、[MyBatis Plus 发布](https://github.com/baomidou/mybatis-plus/releases/tag/v3.5.17)。Boot/Cloud 按[官方兼容矩阵](https://spring.io/projects/spring-cloud)组合；不切换至 Boot 4，也不单独升级 BOM 管理的框架依赖。

## 本地启动

需要已有 JDK 21、Maven、Docker MySQL、Redis、Nacos。所有数据库操作仅通过本地 Docker socket 执行。脚本不安装或删除任何中间件，不重建共享容器，不执行清库。

```bash
cd /Users/yifei/git/aicloud
# 先启动已有本地 MySQL、Redis、Nacos；容器已运行时跳过
docker start mysql redis nacos
mvn -q -s .mvn/settings.xml -DskipTests package
python3 scripts/init-db.py
python3 scripts/reuse-local.py
python3 scripts/run-local.py start
python3 scripts/run-local.py status
python3 scripts/init-business.py  # 导入并发布 14 个 OA、HRM 请假和 CRM 合同审批流程，保留已有自定义模型
python3 scripts/verify-openapi.py # 检查 Knife4j 的全部 22 个业务分组
```

初始化自动生成权限为 0600 的 `.env`，包含仅用于 aicloud 库的 MySQL 用户、随机数据库密码和内部 RPC 凭据。已有 `.env` 会被保留。`reuse-local.py` 读取本机已有 Redis 容器的认证配置并保存到 `.env`，不会打印凭据；其他认证方式可从 `.env.example` 手动配置。默认启动全部 24 个应用，按依赖顺序逐个等待健康检查，完整启动需要数分钟。本地普通服务最大堆为 512m，BPM、AI、MES 为 768m，可用 `AICLOUD_JAVA_XMX` 调整；按需裁剪时使用下面的 `core` 组合。

本机已有旧版本 aicloud 数据时，脚本检查兼容性后追加新表；保留旧表与数据。新文件表为 `infra_file_record`，会员等级表为 `member_user_level`，工作流使用 `ac_ACT_*` / `ac_FLW_*`，IoT 调度使用 `ac_QRTZ_*`，避免撞名。脚本可恢复未完成的初始化，也可重复执行，不会重置账号密码。手动 SQL 初始化请在全新、兼容的 `aicloud` 库执行 `sql/mysql/aicloud.sql`。

运行日志：`data/local/<服务>.log`；文件：`data/files`。本地服务只监听 `127.0.0.1`，Nacos 使用 `AICLOUD` 分组。停止时只操作本脚本记录且确认属于 aicloud 的 Java PID：

```bash
python3 scripts/run-local.py stop
# 只运行登录、权限、文件和会员等核心服务
python3 scripts/run-local.py start core
# 按需启动业务服务
python3 scripts/run-local.py start pay product promotion trade statistics bpm crm erp
python3 scripts/run-local.py stop pay product promotion trade statistics bpm crm erp
```

集成验收需先启动 `core bpm wms erp crm fms hrm oa`，再执行 `python3 scripts/verify-integration.py`。脚本通过本机网关创建带验收标识的单据，保留库存、审批和凭证审计记录；不会覆盖已有审批模型或财务规则。

## 登录与 API 调试

所有演示账号密码均为 **Aicloud@123**，租户编号为 **1**。正式使用时修改演示密码及开发 OAuth 客户端密钥。

| 身份 | 账号 | 权限 |
|---|---|---|
| 管理员 | admin | 超级管理员，全部已启用模块 |
| 管理员 | viewer | 用户查询，只读验证账号 |
| 会员 | 13800000000 | 用户端账号，不能访问管理端接口 |

网关：[Knife4j](http://127.0.0.1:18080/doc.html)。Java 进程方式还可直连系统服务 [Swagger UI](http://127.0.0.1:48081/swagger-ui/index.html)；Docker 方式使用网关文档。在 Swagger 中设置 `tenant-id: 1`，登录后填入 `Bearer <accessToken>`。

Knife4j 聚合 22 个业务服务，MQTT 网关和 HTTP 网关自身没有业务分组。只启动 `core` 时，其他分组的文档地址会返回 503；要浏览全部接口，请执行 `python3 scripts/run-local.py start full`，或者启动 Docker 的 `full` profile。`verify-openapi.py` 逐组检查真实网关文档，并将结果保存到 `docs/knife4j-verification.json`。业务验证脚本保留运行前已经启动的应用。

```bash
curl http://127.0.0.1:18080/admin-api/system/auth/login \
  -H 'Content-Type: application/json' -H 'tenant-id: 1' \
  -d '{"username":"admin","password":"Aicloud@123"}'
curl http://127.0.0.1:18080/app-api/member/auth/login \
  -H 'Content-Type: application/json' -H 'tenant-id: 1' -H 'terminal: 31' \
  -d '{"mobile":"13800000000","password":"Aicloud@123"}'
```

动态菜单由 `GET /admin-api/system/auth/get-permission-info` 返回。按钮 permission 与后台 `@PreAuthorize("@ss.hasPermission('模块:资源:操作')")` 对应；只读账号调用写接口将被拒绝。`terminal` 为 `10` 小程序、`11` 公众号、`20` H5、`31` App；不传时为 Web。登录、刷新需使用同一终端。

## SSO 与第三方 API

SSO 接口为 `/admin-api/system/oauth2/**` 与 `/app-api/system/oauth2/**`。客户端精确配置允许的 `redirectUris`；申请授权时提供随机 `state`。授权端返回回调 URL，由调用端校验 state 后兑换授权码。

开发客户端：`sso-web / aicloud_dev_sso_secret`（回调 `http://localhost:3001/callback`）、`sso-mobile / aicloud_dev_mobile_secret`（回调 `http://localhost:3002/callback`）；公开客户端 `sso-public` 没有密钥，回调 `http://localhost:3003/callback`，必须使用 PKCE。

`POST .../authorize` 表单包含 `response_type=code`、`client_id`、`redirect_uri`、`state`、`auto_approve=false`、`scope={"user.read":true}`。PKCE 另传 `code_challenge=BASE64URL(SHA256(code_verifier))`、`code_challenge_method=S256`。`POST .../token` 表单使用 `grant_type=authorization_code`、`code`、`redirect_uri`、`state`；公开客户端传 `client_id` 和 `code_verifier`，机密客户端传 Basic 认证或 `client_id/client_secret`。授权码只能使用一次，过期后不可兑换，刷新令牌不能作为 Bearer 凭据。

`POST .../logout` 撤销当前身份在所有客户端的令牌；`DELETE .../token` 用机密客户端凭据撤销该客户端令牌。第三方应用使用独立 APPLICATION 身份与 scope，不能调用管理端或会员接口：

```bash
curl http://127.0.0.1:18080/admin-api/system/oauth2/token \
  -u third-party:aicloud_dev_api_secret -H 'tenant-id: 1' \
  -d 'grant_type=client_credentials&scope=dictionary.read'
# 使用返回的 access_token
curl http://127.0.0.1:18080/open-api/v1/system/dictionaries/system_user_sex \
  -H 'tenant-id: 1' -H 'Authorization: Bearer <access_token>'
```

内部 `/rpc-api/**` 只接受 `AICLOUD_INTERNAL_SECRET` 服务凭据，网关不提供 RPC 路由；外部身份头不作为认证依据。后端仍验证用户 Token、身份类型、租户和方法权限。

## Docker 与模块裁剪

Compose 仅管理 aicloud 应用；MySQL/Redis/Nacos 仍使用本机已有容器。宿主机地址由 `host.docker.internal` 访问，Linux 已配置 host-gateway。先执行 Maven 打包并生成 `.env`，然后：

```bash
# 如果已用 run-local 启动核心服务，先停止它们以释放端口和避免重复注册
python3 scripts/run-local.py stop
docker compose build
docker compose up -d
docker compose ps
# 完整业务启动（24 个服务，需要足够内存）
docker compose --profile full build
docker compose --profile full up -d
# 可独立开启业务 profile
docker compose --profile erp --profile crm up -d --build
```

核心默认启动 gateway/system/infra/member，其余模块有同名 profile（mall 拆为 product/promotion/trade/statistics，iot 另含 iot-gateway）。HTTP 入口只发布网关 18080，业务服务通过内部网络访问；启用 iot-gateway profile 时另发布本机 MQTT 1883，可用 IOT_MQTT_PORT 调整宿主端口。数据保存在应用卷中，不删除卷。裁剪时保留 dependencies/framework/gateway/system/infra；面向用户端还需 member。交易依赖商品、促销、会员和支付；工作流依赖 system/infra；运行组合见[模块依赖和业务操作](docs/03-local-business-guide.md)。

应用使用同一种运行方式：Docker 核心配合 Docker profiles，Java 核心配合 run-local。切回 Java 进程方式时，先用 `docker compose --profile full stop` 停止本项目应用容器，再启动 run-local；这会保留容器和数据卷，也不操作共享中间件。

## 外部服务配置

短信/邮件渠道、支付应用和渠道、微信公众号/小程序、OAuth 社交登录、AI 模型的凭据均由配置或对应后台接口管理。支付通知 URL 必须是对应平台可访问的网关地址；微信 `WECHAT_MP_APP_ID`、`WECHAT_MP_SECRET`、`WECHAT_MINI_APP_ID`、`WECHAT_MINI_SECRET` 按自己的账号配置。本地 BPM 短信通知默认关闭（`BPM_SMS_ENABLED=false`），配置渠道与模板后再启用。IoT 消息与属性历史使用 MySQL，消息总线使用已有 Redis；本地 AI 简单向量存储位于 `data/vector_store`，Docker 应用卷会保留它。没有凭据时不会伪造外部调用成功。需要额外中间件时先确认安装，后续保留供其他项目复用。

## 本地验收结果

Java 21 全模块编译通过，87 个 Maven 模块、24 个应用包。Knife4j 22/22 分组通过文档与浏览器检查；核心真实 HTTP 55 项、业务查询 393 次、OA 293 项、财务 24 项检查通过。业务模块本地单测报告累计 2460 项，无失败；原平台单测、五类业务流程和 Docker 验证详见[验收清单](docs/02-implementation.md)。完整 SQL 在全新本地库执行成功，基线创建 599 张应用表；集成升级后应用表为 611 张，本机原有 152 张表保留。

上述数字是集成改造前的历史基线，不能代替当前联动验收。新增改动及回归结果以 [业务集成状态](docs/07-integration-status.md)、`docs/integration-http-verification.json` 和 `docs/integration-unit-verification.json` 为准。外部支付、微信、短信、真实邮箱和 AI 调用按约定待配置凭据后验证。运行核心验收与业务验收时使用串行命令，核心单点退出检查会撤销管理员的其他令牌。

## 协议与参考

新增及派生应用代码使用 [MIT](LICENSE)，保留 [yudao-cloud 上游 MIT](LICENSES/yudao-cloud-MIT.txt) 和 [NOTICE](NOTICE)。随附 Flowable/Quartz SQL 仍适用 [Apache-2.0](LICENSES/flowable-Apache-2.0.txt)；各第三方依赖保留其原有协议。

[ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro/tree/master-jdk17)、[yudao-cloud](https://github.com/YunaiV/yudao-cloud/tree/master-jdk17)、[pig](https://github.com/pig-mesh/pig)、[SpringBlade](https://github.com/chillzhuang/SpringBlade)、[JeecgBoot](https://github.com/jeecgboot/JeecgBoot)。后面三项用于架构参考，未复制其源码。
