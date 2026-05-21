import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import type { ApiResponse } from '@/types/api'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

service.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
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
  (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    const message = error.response?.data?.msg || error.message || '网络异常'
    if (status === 401) {
      const auth = useAuthStore()
      auth.logout()
      router.replace(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as Promise<T>
}

export default service
