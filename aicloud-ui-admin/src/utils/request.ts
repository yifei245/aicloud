import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import type { ApiResponse, LoginResult } from '@/types/api'

const baseURL = import.meta.env.VITE_API_BASE || '/api'
const service = axios.create({ baseURL, timeout: 15000 })
const refreshClient = axios.create({ baseURL, timeout: 15000 })
let refreshing: Promise<LoginResult> | null = null

service.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.accessToken) config.headers.Authorization = `Bearer ${auth.accessToken}`
  if (auth.tenantId) config.headers['X-Tenant-Id'] = String(auth.tenantId)
  config.headers['X-Request-From'] = 'aicloud-ui-admin'
  return config
})

service.interceptors.response.use(
  (response: any): any => {
    const payload = response.data as ApiResponse<unknown>
    if (payload && typeof payload.code === 'number' && payload.code !== 0) {
      ElMessage.error(payload.msg || '接口请求失败')
      return Promise.reject(payload)
    }
    return payload?.data ?? payload
  },
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    const original = error.config as (AxiosRequestConfig & { _retry?: boolean }) | undefined
    if (status === 401 && original && !original._retry && !String(original.url || '').includes('/auth/login')) {
      const auth = useAuthStore()
      if (auth.refreshToken) {
        try {
          original._retry = true
          const data = await refreshSession(auth.refreshToken)
          auth.setSession(data)
          original.headers = { ...(original.headers || {}), Authorization: `Bearer ${data.accessToken}`, 'X-Tenant-Id': String(auth.tenantId || data.tenantId || 1) }
          return service.request(original)
        } catch {
          auth.logout()
          router.replace(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
          ElMessage.error('登录已过期，请重新登录')
          return Promise.reject(error)
        }
      }
      auth.logout()
      router.replace(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
    }
    const message = error.response?.data?.msg || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

function refreshSession(refreshToken: string) {
  if (!refreshing) {
    refreshing = refreshClient.post<ApiResponse<LoginResult>>('/auth/token/refresh', { refreshToken })
      .then(response => {
        if (response.data.code !== 0) throw new Error(response.data.msg || 'refresh token 无效')
        return response.data.data
      })
      .finally(() => { refreshing = null })
  }
  return refreshing
}

export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as Promise<T>
}

export default service
