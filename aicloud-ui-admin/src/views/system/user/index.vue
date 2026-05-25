<template>
  <section class="module-page user-admin-page">
    <div class="module-hero" style="--accent: #1c4c5b">
      <div>
        <span class="eyebrow">System Admin</span>
        <h1>用户管理</h1>
        <p>管理后台用户、会员用户、部门岗位、角色权限和登录状态。支持新增、编辑、启停、重置密码和删除。</p>
      </div>
      <el-icon class="hero-icon"><User /></el-icon>
    </div>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header user-toolbar">
          <div>
            <strong>用户列表</strong>
            <small>GET /system/user/list</small>
          </div>
          <div class="result-actions user-actions">
            <el-select
              v-if="auth.isSuperAdmin"
              v-model="selectedTenantId"
              class="tenant-filter"
              placeholder="租户"
              @change="changeTenantContext"
            >
              <el-option v-for="tenant in tenants" :key="tenant.id" :label="`${tenant.name}（${tenant.id}）`" :value="tenant.id" />
            </el-select>
            <el-input v-model="query.keyword" clearable placeholder="用户名 / 昵称 / 手机号" prefix-icon="Search" @keyup.enter="loadUsers" />
            <el-select v-model="query.status" clearable placeholder="状态" class="status-filter">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button :loading="loading" @click="loadUsers"><el-icon><Refresh /></el-icon>刷新</el-button>
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增用户</el-button>
          </div>
        </div>
      </template>

      <div class="metric-grid compact user-metrics">
        <DataMetric label="用户总数" :value="userTotal" hint="当前筛选记录" />
        <DataMetric label="启用用户" :value="enabledCount" hint="status = 1" />
        <DataMetric label="管理员" :value="adminCount" hint="userType = ADMIN" />
        <DataMetric label="会员用户" :value="memberCount" hint="userType = MEMBER" />
      </div>

      <el-table v-loading="loading" :data="pagedUsers" stripe border height="560" class="admin-data-table">
        <el-table-column prop="id" label="ID" width="76" fixed />
        <el-table-column prop="tenantId" label="租户" width="86" />
        <el-table-column prop="username" label="用户名" min-width="130" fixed />
        <el-table-column prop="nickname" label="昵称" min-width="130" />
        <el-table-column prop="mobile" label="手机号" min-width="145" />
        <el-table-column prop="email" label="邮箱" min-width="190" show-overflow-tooltip />
        <el-table-column prop="userType" label="用户类型" width="116">
          <template #default="{ row }">
            <el-tag :type="row.userType === 'ADMIN' ? 'success' : 'info'">{{ userTypeLabel(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="112">
          <template #default="{ row }">
            <el-switch :model-value="row.status" :active-value="1" :inactive-value="0" inline-prompt active-text="启" inactive-text="禁" @change="(value: string | number | boolean) => changeStatus(row, Number(value))" />
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="部门" min-width="120" />
        <el-table-column label="岗位" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ formatArray(row.postNames) }}</template>
        </el-table-column>
        <el-table-column label="角色" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ formatArray(row.roleCodes) }}</template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="180" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openResetPassword(row)">重置密码</el-button>
            <el-button link type="danger" :disabled="row.username === 'admin'" @click="removeUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-pagination">
        <span class="pagination-total">共 {{ userTotal }} 条</span>
        <el-pagination v-model:current-page="pagination.pageNo" v-model:page-size="pagination.pageSize" :page-sizes="[10, 20, 50, 100]" :total="userTotal" layout="sizes, prev, pager, next, jumper" background @size-change="handlePageSizeChange" @current-change="loadUsers" />
      </div>
    </el-card>

    <el-drawer v-model="formVisible" :title="form.id ? '编辑用户' : '新增用户'" size="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="94px" class="admin-form">
        <el-form-item label="归属租户">
          <el-select
            v-if="auth.isSuperAdmin"
            v-model="selectedTenantId"
            :disabled="Boolean(form.id)"
            placeholder="选择新增用户归属租户"
            @change="changeTenantContext"
          >
            <el-option v-for="tenant in tenants" :key="tenant.id" :label="`${tenant.name}（${tenant.id}）`" :value="tenant.id" />
          </el-select>
          <el-tag v-else>租户 {{ auth.tenantId }}</el-tag>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="Boolean(form.id)" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="默认建议 123456" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio-button label="ADMIN">管理员</el-radio-button>
            <el-radio-button label="MEMBER">会员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="0">禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" clearable filterable placeholder="选择部门">
            <el-option v-for="dept in deptOptions" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位">
          <el-select v-model="form.postIds" multiple clearable filterable placeholder="选择岗位">
            <el-option v-for="post in postOptions" :key="post.id" :label="post.name" :value="post.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple clearable filterable placeholder="选择角色">
            <el-option v-for="role in roleOptions" :key="role.id" :label="`${role.name}（${role.code}）`" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="passwordVisible" title="重置密码" width="420px">
      <el-form :model="passwordForm" label-width="86px">
        <el-form-item label="用户">
          <el-input :model-value="passwordForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import DataMetric from '@/components/DataMetric.vue'
import { request } from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import type { TenantOption } from '@/types/api'

interface UserRow {
  id: number
  tenantId: number
  username: string
  nickname: string
  mobile: string
  email: string
  userType: string
  status: number
  deptId?: number
  deptName: string
  postIds?: number[]
  postNames?: string[]
  roleIds?: number[]
  roleCodes?: string[]
  lastLoginTime?: string
  createTime?: string
}
interface RoleOption { id: number; name: string; code: string; status: number }
interface PostOption { id: number; name: string; code: string; status: number }
interface DeptOption { id: number; name: string; children?: DeptOption[] }
interface UserForm {
  id?: number
  tenantId?: number
  username: string
  password?: string
  nickname: string
  mobile: string
  email: string
  userType: string
  status: number
  deptId?: number
  postIds: number[]
  roleIds: number[]
}

const loading = ref(false)
const saving = ref(false)
const formVisible = ref(false)
const passwordVisible = ref(false)
const formRef = ref<FormInstance>()
const auth = useAuthStore()
const users = ref<UserRow[]>([])
const tenants = ref<TenantOption[]>([])
const roleOptions = ref<RoleOption[]>([])
const postOptions = ref<PostOption[]>([])
const deptOptions = ref<DeptOption[]>([])
const query = reactive<{ keyword: string; status?: number }>({ keyword: '' })
const pagination = reactive({ pageNo: 1, pageSize: 20 })
const form = reactive<UserForm>(emptyForm())
const passwordForm = reactive({ id: 0, username: '', password: '123456' })

const rules: FormRules<UserForm> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const userTotal = ref(0)
const pagedUsers = computed(() => users.value)
const enabledCount = computed(() => users.value.filter(item => item.status === 1).length)
const adminCount = computed(() => users.value.filter(item => item.userType === 'ADMIN').length)
const memberCount = computed(() => users.value.filter(item => item.userType === 'MEMBER').length)
const selectedTenantId = computed({
  get: () => auth.tenantId,
  set: value => auth.setActiveTenant(Number(value))
})

watch(() => [query.keyword, query.status], () => { pagination.pageNo = 1 })

onMounted(async () => {
  await loadTenants()
  await Promise.all([loadOptions(), loadUsers()])
})

async function loadTenants() {
  if (!auth.isSuperAdmin) {
    tenants.value = [{ id: auth.tenantId, name: `租户 ${auth.tenantId}`, code: String(auth.tenantId), status: 1 }]
    return
  }
  tenants.value = await request<TenantOption[]>({ url: '/system/tenant/list', method: 'GET' })
}

async function changeTenantContext() {
  if (!auth.isSuperAdmin) return
  if (!form.id) Object.assign(form, emptyForm())
  await Promise.all([loadOptions(), loadUsers()])
}

async function loadUsers() {
  loading.value = true
  try {
    const data = await request<{ total: number; list: UserRow[] }>({
      url: '/system/user/list',
      method: 'GET',
      params: { keyword: query.keyword || undefined, status: query.status, pageNo: pagination.pageNo, pageSize: pagination.pageSize }
    })
    users.value = data.list || []
    userTotal.value = data.total || users.value.length
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [roles, posts, depts] = await Promise.all([
    request<RoleOption[]>({ url: '/system/role/list', method: 'GET' }),
    request<PostOption[]>({ url: '/system/post/list', method: 'GET' }),
    request<DeptOption[]>({ url: '/system/dept/tree', method: 'GET' })
  ])
  roleOptions.value = roles || []
  postOptions.value = posts || []
  deptOptions.value = flattenDept(depts || [])
}

function handlePageSizeChange() {
  pagination.pageNo = 1
  loadUsers()
}

function openCreate() {
  Object.assign(form, emptyForm())
  formVisible.value = true
}

async function openEdit(row: UserRow) {
  const detail = await request<UserRow>({ url: `/system/user/${row.id}`, method: 'GET' })
  Object.assign(form, {
    id: detail.id,
    tenantId: detail.tenantId,
    username: detail.username,
    nickname: detail.nickname,
    mobile: detail.mobile,
    email: detail.email,
    userType: detail.userType || 'ADMIN',
    status: detail.status ?? 1,
    deptId: detail.deptId,
    postIds: detail.postIds || [],
    roleIds: detail.roleIds || []
  })
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form, password: form.id ? undefined : form.password }
    await request({ url: form.id ? '/system/user/update' : '/system/user/create', method: form.id ? 'PUT' : 'POST', data: payload })
    ElMessage.success(form.id ? '用户已更新' : '用户已创建')
    formVisible.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row: UserRow, status: number) {
  await request({ url: '/system/user/status', method: 'PUT', data: { id: row.id, status } })
  ElMessage.success(status === 1 ? '用户已启用' : '用户已禁用')
  row.status = status
}

function openResetPassword(row: UserRow) {
  Object.assign(passwordForm, { id: row.id, username: row.username, password: '123456' })
  passwordVisible.value = true
}

async function submitPassword() {
  if (!passwordForm.password) {
    ElMessage.warning('请输入新密码')
    return
  }
  saving.value = true
  try {
    await request({ url: '/system/user/reset-password', method: 'PUT', data: { id: passwordForm.id, password: passwordForm.password } })
    ElMessage.success('密码已重置')
    passwordVisible.value = false
  } finally {
    saving.value = false
  }
}

async function removeUser(row: UserRow) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？此操作不可恢复。`, '删除确认', { type: 'warning' })
  await request({ url: `/system/user/${row.id}`, method: 'DELETE' })
  ElMessage.success('用户已删除')
  await loadUsers()
}

function emptyForm(): UserForm {
  return { tenantId: auth.tenantId, username: '', password: '123456', nickname: '', mobile: '', email: '', userType: 'ADMIN', status: 1, postIds: [], roleIds: [] }
}

function flattenDept(nodes: DeptOption[], prefix = ''): DeptOption[] {
  return nodes.flatMap(node => {
    const label = prefix ? `${prefix} / ${node.name}` : node.name
    return [{ ...node, name: label }, ...flattenDept(node.children || [], label)]
  })
}

function formatArray(value?: string[]) {
  return value?.length ? value.join('、') : '-'
}

function userTypeLabel(value: string) {
  return value === 'ADMIN' ? '管理员' : '会员'
}
</script>
