# AICloud

AICloud 是一个以 Java 后端为主的综合型微服务项目，项目结构主要参考 `ruoyi-vue-pro / yudao-cloud` 风格，并结合 Pig、SpringBlade、JeecgBoot 的模块化思想实现。当前项目支持 Spring Cloud 微服务架构、统一网关、统一认证、动态权限菜单、多租户、Knife4j 聚合接口文档、Vue 管理端、Docker、本地 MySQL/Redis/Nacos 环境。

## 技术栈

- JDK：21
- Spring Boot：3.5.0
- Spring Cloud：2025.0.0
- Spring Cloud Alibaba：2025.0.0.0
- 注册中心：Nacos
- 数据库：MySQL，数据库名 `aicloud`
- ORM：MyBatis Plus
- 缓存：Redis
- 网关：Spring Cloud Gateway
- 接口文档：Springdoc OpenAPI + Knife4j 聚合入口
- 管理端：Vue 3 + TypeScript + Vite + Element Plus + Pinia
- 构建：Maven 多模块 + pnpm
- 容器：Docker / Docker Compose
- JDK 21 新特性：Servlet 业务服务启用虚拟线程，容器默认使用 Generational ZGC
- AOT/Native：提供 Spring AOT 检查脚本和 GraalVM Native Maven profile，默认不影响普通 JVM 构建
- 代码规范：阿里 P3C PMD 规则
- 许可证：MIT

## 登录账号

| 租户 | 用户名 | 密码 | 终端 | 说明 |
| --- | --- | --- | --- | --- |
| 1 | `admin` | `123456` | `ADMIN` | 超级管理员 |
| 1 | `ops` | `123456` | `ADMIN` | 运营/管理账号 |
| 1 | `appuser` | `123456` | `APP` | 终端会员账号 |
| 2 | `admin` | `123456` | `ADMIN` | 租户 2 管理账号，按数据脚本初始化情况可验证 |

> 网关会根据 `Authorization: Bearer <accessToken>`、`X-Tenant-Id`、用户角色和权限点进行鉴权和租户上下文转发。超级管理员支持跨租户访问策略扩展。

## 项目结构

```text
aicloud
├── aicloud-dependencies                 # 统一依赖版本管理
├── aicloud-framework                    # 公共框架 starter
│   ├── aicloud-common                   # ApiResponse、PageResponse、异常码、业务异常
│   ├── aicloud-spring-boot-starter-web  # 全局异常、审计过滤器、Web 公共配置
│   ├── aicloud-spring-boot-starter-biz-tenant      # TenantContext、多租户上下文
│   ├── aicloud-spring-boot-starter-mybatis-plus    # MyBatis Plus 分页与租户拦截
│   ├── aicloud-spring-boot-starter-biz-rbac        # @RequirePermission、权限拦截
│   ├── aicloud-spring-boot-starter-security        # 登录用户上下文门面
│   ├── aicloud-spring-boot-starter-openapi         # OpenAPI 公共能力预留
│   ├── aicloud-spring-boot-starter-redis           # Redis starter 预留
│   ├── aicloud-spring-boot-starter-job             # 任务 starter 预留
│   └── aicloud-spring-boot-starter-mq              # MQ starter 预留
├── aicloud-gateway                      # 网关服务
├── aicloud-auth                         # 认证中心
├── aicloud-api                          # Admin/App/Web/Mp/OpenAPI 接口契约 DTO
├── aicloud-module-system                # 系统管理
├── aicloud-module-infra                 # 基础设施
├── aicloud-module-member                # 会员中心
├── aicloud-module-product               # 商品中心
├── aicloud-module-promotion             # 营销中心
├── aicloud-module-trade                 # 交易中心
├── aicloud-module-pay                   # 支付中心
├── aicloud-module-merchant              # 商户中心
├── aicloud-module-crm                   # CRM
├── aicloud-module-erp                   # ERP / 库存
├── aicloud-module-bpm                   # 工作流
├── aicloud-module-mp                    # 小程序/公众号
├── aicloud-module-openapi               # 第三方开放平台
├── aicloud-module-report                # 报表中心
├── aicloud-server                       # 服务聚合/部署模块
├── aicloud-ui-admin                     # Vue 管理端
├── docker                               # Dockerfile 与 compose
├── sql                                  # 初始化脚本与迁移脚本
├── scripts                              # 本地运行、构建、迁移、冒烟测试脚本
└── docs                                 # 架构、API、部署、规范文档
```

## 已实现功能清单

### 1. 公共框架

- 统一响应对象：`ApiResponse<T>`。
- 统一分页对象：`PageResponse<T>`。
- 统一异常模型：`BusinessException`、`ErrorCode`。
- 全局异常处理：隐藏内部异常细节，避免直接暴露堆栈和数据库错误。
- 公共审计模型：`AuditLogEvent`、`AuditLogPublisher`、`AuditLogFilter`。
- 公共安全上下文：从网关透传请求头解析当前用户、角色、权限、租户。
- 公共 RBAC：`@RequirePermission` 注解和权限拦截器。
- 公共租户上下文：`TenantContext`、`TenantContextHolder`、`TenantContextFilter`。
- MyBatis Plus 公共配置：分页插件和 `TenantLineInnerInterceptor` 租户拦截器。
- Redis Starter：统一 `RedisCacheService`，提供 key 前缀、默认 TTL、JSON 序列化、计数与删除能力。
- Job Starter：统一异步线程池、调度线程池和 `JobRunner`，避免各模块重复定义执行器。
- MQ Starter：统一 `DomainEvent` 和 `MessagePublisher` 抽象，默认使用 Spring 本地事件，后续可替换 RabbitMQ/Kafka/RocketMQ 适配器。
- OpenAPI Starter：统一 Swagger/OpenAPI 文档信息、Bearer Token 和 `X-Tenant-Id` 安全头。
- Biz Auth Starter：统一 `BizAuthService`，提供当前用户、租户、管理员、权限断言能力。
- 阿里 P3C 代码规范检查脚本已接入。

### 2. 网关与鉴权

- Spring Cloud Gateway 统一入口。
- 统一 Token 校验，调用认证中心 `/auth/token/verify`。
- 多终端支持：`ADMIN`、`WEB`、`APP`、`MP`、`OPENAPI`。
- 用户类型支持：管理端用户、会员端用户、开放平台应用。
- 权限点校验：根据用户权限列表和网关路由规则控制访问。
- 租户头转发：`X-Tenant-Id`。
- 用户上下文转发：`X-User-Id`、`X-Username`、`X-User-Type`、`X-Login-Terminal`、`X-User-Roles`、`X-User-Permissions`。
- 超级管理员跨租户策略：支持根据请求租户切换有效租户。
- OpenAPI 签名鉴权：`X-App-Key`、`X-Timestamp`、`X-Nonce`、`X-Sign`。
- OpenAPI 防重放、时间窗校验、限流头返回。
- 网关保持纯 WebFlux 边界层：不引入 MyBatis Plus、JDBC、Mapper、实体表映射，不直连 MySQL。
- OpenAPI 应用签名密钥由 `aicloud.gateway.openapi.apps` 配置注册，后续可改为远程调用开放平台服务校验。
- 网关审计日志当前输出结构化日志；审计落库、归档、查询、导出应下沉到 infra/report/audit 业务服务，避免网关混入阻塞型数据层。

### 3. 认证中心

- 账号密码登录：`/auth/login`。
- SSO 客户端换取令牌：`/auth/sso/token`。
- Access Token 刷新：`/auth/token/refresh`。
- Token 校验：`/auth/token/verify`。
- 当前登录用户信息：`/auth/me`。
- 退出登录：`/auth/logout`。
- 在线会话列表：`/auth/sso/sessions`。
- 按用户踢出会话：`/auth/sso/kickout`。
- 按会话 ID 强制下线：`/auth/sso/kickout/session`。
- 权限点检查：`/auth/permission/check`。
- 登录日志记录。
- Refresh Token 独立过期时间。

### 4. 多租户体系

- 数据表统一使用 `tenant_id` 进行租户隔离。
- 请求级租户上下文自动解析。
- MyBatis Plus 租户拦截器已接入主要业务模块。
- 超管支持跨租户访问扩展。
- 用户、角色、菜单、部门、岗位、字典、系统参数、商品、订单、支付、会员、营销等核心表具备租户字段。
- 已新增角色部门权限表 `ai_role_dept`。
- 管理端用户新增/编辑支持租户上下文。
- 商品、订单、支付等业务接口支持租户维度分页查询。

### 5. 系统管理

- 用户管理：分页、查询、详情、新增、编辑、启停、重置密码、删除、角色分配、部门岗位关联。
- 角色管理：列表、详情、新增、编辑、启停、删除、菜单授权、数据范围、部门数据权限。
- 菜单管理：菜单树、按钮权限、动态菜单加载、菜单新增/编辑/删除。
- 部门管理：部门树、新增、编辑、删除、负责人、状态、排序。
- 岗位管理：列表、新增、编辑、删除、状态。
- 字典管理：字典类型、字典数据、状态、排序、增删改查。
- 参数配置：列表、保存、删除、状态。
- 日志管理：登录日志、审计日志查询。
- 角色数据权限：`ALL`、`DEPT_AND_CHILD`、`CUSTOM`、`DEPT`、`SELF`。
- 管理端角色页面已支持自定义部门树授权。

### 6. 商品中心

- 商品 SPU 管理：分页、详情、创建、更新、删除。
- 商品上下架/状态更新。
- 商品库存设置和库存增减。
- 商品分类树：列表、详情、新增、更新、删除、状态。
- App 商品列表和商品详情接口。
- 扩展资源接口：品牌、SKU、属性、属性值等支持列表、详情、保存、状态、删除。
- 商品分页接口已返回 `total/pageNo/pageSize/list`。

### 7. 交易中心

- 管理端订单分页、详情、创建、状态更新。
- 会员端下单。
- 会员端订单列表。
- 会员取消订单。
- 会员确认收货。
- 购物车列表、加入购物车、修改数量、删除购物车项。
- 物流列表、订单发货、物流签收。
- 售后列表、会员申请售后、售后审核。
- 订单状态覆盖：创建、支付中、已支付、已发货、已完成、已取消、售后中等。

### 8. 支付中心

- 管理端支付单分页、详情、创建、状态更新。
- 会员端创建支付单。
- 会员端支付单列表。
- 模拟支付成功回调。
- 支付渠道列表、新增、更新。
- 退款单列表、创建退款、模拟退款成功回调。
- 支付成功后联动交易订单状态。
- 退款成功后联动交易订单退款状态。

### 9. 会员中心

- 管理端会员档案列表、保存、启停。
- 会员等级列表。
- 会员地址列表。
- 会员积分/余额流水列表。
- App/Web 会员资料查询。
- App/Web 会员资料更新。
- App/Web 会员等级查询。
- App/Web 地址列表、地址保存、地址删除。
- App/Web 账户流水查询。
- 扩展资源：积分规则、签到记录、会员标签、成长值流水等支持通用管理接口。

### 10. 营销中心

- 优惠券模板列表、详情、创建、更新、启停、删除。
- 管理端会员券列表。
- 会员优惠券列表。
- 会员领取优惠券。
- 优惠券试算。
- 优惠券核销。
- 优惠券作废。
- 扩展资源：活动、折扣规则、秒杀等支持列表、详情、保存、状态、删除。

### 11. 商户中心

- 商户概览。
- 商户档案分页、详情、新增、编辑、审核、启停、删除。
- 商户账号列表、详情、新增、编辑、启停、删除。
- 扩展资源：店铺、费率、结算、提现等支持列表、详情、保存、状态、删除。

### 12. CRM

- CRM 概览。
- 客户列表、详情、新增、编辑、状态变更、转移、删除。
- 跟进记录列表、新增。
- 商机列表、详情、新增、编辑、阶段变更、状态变更、删除。
- 扩展资源：联系人、合同、回款计划、公海等支持列表、详情、保存、状态、删除。

### 13. ERP / 库存

- 库存列表、详情、汇总、低库存查询。
- 库存调整。
- 库存锁定、释放。
- 库存流水列表。
- 盘点单列表、详情、新增、更新、完成、删除。
- 扩展资源：仓库、供应商、采购单、调拨单等支持列表、详情、保存、状态、删除。

### 14. 工作流 BPM

- 已集成 Flowable Process Engine，BPM 不再是手工状态流。
- Flowable 自动维护 `ACT_*` 引擎表，业务侧 `ai_bpm_*` 表作为流程定义、实例、任务镜像。
- 流程定义列表、新增、更新，保存时通过 Flowable `RepositoryService` 部署 BPMN。
- 支持传入自定义 BPMN XML；未传入时自动生成单级审批 BPMN。
- 发起流程实例，使用 Flowable `RuntimeService` 创建真实流程实例，并写入 `engineInstanceId`。
- 流程实例列表，展示业务实例状态和 Flowable 引擎定义/实例 ID。
- 待办任务列表，来自 Flowable `TaskService`，并映射 `engineTaskId`。
- 任务办理，调用 Flowable `TaskService.complete` 推进流程。
- 扩展资源：模型、表单、抄送等支持列表、详情、保存、状态、删除。

### 15. 基础设施 Infra

- 配置状态、配置列表、详情、保存、删除。
- 文件列表、保存、删除。
- 定时任务列表、保存、立即运行。
- 通知公告列表、保存、发布。
- 扩展资源：代码生成表、代码生成字段、存储配置、任务日志等支持列表、详情、保存、状态、删除。

### 16. 小程序 / 公众号 MP

- 小程序用户资料。
- 小程序用户绑定、解绑。
- 菜单列表、保存、发布。
- 素材列表、保存。
- 消息模板列表、保存。
- 模板消息发送。
- 消息发送日志列表。

### 17. 开放平台 OpenAPI

- 开放应用列表、保存、状态变更。
- OpenAPI 调用日志列表。
- Webhook 列表、保存、测试。
- 第三方接口示例：`/openapi/v1/ping`。
- 第三方会员摘要、会员详情、会员账户流水接口。
- 网关侧签名、防重放、限流、审计联动。

### 18. 报表中心

- 经营看板。
- 销售概览。
- 销售状态分布。
- 支付概览。
- 会员概览。
- CRM 概览。
- 商户概览。
- 库存概览。
- 漏斗看板。

### 19. API 契约模块

- `aicloud-api-admin`：管理端用户、角色、分页查询、路径契约。
- `aicloud-api-app`：App 商品、会员、订单创建契约。
- `aicloud-api-web`：Web 订单提交、营销展示契约。
- `aicloud-api-mp`：小程序用户绑定、消息发送契约。
- `aicloud-api-open`：第三方 OpenAPI 签名请求、商品同步、订单状态、Webhook 契约。

### 20. Vue 管理端

- 登录页。
- 动态菜单加载。
- 按钮级权限指令。
- 管理端布局、侧边栏、工作台。
- 系统管理专用页面：用户、角色、菜单、部门、岗位、字典、参数、日志。
- 业务中心页面：会员、商品、营销、交易、支付、商户、CRM、ERP、BPM、报表、开放平台、小程序、基础设施。
- 角色页面支持菜单权限和部门数据权限配置。
- 多页面已接入分页组件。
- 通用业务表格正在逐步替换为专用业务页面，核心系统页和部分业务页已完成专用化。

## 接口文档

服务启动后可通过网关访问聚合文档：

- Knife4j 聚合入口：`http://127.0.0.1:48080/doc.html`
- OpenAPI JSON：`http://127.0.0.1:48080/v3/api-docs`
- 网关地址：`http://127.0.0.1:48080`

推荐测试顺序：

1. 调用 `/auth/login` 获取 `accessToken`。
2. 在 Knife4j 全局参数中填写 `Authorization: Bearer <accessToken>`。
3. 填写 `X-Tenant-Id: 1`。
4. 测试 `/system/menu/tree`、`/system/user/list`、`/product/spu/list`。
5. 再测试交易、支付、会员、营销等业务接口。

## 数据库脚本

- `sql/aicloud-00-schema.sql`：基础表结构。
- `sql/aicloud-01-system.sql`：系统管理初始化数据。
- `sql/aicloud-02-auth.sql`：认证中心初始化数据。
- `sql/aicloud-03-business.sql`：业务模块表结构。
- `sql/aicloud-99-demo.sql`：演示数据。
- `sql/migrations/`：增量迁移脚本。
- `sql/aicloud-all.sql`：全量脚本入口。

当前已新增迁移：

- `V20260525_01_role_dept_and_framework.sql`：新增 `ai_role_dept` 角色部门数据权限表及初始化数据。

## 本地运行

本地环境约定：

- MySQL：`127.0.0.1:3306`，用户名 `root`，密码 `Aa123456`
- Redis：密码 `Aa123456`
- Nacos：用户名 `nacos`，密码 `nacos`
- 数据库：`aicloud`

常用命令：

```bash
# 初始化数据库
./scripts/init-db.sh

# 执行增量迁移
./scripts/migrate-local.sh

# 启动本地服务
./scripts/run-local.sh

# 冒烟测试
./scripts/smoke-test.sh

# 全量构建
mvn -q package -DskipTests

# 阿里 P3C 规范检查
./scripts/coding-check.sh

# 管理端构建
cd aicloud-ui-admin && pnpm build
```

## JDK 21 / AOT / Native 优化

- 虚拟线程：除 Gateway 外，Servlet 业务服务在 `application.yml` 中启用 `spring.threads.virtual.enabled=true`，适合 MyBatis、Redis、HTTP 等阻塞 IO 场景。
- Gateway：基于 WebFlux/Netty，Compose 中显式设置 `SPRING_THREADS_VIRTUAL_ENABLED=false`，避免把虚拟线程当成错误优化点。
- JVM GC：Dockerfile 和 Compose 默认 `JAVA_OPTS` 使用 `-XX:+UseZGC -XX:+ZGenerational`、`MaxRAMPercentage`、UTF-8 和非阻塞随机源。
- AOT 检查：执行 `./scripts/aot-check.sh aicloud-auth/aicloud-auth-server`，默认以认证服务作为试点。
- GraalVM Native：根 POM 提供 `native` profile，建议先从 `auth/openapi/gateway` 这类轻服务试点，不建议第一批处理 BPM/Flowable。
- 普通构建：`mvn package -DskipTests` 不会触发 AOT/Native，不影响日常开发和本地调试。

## Docker 支持

- 网关 Dockerfile：`docker/gateway/Dockerfile`
- 认证服务 Dockerfile：`docker/auth/Dockerfile`
- 业务模块 Dockerfile：`docker/modules/Dockerfile`
- Compose：`docker/docker-compose.yml`
- 容器安全：固定非 root UID `10001`，启用 `no-new-privileges`，丢弃 Linux capabilities，挂载受限 tmpfs 给 `/tmp` 和 `/app/logs`。

当前服务容器命名约定：

- `aicloud-gateway`
- `aicloud-auth`
- `aicloud-system`
- `aicloud-product`
- `aicloud-trade`
- `aicloud-pay`
- `aicloud-member`
- `aicloud-promotion`
- `aicloud-infra`
- `aicloud-merchant`
- `aicloud-crm`
- `aicloud-erp`
- `aicloud-bpm`
- `aicloud-mp`
- `aicloud-openapi`
- `aicloud-report`

## 当前验证状态

最近一次验证通过项：

- `mvn -q package -DskipTests`
- `./scripts/coding-check.sh`
- `cd aicloud-ui-admin && pnpm build`
- 登录接口 `/auth/login`
- 角色详情 `/system/role/1`，已返回 `deptIds`
- 商品分页 `/product/spu/list?pageNo=1&pageSize=2`
- 本地 MySQL 迁移脚本 `scripts/migrate-local.sh`

## 后续重点

- 继续将通用扩展资源页面替换成专用业务页面。
- 将更多 biz 内 DTO 逐步迁移到 `aicloud-api-*` 契约模块。
- 进一步完善数据权限 SQL 过滤策略。
- 增加更多单元测试、集成测试和 Testcontainers 测试。
- 按真实业务规则深化订单库存扣减、优惠券锁定、支付回调幂等、退款流程、BPM 审批链路。

## License

本项目使用 MIT License。
