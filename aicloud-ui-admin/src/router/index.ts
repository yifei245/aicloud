import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/AdminLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useMenuStore } from '@/stores/menu'

const baseRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { public: true } },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '工作台' } },
      { path: 'api-docs', name: 'ApiDocs', component: () => import('@/views/dashboard/ApiDocs.vue'), meta: { title: '接口文档' } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/NotFound.vue'), meta: { public: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes: baseRoutes,
  scrollBehavior: () => ({ top: 0 })
})

let dynamicAdded = false

router.beforeEach(async to => {
  const auth = useAuthStore()
  const menu = useMenuStore()
  if (to.meta.public) return true
  if (!auth.isLogin) return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  if (!menu.loaded) {
    const routes = await menu.loadMenus()
    if (!dynamicAdded) {
      routes.forEach(route => router.addRoute('/', route))
      dynamicAdded = true
    }
    return to.fullPath
  }
  return true
})

export default router
