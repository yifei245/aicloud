import type { MenuNode } from '@/types/api'

export function flattenMenus(nodes: MenuNode[] = []): MenuNode[] {
  return nodes.flatMap(node => [node, ...flattenMenus(node.children || [])])
}

export function normalizePath(path = '') {
  if (!path) return '/'
  return path.startsWith('/') ? path : `/${path}`
}

export function menuIcon(name: string) {
  const map: Record<string, string> = {
    系统管理: 'Setting',
    基础设施: 'Cpu',
    工作流: 'Connection',
    报表中心: 'DataAnalysis',
    会员中心: 'User',
    商品中心: 'Goods',
    营销中心: 'Present',
    交易中心: 'ShoppingCart',
    支付中心: 'Wallet',
    商户中心: 'Shop',
    CRM: 'TrendCharts',
    ERP: 'Box',
    小程序: 'Cellphone',
    开放平台: 'Share'
  }
  return map[name] || 'Menu'
}
