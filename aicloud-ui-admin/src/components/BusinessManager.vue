<template>
  <section class="business-page">
    <div class="business-hero" :style="{ '--accent': config.accent || '#2f6b4f' }">
      <div>
        <span class="eyebrow">Business Center</span>
        <h1>{{ config.title }}</h1>
        <p>{{ config.summary }}</p>
      </div>
      <el-icon class="hero-icon"><component :is="config.icon || 'Grid'" /></el-icon>
    </div>

    <el-tabs v-model="activeKey" class="business-tabs" @tab-change="loadActive">
      <el-tab-pane v-for="resource in config.resources" :key="resource.key" :label="resource.title" :name="resource.key" />
    </el-tabs>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header business-header">
          <div>
            <strong>{{ active.title }}</strong>
            <small>{{ active.description || active.listUrl }}</small>
          </div>
          <div class="result-actions user-actions compact-actions">
            <el-input v-if="active.search !== false" v-model="keyword" clearable placeholder="关键词/状态/编号" prefix-icon="Search" @keyup.enter="loadActive" />
            <el-button @click="loadActive"><el-icon><Refresh /></el-icon>刷新</el-button>
            <el-button v-if="active.createUrl" type="primary" @click="openForm()">新增{{ active.entityName || active.title }}</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="filteredRows" stripe border class="admin-data-table" height="520">
        <el-table-column type="index" width="58" label="#" fixed />
        <el-table-column v-for="column in visibleColumns" :key="column.prop" :prop="column.prop" :label="column.label" :min-width="column.width || 140" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="column.type === 'status'" :type="statusType(row[column.prop])">{{ formatCell(row[column.prop]) }}</el-tag>
            <span v-else>{{ formatCell(row[column.prop]) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-if="active.updateUrl" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-for="action in active.rowActions || []" :key="action.label" link :type="action.danger ? 'danger' : 'primary'" @click="runAction(action, row)">{{ action.label }}</el-button>
            <el-button v-if="active.deleteUrl" link type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="`${form.id ? '编辑' : '新增'}${active.entityName || active.title}`" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="110px">
        <el-form-item v-for="field in active.fields || []" :key="field.prop" :label="field.label" :required="field.required">
          <el-select v-if="field.type === 'select'" v-model="form[field.prop]" clearable filterable :placeholder="`请选择${field.label}`">
            <el-option v-for="option in field.options || []" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
          <el-input-number v-else-if="field.type === 'number'" v-model="form[field.prop]" :min="field.min ?? 0" controls-position="right" style="width: 100%" />
          <el-date-picker v-else-if="field.type === 'datetime'" v-model="form[field.prop]" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
          <el-date-picker v-else-if="field.type === 'date'" v-model="form[field.prop]" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="3" />
          <el-input v-else v-model="form[field.prop]" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="actionVisible" :title="currentAction?.label" width="560px" destroy-on-close>
      <el-form :model="actionForm" label-width="110px">
        <el-form-item v-for="field in currentAction?.fields || []" :key="field.prop" :label="field.label" :required="field.required">
          <el-select v-if="field.type === 'select'" v-model="actionForm[field.prop]" clearable filterable>
            <el-option v-for="option in field.options || []" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
          <el-input-number v-else-if="field.type === 'number'" v-model="actionForm[field.prop]" :min="field.min ?? 0" controls-position="right" style="width: 100%" />
          <el-input v-else-if="field.type === 'textarea'" v-model="actionForm[field.prop]" type="textarea" :rows="3" />
          <el-input v-else v-model="actionForm[field.prop]" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAction">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'

type FieldType = 'text' | 'number' | 'select' | 'textarea' | 'datetime' | 'date'
type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'

export interface FieldConfig { prop: string; label: string; type?: FieldType; required?: boolean; min?: number; default?: unknown; options?: { label: string; value: string | number }[] }
export interface ColumnConfig { prop: string; label: string; type?: 'status' | 'text'; width?: number }
export interface RowAction { label: string; url: string; method?: Method; danger?: boolean; confirm?: string; mode?: 'params' | 'body'; fields?: FieldConfig[]; defaults?: Record<string, unknown>; map?: Record<string, string> }
export interface ResourceConfig { key: string; title: string; entityName?: string; description?: string; listUrl: string; createUrl?: string; updateUrl?: string; deleteUrl?: string; deleteMode?: 'path' | 'params'; search?: boolean; columns: ColumnConfig[]; fields?: FieldConfig[]; rowActions?: RowAction[] }
export interface BusinessConfig { title: string; summary: string; icon?: string; accent?: string; resources: ResourceConfig[] }

const props = defineProps<{ config: BusinessConfig }>()
const activeKey = ref(props.config.resources[0]?.key || '')
const rows = ref<Record<string, unknown>[]>([])
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const formVisible = ref(false)
const actionVisible = ref(false)
const form = reactive<Record<string, any>>({})
const actionForm = reactive<Record<string, any>>({})
const currentAction = ref<RowAction>()
const currentRow = ref<Record<string, unknown>>()

const active = computed(() => props.config.resources.find(item => item.key === activeKey.value) || props.config.resources[0])
const visibleColumns = computed(() => active.value.columns)
const filteredRows = computed(() => {
  const word = keyword.value.trim().toLowerCase()
  if (!word) return rows.value
  return rows.value.filter(row => JSON.stringify(row).toLowerCase().includes(word))
})

watch(() => props.config.title, () => {
  activeKey.value = props.config.resources[0]?.key || ''
  loadActive()
})

onMounted(loadActive)

async function loadActive() {
  if (!active.value) return
  loading.value = true
  try {
    const data = await request<unknown>({ url: active.value.listUrl, method: 'GET' })
    rows.value = extractRows(data)
  } finally {
    loading.value = false
  }
}

function openForm(row?: Record<string, unknown>) {
  clearObject(form)
  for (const field of active.value.fields || []) form[field.prop] = row?.[field.prop] ?? field.default ?? ''
  if (row?.id != null) form.id = row.id
  formVisible.value = true
}

async function submitForm() {
  saving.value = true
  try {
    const url = form.id && active.value.updateUrl ? active.value.updateUrl : active.value.createUrl
    if (!url) return
    await request({ url, method: 'POST', data: cleanPayload(form) })
    ElMessage.success('保存成功')
    formVisible.value = false
    await loadActive()
  } finally {
    saving.value = false
  }
}

async function removeRow(row: Record<string, unknown>) {
  if (!active.value.deleteUrl) return
  await ElMessageBox.confirm(`确认删除 ${row.name || row.id}？`, '删除确认', { type: 'warning' })
  const url = active.value.deleteMode === 'params' ? active.value.deleteUrl : `${active.value.deleteUrl}/${row.id}`
  const params = active.value.deleteMode === 'params' ? { id: row.id } : undefined
  await request({ url, method: 'DELETE', params })
  ElMessage.success('删除成功')
  await loadActive()
}

async function runAction(action: RowAction, row: Record<string, unknown>) {
  currentAction.value = action
  currentRow.value = row
  clearObject(actionForm)
  for (const field of action.fields || []) {
    const mappedProp = action.map?.[field.prop]
    actionForm[field.prop] = row[mappedProp || field.prop] ?? action.defaults?.[field.prop] ?? field.default ?? ''
  }
  if (action.fields?.length) {
    actionVisible.value = true
    return
  }
  await executeAction(action, row, {})
}

async function submitAction() {
  if (!currentAction.value || !currentRow.value) return
  saving.value = true
  try {
    await executeAction(currentAction.value, currentRow.value, actionForm)
    actionVisible.value = false
  } finally {
    saving.value = false
  }
}

async function executeAction(action: RowAction, row: Record<string, unknown>, payload: Record<string, unknown>) {
  if (action.confirm) await ElMessageBox.confirm(action.confirm, '操作确认', { type: action.danger ? 'warning' : 'info' })
  const data = buildPayload(action, row, payload)
  const method = action.method || 'POST'
  await request({ url: action.url, method, data: action.mode === 'body' ? data : undefined, params: action.mode === 'body' ? undefined : data })
  ElMessage.success('操作成功')
  await loadActive()
}

function buildPayload(action: RowAction, row: Record<string, unknown>, payload: Record<string, unknown>) {
  const result: Record<string, unknown> = { id: row.id, ...action.defaults, ...payload }
  for (const [target, source] of Object.entries(action.map || {})) result[target] = row[source]
  return cleanPayload(result)
}

function extractRows(input: unknown): Record<string, unknown>[] {
  if (Array.isArray(input)) return input as Record<string, unknown>[]
  if (input && typeof input === 'object') {
    const obj = input as Record<string, unknown>
    for (const key of ['list', 'records', 'rows', 'items', 'data']) if (Array.isArray(obj[key])) return obj[key] as Record<string, unknown>[]
    return [obj]
  }
  return []
}

function cleanPayload(source: Record<string, any>) {
  const payload: Record<string, unknown> = {}
  Object.entries(source).forEach(([key, value]) => {
    if (value !== '' && value !== undefined && value !== null) payload[key] = value
  })
  return payload
}

function clearObject(target: Record<string, any>) {
  Object.keys(target).forEach(key => delete target[key])
}

function formatCell(value: unknown) {
  if (value === null || value === undefined || value === '') return '-'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function statusType(value: unknown) {
  if ([1, '1', 'ON_SALE', 'ENABLE', 'ENABLED', 'ACTIVE', 'APPROVED', 'PAID', 'SHIPPED', 'COMPLETED', 'SUCCESS'].includes(value as any)) return 'success'
  if ([0, '0', 'DISABLED', 'REJECTED', 'CANCELLED', 'CLOSED', 'FAILED'].includes(value as any)) return 'danger'
  return 'warning'
}
</script>
