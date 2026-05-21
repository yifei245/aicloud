<template>
  <section class="module-page dept-admin-page">
    <div class="module-hero" style="--accent: #365314">
      <div>
        <span class="eyebrow">Organization Admin</span>
        <h1>部门管理</h1>
        <p>维护组织树、负责人、排序和启停状态，支撑用户归属、数据权限和组织维度管理。</p>
      </div>
      <el-icon class="hero-icon"><OfficeBuilding /></el-icon>
    </div>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header user-toolbar">
          <div><strong>部门树</strong><small>GET /system/dept/tree</small></div>
          <div class="result-actions user-actions">
            <el-button :loading="loading" @click="loadDepts"><el-icon><Refresh /></el-icon>刷新</el-button>
            <el-button type="primary" @click="openCreate()"><el-icon><Plus /></el-icon>新增部门</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="depts" row-key="id" stripe border height="560" class="admin-data-table" default-expand-all>
        <el-table-column prop="name" label="部门名称" min-width="220" fixed />
        <el-table-column prop="leaderNickname" label="负责人" min-width="120" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCreate(row)">新增下级</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeDept(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑部门' : '新增部门'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="上级部门"><el-tree-select v-model="form.parentId" :data="deptSelectOptions" node-key="id" :props="treeProps" check-strictly clearable placeholder="不选则为顶级部门" /></el-form-item>
        <el-form-item label="部门名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="负责人"><el-select v-model="form.leaderUserId" clearable filterable placeholder="选择负责人"><el-option v-for="user in users" :key="user.id" :label="user.nickname || user.username" :value="user.id" /></el-select></el-form-item>
        <el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio-button :label="1">启用</el-radio-button><el-radio-button :label="0">禁用</el-radio-button></el-radio-group></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/utils/request'

interface DeptRow { id?: number; parentId?: number; name: string; leaderUserId?: number; leaderNickname?: string; sort: number; status: number; children?: DeptRow[] }
interface DeptForm { id?: number; parentId?: number; name: string; leaderUserId?: number; sort: number; status: number }
interface UserOption { id: number; username: string; nickname: string }

const loading = ref(false)
const saving = ref(false)
const formVisible = ref(false)
const formRef = ref<FormInstance>()
const depts = ref<DeptRow[]>([])
const users = ref<UserOption[]>([])
const form = reactive<DeptForm>(emptyForm())
const treeProps = { label: 'name', children: 'children' }
const rules: FormRules<DeptForm> = { name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }
const deptSelectOptions = computed(() => depts.value)

onMounted(async () => { await Promise.all([loadDepts(), loadUsers()]) })

async function loadDepts() {
  loading.value = true
  try { depts.value = await request<DeptRow[]>({ url: '/system/dept/tree', method: 'GET' }) } finally { loading.value = false }
}
async function loadUsers() {
  const data = await request<{ list: UserOption[] }>({ url: '/system/user/list', method: 'GET' })
  users.value = data.list || []
}
function openCreate(parent?: DeptRow) { Object.assign(form, emptyForm(), { parentId: parent?.id }); formVisible.value = true }
function openEdit(row: DeptRow) { Object.assign(form, { id: row.id, parentId: row.parentId, name: row.name, leaderUserId: row.leaderUserId, sort: row.sort, status: row.status }); formVisible.value = true }
async function submitForm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await request({ url: form.id ? '/system/dept/update' : '/system/dept/create', method: form.id ? 'PUT' : 'POST', data: form })
    ElMessage.success(form.id ? '部门已更新' : '部门已创建')
    formVisible.value = false
    await loadDepts()
  } finally { saving.value = false }
}
async function removeDept(row: DeptRow) {
  await ElMessageBox.confirm(`确认删除部门「${row.name}」？`, '删除确认', { type: 'warning' })
  await request({ url: `/system/dept/${row.id}`, method: 'DELETE' })
  ElMessage.success('部门已删除')
  await loadDepts()
}
function emptyForm(): DeptForm { return { parentId: undefined, name: '', leaderUserId: undefined, sort: 0, status: 1 } }
</script>
