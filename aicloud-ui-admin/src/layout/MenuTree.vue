<template>
  <template v-for="menu in menus" :key="menu.id">
    <el-sub-menu v-if="visibleChildren(menu).length" :index="normalizePath(menu.path)">
      <template #title>
        <el-icon><component :is="menuIcon(menu.name)" /></el-icon>
        <span>{{ menu.name }}</span>
      </template>
      <MenuTree :menus="visibleChildren(menu)" />
    </el-sub-menu>
    <el-menu-item v-else-if="menu.type !== 3" :index="normalizePath(menu.path)">
      <el-icon><component :is="menuIcon(menu.name)" /></el-icon>
      <span>{{ menu.name }}</span>
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
import type { MenuNode } from '@/types/api'
import { menuIcon, normalizePath } from '@/utils/menu'

defineProps<{ menus: MenuNode[] }>()

function visibleChildren(menu: MenuNode) {
  return (menu.children || []).filter(child => child.type !== 3)
}
</script>
