<template>
  <div class="admin-shell">
    <aside class="side-panel" :class="{ collapsed }">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <div v-if="!collapsed" class="brand-copy">
          <strong>AICloud</strong>
          <span>Microservice Admin</span>
        </div>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu :default-active="route.path" router :collapse="collapsed" class="admin-menu">
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon><span>工作台</span>
          </el-menu-item>
          <MenuTree :menus="menus" />
          <el-menu-item index="/api-docs">
            <el-icon><Document /></el-icon><span>接口文档</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </aside>
    <section class="main-panel">
      <header class="topbar">
        <div class="topbar-left">
          <el-button text circle @click="collapsed = !collapsed"><el-icon><Fold /></el-icon></el-button>
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-subtitle">多租户 · 多终端 · 动态权限管理端</div>
          </div>
        </div>
        <div class="topbar-actions">
          <el-select
            v-if="auth.isSuperAdmin"
            v-model="activeTenantId"
            class="tenant-switch"
            size="default"
            placeholder="切换租户"
            @change="changeTenant"
          >
            <el-option v-for="tenant in tenants" :key="tenant.id" :label="`${tenant.name}（${tenant.id}）`" :value="tenant.id" />
          </el-select>
          <span v-else class="tenant-badge">租户 {{ auth.tenantId }}</span>
          <el-button @click="openKnife4j"><el-icon><Link /></el-icon>Knife4j</el-button>
          <el-dropdown @command="handleCommand">
            <button class="user-chip">
              <el-icon><User /></el-icon>
              <span>{{ auth.username }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="content-wrap">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useMenuStore } from '@/stores/menu'
import MenuTree from './MenuTree.vue'
import { request } from '@/utils/request'
import type { TenantOption } from '@/types/api'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const menuStore = useMenuStore()
const { menus } = storeToRefs(menuStore)
const tenants = ref<TenantOption[]>([])
const activeTenantId = computed({
  get: () => auth.tenantId,
  set: value => auth.setActiveTenant(Number(value))
})

const pageTitle = computed(() => String(route.meta.title || 'AICloud 管理平台'))

onMounted(loadTenants)

async function loadTenants() {
  if (!auth.isSuperAdmin) return
  tenants.value = await request<TenantOption[]>({ url: '/system/tenant/list', method: 'GET' })
}

async function changeTenant(value: number) {
  auth.setActiveTenant(Number(value))
  menuStore.reset()
  await menuStore.loadMenus()
}

function openKnife4j() {
  window.open(import.meta.env.VITE_KNIFE4J_URL || 'http://127.0.0.1:48080/doc.html', '_blank')
}

function handleCommand(command: string) {
  if (command === 'logout') {
    auth.logout()
    menuStore.reset()
    router.replace('/login')
  }
}
</script>
