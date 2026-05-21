<template>
  <section class="dashboard-page">
    <div class="dashboard-hero">
      <div>
        <span class="eyebrow">Operations Cockpit</span>
        <h1>AICloud 管理工作台</h1>
        <p>参照 Yudao/RuoYi 的管理端能力，把系统权限、业务模块、开放平台、接口联调放在一个清晰的入口。</p>
      </div>
      <el-button type="primary" size="large" @click="router.push('/api-docs')">打开接口文档</el-button>
    </div>
    <div class="metric-grid">
      <DataMetric label="菜单节点" :value="flattenMenus(menuStore.menus).length" hint="后端动态加载" />
      <DataMetric label="按钮权限" :value="auth.permissions.length" hint="v-permission 控制" />
      <DataMetric label="业务模块" :value="moduleCatalog.length" hint="可拆可合" />
      <DataMetric label="网关地址" value="48080" hint="统一入口" />
    </div>
    <div class="module-map">
      <article v-for="item in moduleCatalog" :key="item.key" class="module-tile" :style="{ '--accent': item.accent }" @click="go(item.key)">
        <el-icon><component :is="item.icon" /></el-icon>
        <strong>{{ item.title }}</strong>
        <span>{{ item.summary }}</span>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { moduleCatalog } from '@/api/modules'
import DataMetric from '@/components/DataMetric.vue'
import { useAuthStore } from '@/stores/auth'
import { useMenuStore } from '@/stores/menu'
import { flattenMenus } from '@/utils/menu'

const router = useRouter()
const auth = useAuthStore()
const menuStore = useMenuStore()
const pathMap: Record<string, string> = {
  system: '/system/user', infra: '/infra/config', report: '/report/dashboard', product: '/product/spu', trade: '/trade/order', pay: '/pay/order', crm: '/crm/customer', erp: '/erp/inventory', mp: '/mp/menu', openapi: '/openapi/app'
}
function go(key: string) { router.push(pathMap[key] || '/dashboard') }
</script>
