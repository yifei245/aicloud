import { defineStore } from 'pinia'
import { loginApi } from '@/api/auth'
import { storage } from '@/utils/storage'
import type { LoginRequest, LoginResult } from '@/types/api'

interface AuthState {
  accessToken: string
  refreshToken: string
  user: Partial<LoginResult>
  permissions: string[]
  roles: string[]
  activeTenantId: number
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    accessToken: storage.get('access_token', ''),
    refreshToken: storage.get('refresh_token', ''),
    user: storage.get('user', {}),
    permissions: storage.get('permissions', []),
    roles: storage.get('roles', []),
    activeTenantId: storage.get('active_tenant_id', storage.get<Partial<LoginResult>>('user', {}).tenantId || 1)
  }),
  getters: {
    isLogin: state => Boolean(state.accessToken),
    username: state => state.user.nickname || state.user.username || '管理员',
    tenantId: state => state.activeTenantId || state.user.tenantId || 1,
    isSuperAdmin: state => state.roles.includes('SUPER_ADMIN')
  },
  actions: {
    async login(payload: LoginRequest) {
      const data = await loginApi(payload)
      this.setSession(data)
      return data
    },
    setSession(data: LoginResult) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data
      this.permissions = data.permissions || []
      this.roles = data.roles || []
      this.activeTenantId = data.tenantId || this.activeTenantId || 1
      storage.set('access_token', this.accessToken)
      storage.set('refresh_token', this.refreshToken)
      storage.set('user', this.user)
      storage.set('permissions', this.permissions)
      storage.set('roles', this.roles)
      storage.set('active_tenant_id', this.activeTenantId)
    },
    setActiveTenant(tenantId: number) {
      this.activeTenantId = tenantId || this.user.tenantId || 1
      storage.set('active_tenant_id', this.activeTenantId)
    },
    setPermissions(permissions: string[]) {
      this.permissions = permissions
      storage.set('permissions', permissions)
    },
    hasPermission(permission?: string) {
      if (!permission) return true
      return this.permissions.includes(permission) || this.roles.includes('SUPER_ADMIN') || this.roles.includes('ADMIN')
    },
    logout() {
      this.accessToken = ''
      this.refreshToken = ''
      this.user = {}
      this.permissions = []
      this.roles = []
      this.activeTenantId = 1
      storage.clear()
    }
  }
})
