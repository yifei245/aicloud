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
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useMenuStore } from '@/stores/menu'
import MenuTree from './MenuTree.vue'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const menuStore = useMenuStore()
const { menus } = storeToRefs(menuStore)

const pageTitle = computed(() => String(route.meta.title || 'AICloud 管理平台'))

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
