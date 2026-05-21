# Nacos 配置模板

## 1. 建议命名
- Data ID: `aicloud-gateway.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-auth.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-system.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-member.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-mp.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-openapi.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-infra.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-product.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-trade.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-pay.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-promotion.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-merchant.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-crm.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-erp.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-bpm.yaml`，Group: `DEFAULT_GROUP`
- Data ID: `aicloud-module-report.yaml`，Group: `DEFAULT_GROUP`

## 2. aicloud-gateway.yaml
```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0
  cloud:
    gateway:
      routes:
        - id: aicloud-auth
          uri: http://127.0.0.1:48081
          predicates:
            - Path=/auth/**
        - id: aicloud-system
          uri: http://127.0.0.1:49010
          predicates:
            - Path=/system/**
        - id: aicloud-member
          uri: http://127.0.0.1:49012
          predicates:
            - Path=/app/**,/web/**
        - id: aicloud-mp
          uri: http://127.0.0.1:49013
          predicates:
            - Path=/mp/**
        - id: aicloud-openapi
          uri: http://127.0.0.1:49023
          predicates:
            - Path=/openapi/**
        - id: aicloud-infra
          uri: http://127.0.0.1:49011
          predicates:
            - Path=/infra/**
        - id: aicloud-product
          uri: http://127.0.0.1:49017
          predicates:
            - Path=/product/**
        - id: aicloud-trade
          uri: http://127.0.0.1:49014
          predicates:
            - Path=/trade/**
        - id: aicloud-pay
          uri: http://127.0.0.1:49015
          predicates:
            - Path=/pay/**
        - id: aicloud-promotion
          uri: http://127.0.0.1:49016
          predicates:
            - Path=/promotion/**
        - id: aicloud-merchant
          uri: http://127.0.0.1:49018
          predicates:
            - Path=/merchant/**
        - id: aicloud-crm
          uri: http://127.0.0.1:49019
          predicates:
            - Path=/crm/**
        - id: aicloud-erp
          uri: http://127.0.0.1:49020
          predicates:
            - Path=/erp/**
        - id: aicloud-bpm
          uri: http://127.0.0.1:49021
          predicates:
            - Path=/bpm/**
        - id: aicloud-report
          uri: http://127.0.0.1:49022
          predicates:
            - Path=/report/**

aicloud:
  gateway:
    openapi:
      enabled: true
      max-time-drift-seconds: 300
      max-future-seconds: 30
      replay-protect-enabled: true
      nonce-ttl-seconds: 300
      min-nonce-window-seconds: 60
      rate-limit-enabled: true
      requests-per-minute: 120
      redis-guard-enabled: true
    audit:
      archive:
        enabled: true
        retention-days: 30
        batch-size: 1000
        cron: "0 15 3 * * ?"
      export:
        cleanup:
          enabled: true
          retention-days: 7
          batch-size: 500
          cron: "0 40 3 * * ?"
    permission-rules:
      # 高优先级 deny 规则：禁止 MEMBER 访问全部 system 管理端路径
      - name: deny-member-system
        path: /system/**
        effect: DENY
        priority: 100
        user-types: [MEMBER]
        enabled: true
      # 高优先级 deny 规则：禁止 MEMBER 访问网关管理路径
      - name: deny-member-gateway-admin
        path: /gateway/**
        effect: DENY
        priority: 100
        user-types: [MEMBER]
        enabled: true
      - name: allow-admin-audit-stats
        method: GET
        path: /gateway/audit/stats
        permission: system:role:assign
        effect: ALLOW
        priority: 20
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-audit-logs
        method: GET
        path: /gateway/audit/logs
        permission: system:role:assign
        effect: ALLOW
        priority: 20
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-audit-logs-export
        method: GET
        path: /gateway/audit/logs/export
        permission: system:role:assign
        effect: ALLOW
        priority: 20
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-audit-export-create
        method: POST
        path: /gateway/audit/export/tasks
        permission: system:role:assign
        effect: ALLOW
        priority: 20
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-audit-export-query
        method: GET
        path: /gateway/audit/export/tasks/**
        permission: system:role:assign
        effect: ALLOW
        priority: 20
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-audit-export-list
        method: GET
        path: /gateway/audit/export/tasks/list
        permission: system:role:assign
        effect: ALLOW
        priority: 21
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-audit-export-cleanup-run
        method: POST
        path: /gateway/audit/export/tasks/cleanup/run
        permission: system:role:assign
        effect: ALLOW
        priority: 20
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-audit-archive-run
        method: POST
        path: /gateway/audit/archive/run
        permission: system:role:assign
        effect: ALLOW
        priority: 20
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-member-web-profile
        method: GET
        path: /web/member/profile
        effect: ALLOW
        priority: 8
        terminals: [WEB]
        user-types: [MEMBER]
        enabled: true
      - name: allow-member-app-profile
        method: GET
        path: /app/member/profile
        effect: ALLOW
        priority: 8
        terminals: [APP]
        user-types: [MEMBER]
        enabled: true
      - name: allow-member-mp-profile
        method: GET
        path: /mp/user/profile
        effect: ALLOW
        priority: 8
        terminals: [MP]
        user-types: [MEMBER]
        enabled: true
      - name: allow-openapi-ping
        method: GET
        path: /openapi/v1/ping
        effect: ALLOW
        priority: 8
        terminals: [OPENAPI]
        user-types: [OPENAPI]
        enabled: true
      - name: allow-openapi-member-summary
        method: GET
        path: /openapi/v1/member/summary
        effect: ALLOW
        priority: 8
        terminals: [OPENAPI]
        user-types: [OPENAPI]
        enabled: true
      - name: allow-admin-infra
        path: /infra/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-product
        path: /product/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-trade
        path: /trade/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-pay
        path: /pay/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-promotion
        path: /promotion/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-merchant
        path: /merchant/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-crm
        path: /crm/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-erp
        path: /erp/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-bpm
        path: /bpm/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-report
        path: /report/**
        effect: ALLOW
        priority: 8
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      # 管理端严格策略（ADMIN 终端 + ADMIN 用户类型）
      - name: allow-admin-menu-tree
        method: GET
        path: /system/menu/tree
        permission: system:menu:list
        effect: ALLOW
        priority: 10
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-permission-buttons
        method: GET
        path: /system/permission/buttons
        permission: system:role:assign
        effect: ALLOW
        priority: 10
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-user-list
        method: GET
        path: /system/user/list
        permission: system:user:query
        effect: ALLOW
        priority: 10
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-user-create
        method: POST
        path: /system/user/create
        permission: system:user:create
        effect: ALLOW
        priority: 10
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      - name: allow-admin-user-update
        method: PUT
        path: /system/user/update
        permission: system:user:update
        effect: ALLOW
        priority: 10
        terminals: [ADMIN]
        user-types: [ADMIN]
        enabled: true
      # 用户端宽松策略：APP/MP/WEB 不配置路由级规则
```

## 3. aicloud-auth.yaml
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/aicloud?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

## 4. aicloud-module-system.yaml
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/aicloud?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

## 5. aicloud-module-member.yaml
```yaml
spring:
  application:
    name: aicloud-module-member
```

## 6. aicloud-module-mp.yaml
```yaml
spring:
  application:
    name: aicloud-module-mp
```

## 7. aicloud-module-openapi.yaml
```yaml
spring:
  application:
    name: aicloud-module-openapi
```

## 8. 动态刷新说明
- 网关已启用 `aicloud.gateway.permission-rules` 动态刷新（`@RefreshScope` + Nacos config refresh）。
- 在 Nacos 修改 `aicloud-gateway.yaml` 后，通常会自动生效，无需重启网关。
- `permission-rules` 支持可选灰度字段：
  - `terminals`: 仅匹配指定登录终端（如 `ADMIN/WEB/APP/MP/OPENAPI`）
  - `userTypes`: 仅匹配指定用户类型（如 `ADMIN/MEMBER`）
- `permission-rules` 规则控制字段：
  - `effect`: `ALLOW` 或 `DENY`（默认 `ALLOW`）
  - `priority`: 数值越大优先级越高（默认 `0`）
  - `enabled`: 是否启用规则（默认 `true`）
- 审计归档配置：
  - `aicloud.gateway.audit.archive.enabled`: 是否启用自动归档
  - `aicloud.gateway.audit.archive.retention-days`: 保留天数（默认 30）
  - `aicloud.gateway.audit.archive.batch-size`: 每批处理条数（默认 1000）
  - `aicloud.gateway.audit.archive.cron`: 定时任务表达式（默认每天 03:15）
- 导出任务清理配置：
  - `aicloud.gateway.audit.export.cleanup.enabled`: 是否启用自动清理
  - `aicloud.gateway.audit.export.cleanup.retention-days`: 导出任务保留天数（默认 7）
  - `aicloud.gateway.audit.export.cleanup.batch-size`: 每批清理条数（默认 500）
  - `aicloud.gateway.audit.export.cleanup.cron`: 定时任务表达式（默认每天 03:40）
- OpenAPI 安全配置：
  - `aicloud.gateway.openapi.enabled`: 是否启用 OpenAPI 网关签名鉴权
  - `aicloud.gateway.openapi.max-time-drift-seconds`: 允许的时间戳漂移秒数（默认 300）
  - `aicloud.gateway.openapi.max-future-seconds`: 允许的未来时间戳偏移秒数（默认 30）
  - `aicloud.gateway.openapi.replay-protect-enabled`: 是否启用 nonce 防重放（默认 true）
  - `aicloud.gateway.openapi.nonce-ttl-seconds`: nonce 保留秒数（默认 300）
  - `aicloud.gateway.openapi.min-nonce-window-seconds`: nonce 最小防重放窗口秒数（默认 60）
  - `aicloud.gateway.openapi.rate-limit-enabled`: 是否启用按 appKey 限流（默认 true）
  - `aicloud.gateway.openapi.requests-per-minute`: 每个 appKey 每分钟请求上限（默认 120）
  - `aicloud.gateway.openapi.redis-guard-enabled`: 是否启用 Redis 分布式防重放与限流（默认 true）
- 默认规则文件：`docs/deploy/gateway-permission-rules-default.yaml`
