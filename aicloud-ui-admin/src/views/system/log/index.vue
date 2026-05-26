<template>
  <section class="module-page log-admin-page">
    <div class="module-hero" style="--accent: #31513f">
      <div>
        <span class="eyebrow">System Audit</span>
        <h1>日志管理</h1>
        <p>集中查看系统操作日志与登录日志，支持按用户、模块、终端和执行结果筛选，便于排查权限、登录和业务操作问题。</p>
      </div>
      <el-icon class="hero-icon"><Document /></el-icon>
    </div>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header log-toolbar">
          <div>
            <strong>{{ activeTab === 'operate' ? '操作日志' : '登录日志' }}</strong>
            <small>{{ activeTab === 'operate' ? 'GET /system/log/operate/list' : 'GET /system/log/login/list' }}</small>
          </div>
          <div class="result-actions log-actions">
            <el-select
              v-if="auth.isSuperAdmin"
              v-model="selectedTenantId"
              class="tenant-filter"
              placeholder="租户"
              @change="changeTenantContext"
            >
              <el-option v-for="tenant in tenants" :key="tenant.id" :label="`${tenant.name}（${tenant.id}）`" :value="tenant.id" />
            </el-select>
            <el-input v-model="query.username" clearable placeholder="用户名" prefix-icon="Search" @keyup.enter="loadCurrent" />
            <el-select v-if="activeTab === 'operate'" v-model="query.module" clearable filterable placeholder="模块" class="compact-filter">
              <el-option v-for="item in moduleOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-if="activeTab === 'login'" v-model="query.terminal" clearable placeholder="终端" class="compact-filter">
              <el-option label="管理端" value="ADMIN" />
              <el-option label="用户端" value="APP" />
              <el-option label="Web" value="WEB" />
              <el-option label="小程序" value="MP" />
              <el-option label="开放接口" value="OPENAPI" />
            </el-select>
            <el-select v-model="query.success" clearable placeholder="结果" class="status-filter">
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
            <el-button :loading="loading" @click="loadCurrent"><el-icon><Refresh /></el-icon>刷新</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="log-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="操作日志" name="operate">
          <div class="metric-grid compact log-metrics">
            <DataMetric label="操作总数" :value="operateTotal" hint="当前筛选记录" />
            <DataMetric label="成功操作" :value="operateSuccessCount" hint="success = 1" />
            <DataMetric label="失败操作" :value="operateFailCount" hint="success = 0" />
            <DataMetric label="涉及用户" :value="operateUserCount" hint="去重用户名" />
          </div>

          <el-table v-loading="loading" :data="pagedOperateLogs" stripe border height="560" class="admin-data-table">
            <el-table-column prop="id" label="ID" width="86" fixed />
            <el-table-column prop="tenantId" label="租户" width="86" />
            <el-table-column prop="username" label="用户" min-width="130" fixed />
            <el-table-column prop="module" label="模块" min-width="120" />
            <el-table-column prop="operation" label="操作" min-width="220" show-overflow-tooltip />
            <el-table-column prop="requestMethod" label="方法" width="96">
              <template #default="{ row }"><el-tag>{{ row.requestMethod || '-' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="requestUri" label="请求地址" min-width="260" show-overflow-tooltip />
            <el-table-column prop="requestIp" label="IP" min-width="140" />
            <el-table-column prop="success" label="结果" width="96">
              <template #default="{ row }"><el-tag :type="row.success === 1 ? 'success' : 'danger'">{{ row.success === 1 ? '成功' : '失败' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="错误信息" min-width="220" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" min-width="180" />
          </el-table>
          <div class="table-pagination">
            <span class="pagination-total">共 {{ operateTotal }} 条</span>
            <el-pagination v-model:current-page="operatePage.pageNo" v-model:page-size="operatePage.pageSize" :page-sizes="[10, 20, 50, 100]" :total="operateTotal" layout="sizes, prev, pager, next, jumper" background />
          </div>
        </el-tab-pane>

        <el-tab-pane label="登录日志" name="login">
          <div class="metric-grid compact log-metrics">
            <DataMetric label="登录总数" :value="loginTotal" hint="当前筛选记录" />
            <DataMetric label="登录成功" :value="loginSuccessCount" hint="success = 1" />
            <DataMetric label="登录失败" :value="loginFailCount" hint="success = 0" />
            <DataMetric label="涉及终端" :value="loginTerminalCount" hint="去重终端" />
          </div>

          <el-table v-loading="loading" :data="pagedLoginLogs" stripe border height="560" class="admin-data-table">
            <el-table-column prop="id" label="ID" width="86" fixed />
            <el-table-column prop="tenantId" label="租户" width="86" />
            <el-table-column prop="username" label="用户" min-width="130" fixed />
            <el-table-column prop="userType" label="用户类型" min-width="120" />
            <el-table-column prop="loginTerminal" label="终端" min-width="120">
              <template #default="{ row }"><el-tag>{{ terminalLabel(row.loginTerminal) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="loginIp" label="登录 IP" min-width="150" />
            <el-table-column prop="success" label="结果" width="96">
              <template #default="{ row }"><el-tag :type="row.success === 1 ? 'success' : 'danger'">{{ row.success === 1 ? '成功' : '失败' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="错误信息" min-width="240" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" min-width="180" />
          </el-table>
          <div class="table-pagination">
            <span class="pagination-total">共 {{ loginTotal }} 条</span>
            <el-pagination v-model:current-page="loginPage.pageNo" v-model:page-size="loginPage.pageSize" :page-sizes="[10, 20, 50, 100]" :total="loginTotal" layout="sizes, prev, pager, next, jumper" background />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import DataMetric from '@/components/DataMetric.vue'
import { request } from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import type { TenantOption } from '@/types/api'

interface OperateLogRow {
  id: number
  tenantId: number
  userId?: number
  username?: string
  module?: string
  operation?: string
  requestUri?: string
  requestMethod?: string
  requestIp?: string
  success?: number
  errorMsg?: string
  createTime?: string
}

interface LoginLogRow {
  id: number
  tenantId: number
  userId?: number
  username?: string
  userType?: string
  loginTerminal?: string
  loginIp?: string
  success?: number
  errorMsg?: string
  createTime?: string
}

const auth = useAuthStore()
const loading = ref(false)
const activeTab = ref<'operate' | 'login'>('operate')
const tenants = ref<TenantOption[]>([])
const operateLogs = ref<OperateLogRow[]>([])
const loginLogs = ref<LoginLogRow[]>([])
const query = reactive<{ username: string; module?: string; terminal?: string; success?: number }>({ username: '' })
const operatePage = reactive({ pageNo: 1, pageSize: 20 })
const loginPage = reactive({ pageNo: 1, pageSize: 20 })

const selectedTenantId = computed({
  get: () => auth.tenantId,
  set: value => auth.setActiveTenant(Number(value))
})

const moduleOptions = computed(() => [...new Set(operateLogs.value.map(item => item.module).filter(Boolean) as string[])])
const operateTotal = computed(() => operateLogs.value.length)
const loginTotal = computed(() => loginLogs.value.length)
const operateSuccessCount = computed(() => operateLogs.value.filter(item => item.success === 1).length)
const operateFailCount = computed(() => operateLogs.value.filter(item => item.success === 0).length)
const operateUserCount = computed(() => new Set(operateLogs.value.map(item => item.username).filter(Boolean)).size)
const loginSuccessCount = computed(() => loginLogs.value.filter(item => item.success === 1).length)
const loginFailCount = computed(() => loginLogs.value.filter(item => item.success === 0).length)
const loginTerminalCount = computed(() => new Set(loginLogs.value.map(item => item.loginTerminal).filter(Boolean)).size)
const pagedOperateLogs = computed(() => slicePage(operateLogs.value, operatePage.pageNo, operatePage.pageSize))
const pagedLoginLogs = computed(() => slicePage(loginLogs.value, loginPage.pageNo, loginPage.pageSize))

watch(() => [query.username, query.module, query.terminal, query.success], () => {
  operatePage.pageNo = 1
  loginPage.pageNo = 1
})

onMounted(async () => {
  await loadTenants()
  await loadOperateLogs()
})

async function loadTenants() {
  if (!auth.isSuperAdmin) {
    tenants.value = [{ id: auth.tenantId, name: `租户 ${auth.tenantId}`, code: String(auth.tenantId), status: 1 }]
    return
  }
  tenants.value = await request<TenantOption[]>({ url: '/system/tenant/list', method: 'GET' })
}

async function changeTenantContext() {
  await loadCurrent()
}

async function handleTabChange() {
  await loadCurrent()
}

async function loadCurrent() {
  if (activeTab.value === 'operate') {
    await loadOperateLogs()
    return
  }
  await loadLoginLogs()
}

async function loadOperateLogs() {
  loading.value = true
  try {
    operateLogs.value = await request<OperateLogRow[]>({
      url: '/system/log/operate/list',
      method: 'GET',
      params: {
        username: query.username || undefined,
        module: query.module || undefined,
        success: query.success
      }
    })
  } finally {
    loading.value = false
  }
}

async function loadLoginLogs() {
  loading.value = true
  try {
    loginLogs.value = await request<LoginLogRow[]>({
      url: '/system/log/login/list',
      method: 'GET',
      params: {
        username: query.username || undefined,
        terminal: query.terminal || undefined,
        success: query.success
      }
    })
  } finally {
    loading.value = false
  }
}

function slicePage<T>(rows: T[], pageNo: number, pageSize: number) {
  const start = (pageNo - 1) * pageSize
  return rows.slice(start, start + pageSize)
}

function terminalLabel(value?: string) {
  const map: Record<string, string> = { ADMIN: '管理端', APP: '用户端', WEB: 'Web', MP: '小程序', OPENAPI: '开放接口' }
  return value ? map[value] || value : '-'
}
</script>

<style scoped>
.log-admin-page :deep(.el-card__header) {
  border-bottom: 0;
}

.log-toolbar,
.log-actions {
  gap: 12px;
}

.log-actions .el-input {
  width: 210px;
}

.tenant-filter {
  width: 170px;
}

.compact-filter,
.status-filter {
  width: 132px;
}

.log-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.log-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: rgba(49, 81, 63, 0.14);
}

.log-tabs :deep(.el-tabs__item.is-active) {
  color: #31513f;
  font-weight: 800;
}

.log-tabs :deep(.el-tabs__active-bar) {
  background: #31513f;
}

.log-metrics {
  margin-bottom: 18px;
}

@media (max-width: 960px) {
  .log-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .log-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .log-actions .el-input,
  .tenant-filter,
  .compact-filter,
  .status-filter {
    width: 100%;
  }
}
</style>
