import { request } from '@/utils/request'
import type { LoginRequest, LoginResult, MenuNode } from '@/types/api'

export function loginApi(data: LoginRequest) {
  return request<LoginResult>({ url: '/auth/login', method: 'POST', data })
}

export function refreshTokenApi(refreshToken: string) {
  return request<LoginResult>({ url: '/auth/token/refresh', method: 'POST', data: { refreshToken } })
}

export function getMenuTreeApi() {
  return request<MenuNode[]>({ url: '/system/menu/tree', method: 'GET' })
}

export function getButtonPermissionsApi() {
  return request<string[]>({ url: '/system/permission/buttons', method: 'GET' })
}

export function checkPermissionApi(permission: string) {
  return request<boolean>({ url: '/auth/permission/check', method: 'GET', params: { permission } })
}
