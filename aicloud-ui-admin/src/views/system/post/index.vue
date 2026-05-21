<template>
  <section class="module-page post-admin-page">
    <div class="module-hero" style="--accent: #8b5d33">
      <div>
        <span class="eyebrow">Organization Admin</span>
        <h1>岗位管理</h1>
        <p>维护组织岗位编码、名称、排序和启停状态，供用户管理分配岗位使用。</p>
      </div>
      <el-icon class="hero-icon"><Suitcase /></el-icon>
    </div>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header user-toolbar">
          <div><strong>岗位列表</strong><small>GET /system/post/list</small></div>
          <div class="result-actions user-actions">
            <el-input v-model="query.keyword" clearable placeholder="岗位名称 / 编码" prefix-icon="Search" @keyup.enter="loadPosts" />
            <el-select v-model="query.status" clearable placeholder="状态" class="status-filter">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button :loading="loading" @click="loadPosts"><el-icon><Refresh /></el-icon>刷新</el-button>
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增岗位</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="posts" stripe border height="560" class="admin-data-table">
        <el-table-column prop="id" label="ID" width="90" fixed />
        <el-table-column prop="name" label="岗位名称" min-width="180" fixed />
        <el-table-column prop="code" label="岗位编码" min-width="160" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removePost(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑岗位' : '新增岗位'" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="岗位名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="岗位编码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="0">禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/utils/request'

interface PostRow { id?: number; code: string; name: string; sort: number; status: number; createTime?: string; updateTime?: string }

const loading = ref(false)
const saving = ref(false)
const formVisible = ref(false)
const formRef = ref<FormInstance>()
const posts = ref<PostRow[]>([])
const query = reactive<{ keyword: string; status?: number }>({ keyword: '' })
const form = reactive<PostRow>(emptyForm())
const rules: FormRules<PostRow> = {
  name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }]
}

onMounted(loadPosts)

async function loadPosts() {
  loading.value = true
  try {
    posts.value = await request<PostRow[]>({ url: '/system/post/list', method: 'GET', params: { keyword: query.keyword || undefined, status: query.status } })
  } finally {
    loading.value = false
  }
}
function openCreate() { Object.assign(form, emptyForm()); formVisible.value = true }
function openEdit(row: PostRow) { Object.assign(form, row); formVisible.value = true }
async function submitForm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await request({ url: form.id ? '/system/post/update' : '/system/post/create', method: form.id ? 'PUT' : 'POST', data: form })
    ElMessage.success(form.id ? '岗位已更新' : '岗位已创建')
    formVisible.value = false
    await loadPosts()
  } finally { saving.value = false }
}
async function removePost(row: PostRow) {
  await ElMessageBox.confirm(`确认删除岗位「${row.name}」？`, '删除确认', { type: 'warning' })
  await request({ url: `/system/post/${row.id}`, method: 'DELETE' })
  ElMessage.success('岗位已删除')
  await loadPosts()
}
function emptyForm(): PostRow { return { code: '', name: '', sort: 0, status: 1 } }
</script>
