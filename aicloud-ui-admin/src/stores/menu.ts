import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import { getButtonPermissionsApi, getMenuTreeApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import type { MenuNode } from '@/types/api'
import { normalizePath } from '@/utils/menu'

const viewModules = import.meta.glob('../views/**/*.vue')

interface MenuState {
  menus: MenuNode[]
  dynamicRoutes: RouteRecordRaw[]
  loaded: boolean
}

function resolveComponent(component?: string) {
  if (!component || component === '#') return () => import('@/views/dashboard/index.vue')
  const key = `../views/${component}.vue`
  return viewModules[key] || (() => import('@/views/error/NotFound.vue'))
}

function toRoutes(nodes: MenuNode[] = []): RouteRecordRaw[] {
  return nodes
    .filter(node => node.type !== 3)
    .map(node => {
      const path = normalizePath(node.path)
      return {
        path,
        name: `menu-${node.id}`,
        component: node.children?.length ? () => import('@/layout/BlankView.vue') : resolveComponent(node.component),
        meta: { title: node.name, permission: node.permission, menuId: node.id },
        children: toRoutes(node.children || [])
      }
    })
}

export const useMenuStore = defineStore('menu', {
  state: (): MenuState => ({ menus: [], dynamicRoutes: [], loaded: false }),
  actions: {
    async loadMenus() {
      const [menus, permissions] = await Promise.all([getMenuTreeApi(), getButtonPermissionsApi()])
      this.menus = menus || []
      this.dynamicRoutes = toRoutes(this.menus)
      this.loaded = true
      useAuthStore().setPermissions(permissions || [])
      return this.dynamicRoutes
    },
    reset() {
      this.menus = []
      this.dynamicRoutes = []
      this.loaded = false
    }
  }
})
