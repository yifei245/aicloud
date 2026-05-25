export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface LoginRequest {
  tenantId?: number
  username: string
  password: string
  terminal: 'ADMIN'
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  tokenType?: string
  expiresIn?: number
  expireAt?: string
  refreshExpireAt?: string
  tenantId?: number
  userId?: number
  username?: string
  nickname?: string
  userType?: string
  roles?: string[]
  permissions?: string[]
}

export interface MenuNode {
  id: number
  parentId: number
  name: string
  type: number
  path: string
  component: string
  permission?: string
  children?: MenuNode[]
}

export interface TenantOption {
  id: number
  name: string
  code: string
  status: number
  expireTime?: string
}
