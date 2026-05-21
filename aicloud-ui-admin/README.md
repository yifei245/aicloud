# AICloud 管理端

参照 ruoyi-vue-pro / yudao-cloud 管理端思路实现的 Vue3 管理后台，和当前 AICloud 后端 Gateway 联调。

## 技术栈

- Vue 3 + Vite + TypeScript
- Element Plus + Icons
- Pinia 状态管理
- Vue Router 动态路由
- Axios 请求封装
- 动态菜单：`GET /system/menu/tree`
- 按钮权限：`GET /system/permission/buttons` + `v-permission`
- API 文档：Knife4j 聚合入口

## 目录结构

```text
aicloud-ui-admin
├── src/api              # 后端接口封装与模块端点目录
├── src/assets/styles    # 全局视觉样式
├── src/components       # 通用模块工作台、指标卡片
├── src/directives       # v-permission 权限指令
├── src/layout           # 后台主布局、动态菜单
├── src/router           # 静态路由 + 动态菜单路由注入
├── src/stores           # 登录态、菜单权限状态
├── src/utils            # request/storage/menu 工具
└── src/views            # 登录、工作台、系统与业务模块页面
```

## 默认登录

- 地址：`http://127.0.0.1:5777`
- 账号：`admin`
- 密码：`123456`
- 租户：`1`

## 启动

```bash
pnpm install
pnpm dev
```

默认代理：`/api -> http://127.0.0.1:48080`。

## 构建

```bash
pnpm build
```

## 已实现能力

- 登录后保存 token，自动注入 `Authorization: Bearer <token>`。
- 动态加载后端菜单，并按后端 `component` 字段映射页面。
- 获取按钮权限并提供 `v-permission` 指令。
- 工作台展示菜单数、权限数、模块入口。
- 各业务模块页面可直接点击调用后端接口并展示 JSON 返回。
- 内嵌 Knife4j 聚合文档入口。
