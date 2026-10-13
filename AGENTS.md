# aicloud 开发约束

- 优先满足当前需求的最小实现，复用现有代码，不为假设需求新增抽象。
- 默认只修改后端；未经明确要求不得修改前端，尤其 Vue 文件。
- 仅允许直接连接本地数据库；非本地环境只提供 SQL 或使用用户提供的结果。
- Docker 中间件优先复用本机现有实例。缺少时必须先向用户确认，才能安装或新建。
- 不删除共享或新建的中间件容器、镜像、数据卷；不执行 docker compose down -v 或 prune。
- 不停用、重建、升级其他项目共用的中间件。项目 Compose 只管理 aicloud 应用服务。
- MySQL 使用独立 aicloud 库；发现已存在同名库先检查，不能覆盖。Redis 使用 aicloud 前缀及独立逻辑库；不得 FLUSHALL/FLUSHDB。Nacos 使用 AICLOUD 分组。
