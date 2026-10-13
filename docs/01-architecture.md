# aicloud 架构设计

设计日期：2026-09-29；实施修订：2026-09-30。先设计后实施；验收状态单独记录，不以源码存在代替验证通过。

## 1. 目标与范围

建设 Java 21 后端微服务平台，面向管理后台、手机 App、微信小程序、Web 用户端和第三方应用。首版包含完整平台、会员、商城、支付、工作流、CRM、ERP、GoView 报表、AI，并保留参考项目已有扩展业务。IoT 消息与属性历史使用 MySQL，默认 Redis 消息总线；本地 AI 简单向量文件保存到应用 data 目录。仅开发后端；Swagger/Knife4j 作为接口操作入口，不生成前端工程。

目录遵循 ruoyi-vue-pro 的 dependencies/framework/module 分层；微服务部署复用同源 MIT 项目 yudao-cloud master-jdk17 的业务代码，以避免重写成熟业务导致功能缩水。aicloud 的新增及派生代码使用 MIT，保留上游版权；第三方依赖各自遵守原许可证。

## 2. 核心模块与可裁剪模块

| 模块 | 属性 | 职责 |
|---|---|---|
| aicloud-dependencies | 核心 | 统一 BOM、稳定依赖版本 |
| aicloud-framework | 核心 | Web、安全、MyBatis Plus、Redis、租户、数据权限、RPC、日志、幂等限流 |
| aicloud-gateway | 核心 | 外部统一入口、服务路由、身份头清理、认证、文档聚合 |
| aicloud-module-system | 核心 | 管理员、组织、角色、菜单按钮、OAuth2/SSO、客户端、租户、字典、通知、审计 |
| aicloud-module-infra | 核心 | 文件、配置、任务、代码生成、API 日志 |
| aicloud-module-member | 用户端核心 | 会员身份、等级、标签、地址、积分、签到 |
| aicloud-module-mall | 可裁剪 | 商品、促销、交易、购物车、售后、统计 |
| aicloud-module-pay | 可裁剪 | 支付、退款、钱包、渠道、回调重试 |
| aicloud-module-bpm | 可裁剪 | 流程定义、表单、实例、任务、审批 |
| aicloud-module-crm | 可裁剪 | 客户、线索、联系人、商机、合同、回款 |
| aicloud-module-erp | 可裁剪 | 采购、销售、库存、财务基础 |
| aicloud-module-report | 可裁剪 | 报表、可视化查询 |
| aicloud-module-ai | 可裁剪 | 模型、聊天、知识库、工具、工作流 |
| aicloud-module-mp | 可裁剪 | 公众号、菜单、消息、素材 |
| iot/mes/wms/im/hrm/fms/pms/oa | 可裁剪 | 物联网、制造、仓储、通讯、人事、财务、项目、办公 |
| aicloud-server | 可选聚合运行器 | system/infra 单进程调试；完整业务通过独立 server 进程运行 |

模块内部沿用 controller(admin/app)、service、dal(dataobject/mysql)、convert、framework、api 分层。跨服务通过 api/Feign 调用，不跨服务直接操作表。共享一个 MySQL 数据库 aicloud，用表前缀区分业务；事务限于本服务，跨服务采用已有业务补偿/回调逻辑。

## 3. 技术基线

Java 21；Spring Boot 3.5.x；Spring Cloud 2025.0.x；兼容的 Spring Cloud Alibaba；MyBatis Plus；MySQL；Redis；Nacos 服务发现；Spring Security；OpenAPI 3 + Knife4j；Docker Compose。依赖锁定明确版本，不用 latest、SNAPSHOT、RC 或 Beta 作为外部依赖。版本根据官方 Maven Central 元数据核验，以同一兼容系列的稳定版为界。

已核验主版本：Spring Boot 3.5.16、Spring Cloud 2025.0.3、Spring Cloud Alibaba 2025.0.0.0、MyBatis Plus 3.5.17。以用户指定的 Boot/Cloud 系列为边界，不能为了“最新”切换到 Boot 4。JDK 固定使用 21 LTS；本机已有 21.0.5，运行验证使用该版本，不改动共享 JDK。

优先复用现有结构，不引入额外鉴权框架或重复业务抽象。配置只指向本机或本项目 Docker 网络；禁止连接任何非本地数据库。上游环境配置不能直接用于运行，须先替换数据库、Redis、Nacos、消息队列及云账号配置。

## 4. 认证和权限边界

- 管理端 /admin-api/**：管理员身份、租户校验、接口方法权限、角色数据范围。
- 用户端 /app-api/**：会员身份，终端通过 OAuth 客户端区分；手机号密码、短信、社交登录复用业务实现。
- SSO：中心 OAuth2 授权码、一次性兑换、精确回调地址、访问令牌/刷新令牌、撤销和退出；多应用共享中心登录态。公共客户端需审查 PKCE，未验证能力不得标为完成。
- 第三方：以 OAuth2 client_credentials / scope 为基础接入，独立第三方路由与权限约束；不得用管理员 Token 代替应用凭据。
- 网关清除外部传入的身份头；服务端仍必须认证授权，避免通过直连或伪造头绕过。
- 内部 `/rpc-api/**` 使用独立共享凭据，经环境变量配置，网关不提供该路由。Feign 与网关的令牌检查携带内部凭据；RPC 检查不会递归验证用户令牌。
- 刷新令牌只能用于刷新，不能作为 Bearer 凭据。SSO 支持 S256 PKCE；公开客户端无密钥且必须使用 PKCE，机密客户端验证非空密钥。
- 菜单树来源于数据库角色菜单关联，按钮 permission 与后端方法校验一致；前端隐藏按钮不构成授权。
- 测试覆盖未登录、过期令牌、伪造身份头、跨用户类型、缺少权限、SSO 回调、撤销与第三方 scope。

## 5. 数据库与初始化

完整建库脚本创建 aicloud、业务表、索引、基础字典菜单、管理员和客户端。开发初始化与正式升级分离；启动不执行删除已有数据库。第三方凭据不进入源码。提供可重复的全新本地环境验证；测试只使用本地数据库；完整脚本通过已有 MySQL 中的隔离临时库验证。

本机已有 aicloud 库（152 张旧版本表），保留原表及数据。新工程撞名的 `infra_file`、`member_level` 分别改为 `infra_file_record`、`member_user_level`；源码映射与 SQL 同步。工作流引擎使用 `ac_` 表前缀，避免升级或改动旧版本 ACT/FLW 表。初始化脚本先检查表结构，拒绝覆盖不兼容的同名表。

## 6. 部署与可裁剪性

Compose 仅提供 aicloud 应用服务，复用本机已存在的 MySQL、Redis、Nacos。缺少中间件先向用户确认后安装，后续保留，不删除容器/镜像/数据卷。开发默认入口绑定 127.0.0.1；服务使用内部网络。Nacos 注册服务但不依赖远程配置才能启动。核心服务可独立启动；业务服务按 profile 启用，停止业务服务不会破坏系统登录和权限管理。构建 Maven -pl/-am 可选择模块，README 列明模块依赖。

## 7. 验收原则

每项功能区分：已迁移源码、编译通过、单测通过、集成验证、待外部凭据。验收必须记录命令与结果。不能将源码复制、CRUD 空壳、硬编码成功响应当作完整实现。支付等依赖外部服务的功能保留真实调用，缺少凭据时给出明确配置错误。

## 8. 参考

- https://github.com/YunaiV/ruoyi-vue-pro/tree/master-jdk17 — 主要项目结构
- https://github.com/YunaiV/yudao-cloud/tree/master-jdk17 — 同源业务与微服务实现
- https://github.com/pig-mesh/pig — 网关和认证参考
- https://github.com/chillzhuang/SpringBlade — 模块拆分参考
- https://github.com/jeecgboot/JeecgBoot — 基础设施和报表参考
- https://spring.io/projects/spring-cloud — Boot/Cloud 兼容关系
