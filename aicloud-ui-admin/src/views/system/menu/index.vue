<template>
  <section class="module-page menu-admin-page">
    <div class="module-hero" style="--accent: #1c4c5b">
      <div><span class="eyebrow">Permission Menu</span><h1>菜单管理</h1><p>维护目录、菜单、按钮权限、前端路由组件和排序状态。</p></div>
      <el-icon class="hero-icon"><Menu /></el-icon>
    </div>
    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header user-toolbar">
          <div><strong>菜单树</strong><small>GET /system/menu/list</small></div>
          <div class="result-actions user-actions compact-actions">
            <el-button :loading="loading" @click="loadMenus"><el-icon><Refresh /></el-icon>刷新</el-button>
            <el-button type="primary" @click="openCreate()"><el-icon><Plus /></el-icon>新增菜单</el-button>
          </div>
        </div>
      </template>
      <el-table v-loading="loading" :data="menus" row-key="id" stripe border height="640" default-expand-all class="admin-data-table">
        <el-table-column prop="name" label="名称" min-width="190" fixed />
        <el-table-column prop="type" label="类型" width="90"><template #default="{ row }"><el-tag :type="typeTag(row.type)">{{ typeLabel(row.type) }}</el-tag></template></el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="170" show-overflow-tooltip />
        <el-table-column prop="component" label="组件" min-width="210" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" min-width="210" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="110" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="visible" label="显示" width="80"><template #default="{ row }"><el-tag :type="row.visible === 0 ? 'info' : 'success'">{{ row.visible === 0 ? '隐藏' : '显示' }}</el-tag></template></el-table-column>
        <el-table-column prop="status" label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="240" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openCreate(row)">新增下级</el-button><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="removeMenu(row)">删除</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="上级菜单"><el-tree-select v-model="form.parentId" :data="menuOptions" node-key="id" :props="treeProps" check-strictly clearable placeholder="不选则为顶级菜单" /></el-form-item>
        <el-form-item label="菜单类型" prop="type"><el-radio-group v-model="form.type"><el-radio-button :label="1">目录</el-radio-button><el-radio-button :label="2">菜单</el-radio-button><el-radio-button :label="3">按钮</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="菜单名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="路由路径"><el-input v-model="form.path" placeholder="例如 /system/user" /></el-form-item>
        <el-form-item label="组件路径"><el-input v-model="form.component" placeholder="例如 system/user/index" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.permission" placeholder="例如 system:user:query" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="显示"><el-radio-group v-model="form.visible"><el-radio-button :label="1">显示</el-radio-button><el-radio-button :label="0">隐藏</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio-button :label="1">启用</el-radio-button><el-radio-button :label="0">禁用</el-radio-button></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="formVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitForm">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/utils/request'

interface MenuRow { id?: number; parentId?: number; name: string; type: number; path?: string; component?: string; permission?: string; icon?: string; visible?: number; sort?: number; status?: number; children?: MenuRow[] }
interface MenuForm { id?: number; parentId?: number; name: string; type: number; path?: string; component?: string; permission?: string; icon?: string; visible: number; sort: number; status: number }
const loading = ref(false), saving = ref(false), formVisible = ref(false)
const menus = ref<MenuRow[]>([])
const formRef = ref<FormInstance>()
const form = reactive<MenuForm>(emptyForm())
const treeProps = { label: 'name', children: 'children' }
const rules: FormRules<MenuForm> = { name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }], type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }] }
const menuOptions = computed(() => menus.value)

onMounted(loadMenus)
async function loadMenus() { loading.value = true; try { menus.value = await request<MenuRow[]>({ url: '/system/menu/list', method: 'GET' }) } finally { loading.value = false } }
function openCreate(parent?: MenuRow) { Object.assign(form, emptyForm(), { parentId: parent?.id }); formVisible.value = true }
function openEdit(row: MenuRow) { Object.assign(form, { id: row.id, parentId: row.parentId || undefined, name: row.name, type: row.type, path: row.path, component: row.component, permission: row.permission, icon: row.icon, visible: row.visible ?? 1, sort: row.sort ?? 0, status: row.status ?? 1 }); formVisible.value = true }
async function submitForm() { await formRef.value?.validate(); saving.value = true; try { await request({ url: '/system/menu/save', method: 'POST', data: { ...form, parentId: form.parentId || 0 } }); ElMessage.success('菜单已保存'); formVisible.value = false; await loadMenus() } finally { saving.value = false } }
async function removeMenu(row: MenuRow) { await ElMessageBox.confirm(`确认删除菜单「${row.name}」？`, '删除确认', { type: 'warning' }); await request({ url: `/system/menu/${row.id}`, method: 'DELETE' }); ElMessage.success('菜单已删除'); await loadMenus() }
function emptyForm(): MenuForm { return { parentId: undefined, name: '', type: 2, path: '', component: '', permission: '', icon: '', visible: 1, sort: 0, status: 1 } }
function typeLabel(type: number) { return type === 1 ? '目录' : type === 2 ? '菜单' : '按钮' }
function typeTag(type: number) { return type === 1 ? 'success' : type === 2 ? 'primary' : 'warning' }
</script>
