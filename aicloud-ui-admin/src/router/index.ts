import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/AdminLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useMenuStore } from '@/stores/menu'

const baseRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { public: true } },
  {
    path: '/',
    name: 'Root',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '工作台' } },
      { path: 'api-docs', name: 'ApiDocs', component: () => import('@/views/dashboard/ApiDocs.vue'), meta: { title: '接口文档' } },
      { path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/user/index.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', name: 'SystemMenu', component: () => import('@/views/system/menu/index.vue'), meta: { title: '菜单管理' } },
      { path: 'system/dept', name: 'SystemDept', component: () => import('@/views/system/dept/index.vue'), meta: { title: '部门管理' } },
      { path: 'system/post', name: 'SystemPost', component: () => import('@/views/system/post/index.vue'), meta: { title: '岗位管理' } },
      { path: 'system/dict', name: 'SystemDict', component: () => import('@/views/system/dict/index.vue'), meta: { title: '字典管理' } },
      { path: 'system/config', name: 'SystemConfig', component: () => import('@/views/system/config/index.vue'), meta: { title: '参数配置' } },
      { path: 'system/log', name: 'SystemLog', component: () => import('@/views/system/log/index.vue'), meta: { title: '操作日志' } },
      { path: 'infra/config', name: 'InfraConfig', component: () => import('@/views/infra/config/index.vue'), meta: { title: '基础设施' } },
      { path: 'bpm/process', name: 'BpmProcess', component: () => import('@/views/bpm/process/index.vue'), meta: { title: '工作流' } },
      { path: 'report/dashboard', name: 'ReportDashboard', component: () => import('@/views/report/dashboard/index.vue'), meta: { title: '报表中心' } },
      { path: 'member/profile', name: 'MemberProfile', component: () => import('@/views/member/profile/index.vue'), meta: { title: '会员中心' } },
      { path: 'product/spu', name: 'ProductSpu', component: () => import('@/views/product/spu/index.vue'), meta: { title: '商品中心' } },
      { path: 'promotion/coupon', name: 'PromotionCoupon', component: () => import('@/views/promotion/coupon/index.vue'), meta: { title: '营销中心' } },
      { path: 'trade/order', name: 'TradeOrder', component: () => import('@/views/trade/order/index.vue'), meta: { title: '交易中心' } },
      { path: 'pay/order', name: 'PayOrder', component: () => import('@/views/pay/order/index.vue'), meta: { title: '支付中心' } },
      { path: 'merchant/profile', name: 'MerchantProfile', component: () => import('@/views/merchant/profile/index.vue'), meta: { title: '商户中心' } },
      { path: 'crm/customer', name: 'CrmCustomer', component: () => import('@/views/crm/customer/index.vue'), meta: { title: 'CRM' } },
      { path: 'erp/inventory', name: 'ErpInventory', component: () => import('@/views/erp/inventory/index.vue'), meta: { title: 'ERP' } },
      { path: 'mp/menu', name: 'MpMenu', component: () => import('@/views/mp/menu/index.vue'), meta: { title: '小程序平台' } },
      { path: 'openapi/app', name: 'OpenapiApp', component: () => import('@/views/openapi/app/index.vue'), meta: { title: '开放平台' } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/NotFound.vue') }
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
    try {
      const routes = await menu.loadMenus()
      if (!dynamicAdded) {
        routes.forEach(route => {
          if (!router.hasRoute(String(route.name))) router.addRoute('Root', route)
        })
        dynamicAdded = true
      }
    } catch (error) {
      console.error('加载动态菜单失败', error)
    }
    return to.fullPath
  }
  return true
})

export default router
