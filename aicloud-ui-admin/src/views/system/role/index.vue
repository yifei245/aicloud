<template>
  <section class="module-page role-admin-page">
    <div class="module-hero" style="--accent: #2f6f5e">
      <div>
        <span class="eyebrow">Permission Admin</span>
        <h1>角色管理</h1>
        <p>维护角色编码、数据权限、菜单授权和启停状态，是按钮级权限控制的核心配置。</p>
      </div>
      <el-icon class="hero-icon"><Key /></el-icon>
    </div>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header user-toolbar">
          <div>
            <strong>角色列表</strong>
            <small>GET /system/role/list</small>
          </div>
          <div class="result-actions user-actions">
            <el-input v-model="query.keyword" clearable placeholder="角色名称 / 编码" prefix-icon="Search" @keyup.enter="loadRoles" />
            <el-select v-model="query.status" clearable placeholder="状态" class="status-filter">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button :loading="loading" @click="loadRoles"><el-icon><Refresh /></el-icon>刷新</el-button>
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增角色</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="roles" stripe border height="560" class="admin-data-table">
        <el-table-column prop="id" label="ID" width="80" fixed />
        <el-table-column prop="name" label="角色名称" min-width="150" fixed />
        <el-table-column prop="code" label="角色编码" min-width="150" />
        <el-table-column prop="dataScope" label="数据范围" min-width="140">
          <template #default="{ row }">{{ dataScopeLabel(row.dataScope) }}</template>
        </el-table-column>
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="status" label="状态" width="112">
          <template #default="{ row }">
            <el-switch :model-value="row.status" :active-value="1" :inactive-value="0" inline-prompt active-text="启" inactive-text="禁" @change="(value: string | number | boolean) => changeStatus(row, Number(value))" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑/授权</el-button>
            <el-button link type="danger" :disabled="row.code === 'SUPER_ADMIN'" @click="removeRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="formVisible" :title="form.id ? '编辑角色' : '新增角色'" size="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="admin-form">
        <el-form-item label="角色名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色编码" prop="code"><el-input v-model="form.code" :disabled="Boolean(form.id)" /></el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope">
            <el-option label="全部数据" value="ALL" />
            <el-option label="本部门及以下" value="DEPT_AND_CHILD" />
            <el-option label="本部门" value="DEPT" />
            <el-option label="仅本人" value="SELF" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="0">禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree ref="menuTreeRef" :data="menus" node-key="id" show-checkbox default-expand-all :props="treeProps" class="permission-tree" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import { request } from '@/utils/request'
import type { MenuNode } from '@/types/api'

interface RoleRow { id: number; code: string; name: string; dataScope: string; sort: number; status: number; userCount: number; menuIds?: number[]; createTime?: string }
interface RoleForm { id?: number; code: string; name: string; dataScope: string; sort: number; status: number; menuIds: number[] }

const loading = ref(false)
const saving = ref(false)
const formVisible = ref(false)
const formRef = ref<FormInstance>()
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const roles = ref<RoleRow[]>([])
const menus = ref<MenuNode[]>([])
const query = reactive<{ keyword: string; status?: number }>({ keyword: '' })
const form = reactive<RoleForm>(emptyForm())
const treeProps = { label: 'name', children: 'children' }
const rules: FormRules<RoleForm> = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }]
}

onMounted(async () => {
  await Promise.all([loadRoles(), loadMenus()])
})

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await request<RoleRow[]>({ url: '/system/role/list', method: 'GET', params: { keyword: query.keyword || undefined, status: query.status } })
  } finally {
    loading.value = false
  }
}

async function loadMenus() {
  menus.value = await request<MenuNode[]>({ url: '/system/menu/tree', method: 'GET' })
}

function openCreate() {
  Object.assign(form, emptyForm())
  formVisible.value = true
  nextTick(() => menuTreeRef.value?.setCheckedKeys([]))
}

async function openEdit(row: RoleRow) {
  const detail = await request<RoleRow>({ url: `/system/role/${row.id}`, method: 'GET' })
  Object.assign(form, { id: detail.id, code: detail.code, name: detail.name, dataScope: detail.dataScope || 'ALL', sort: detail.sort || 0, status: detail.status ?? 1, menuIds: detail.menuIds || [] })
  formVisible.value = true
  nextTick(() => menuTreeRef.value?.setCheckedKeys(form.menuIds, false))
}

async function submitForm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const menuIds = menuTreeRef.value?.getCheckedKeys(false).map(Number) || []
    await request({ url: form.id ? '/system/role/update' : '/system/role/create', method: form.id ? 'PUT' : 'POST', data: { ...form, menuIds } })
    ElMessage.success(form.id ? '角色已更新' : '角色已创建')
    formVisible.value = false
    await loadRoles()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row: RoleRow, status: number) {
  await request({ url: '/system/role/status', method: 'PUT', data: { id: row.id, status } })
  row.status = status
  ElMessage.success(status === 1 ? '角色已启用' : '角色已禁用')
}

async function removeRole(row: RoleRow) {
  await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '删除确认', { type: 'warning' })
  await request({ url: `/system/role/${row.id}`, method: 'DELETE' })
  ElMessage.success('角色已删除')
  await loadRoles()
}

function emptyForm(): RoleForm {
  return { code: '', name: '', dataScope: 'ALL', sort: 0, status: 1, menuIds: [] }
}

function dataScopeLabel(value: string) {
  return ({ ALL: '全部数据', DEPT_AND_CHILD: '本部门及以下', DEPT: '本部门', SELF: '仅本人' } as Record<string, string>)[value] || value
}
</script>
