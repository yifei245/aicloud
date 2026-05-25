<template>
  <section class="module-page">
    <div class="module-hero" :style="{ '--accent': module.accent }">
      <div>
        <span class="eyebrow">AICloud Module</span>
        <h1>{{ module.title }}</h1>
        <p>{{ module.summary }}</p>
      </div>
      <el-icon class="hero-icon"><component :is="module.icon" /></el-icon>
    </div>

    <div class="toolbar-panel">
      <el-segmented v-model="activePath" :options="actionOptions" @change="selectAction" />
      <div class="toolbar-actions">
        <el-input v-model="keyword" clearable placeholder="在当前结果内搜索" prefix-icon="Search" />
        <el-button type="primary" :loading="loading" @click="run(activeAction)"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>
    </div>

    <div class="metric-grid compact">
      <DataMetric label="当前接口" :value="activeAction.label" :hint="activeAction.path" />
      <DataMetric label="记录数量" :value="rows.length" hint="自动识别数组数据" />
      <DataMetric label="字段数量" :value="columns.length" hint="按返回字段动态生成" />
      <DataMetric label="权限状态" :value="auth.hasPermission(activeAction.permission) ? '允许' : '受限'" :hint="activeAction.permission || '未声明按钮权限'" />
    </div>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header">
          <div>
            <strong>{{ module.title }}数据视图</strong>
            <small>{{ activeAction.method }} {{ activeAction.path }}</small>
          </div>
          <div class="result-actions">
            <el-tag v-if="lastLoadedAt" effect="dark">{{ lastLoadedAt }}</el-tag>
            <el-button text @click="showRaw = !showRaw">{{ showRaw ? '收起原始数据' : '查看原始数据' }}</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />
      <template v-else>
        <el-empty v-if="!rows.length" description="暂无列表数据，已保留原始返回可查看" />
        <el-table v-else :data="pagedRows" stripe border height="460" class="admin-data-table">
          <el-table-column type="index" width="64" label="#" fixed />
          <el-table-column v-for="column in columns" :key="column" :prop="column" :label="columnLabel(column)" min-width="150" show-overflow-tooltip>
            <template #default="scope">
              <el-tag v-if="isStatusColumn(column)" :type="statusType(scope.row[column])">{{ formatCell(scope.row[column]) }}</el-tag>
              <span v-else>{{ formatCell(scope.row[column]) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="table-pagination">
          <span class="pagination-total">共 {{ filteredRows.length }} 条</span>
          <el-pagination v-model:current-page="pagination.pageNo" v-model:page-size="pagination.pageSize" :page-sizes="[10, 20, 50, 100]" :total="filteredRows.length" layout="sizes, prev, pager, next, jumper" background />
        </div>
        <el-collapse-transition>
          <pre v-if="showRaw" class="raw-block">{{ formattedResult }}</pre>
        </el-collapse-transition>
      </template>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import DataMetric from '@/components/DataMetric.vue'
import { callEndpoint, type EndpointAction, type ModuleCard } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ module: ModuleCard }>()
const auth = useAuthStore()
const loading = ref(false)
const showRaw = ref(false)
const keyword = ref('')
const result = ref<unknown>({ tip: '数据加载中' })
const lastAction = ref<EndpointAction>()
const activePath = ref(props.module.endpoints[0]?.path || '')
const lastLoadedAt = ref('')
const pagination = reactive({ pageNo: 1, pageSize: 20 })

const actionOptions = computed(() => props.module.endpoints.map(item => ({ label: item.label, value: item.path })))
const activeAction = computed(() => props.module.endpoints.find(item => item.path === activePath.value) || props.module.endpoints[0])
const rows = computed(() => extractRows(result.value))
const columns = computed(() => collectColumns(rows.value))
const formattedResult = computed(() => JSON.stringify(result.value, null, 2))
const filteredRows = computed(() => {
  const word = keyword.value.trim().toLowerCase()
  if (!word) return rows.value
  return rows.value.filter(row => JSON.stringify(row).toLowerCase().includes(word))
})
const pagedRows = computed(() => filteredRows.value.slice((pagination.pageNo - 1) * pagination.pageSize, pagination.pageNo * pagination.pageSize))

watch(() => props.module.key, () => {
  activePath.value = props.module.endpoints[0]?.path || ''
  pagination.pageNo = 1
  run(activeAction.value)
})

watch(keyword, () => { pagination.pageNo = 1 })

onMounted(() => run(activeAction.value))

function selectAction() {
  pagination.pageNo = 1
  run(activeAction.value)
}

async function run(action?: EndpointAction) {
  if (!action) return
  if (!auth.hasPermission(action.permission)) {
    ElMessage.warning(`缺少权限：${action.permission}`)
    return
  }
  loading.value = true
  lastAction.value = action
  try {
    result.value = await callEndpoint(action)
    lastLoadedAt.value = new Date().toLocaleTimeString()
  } finally {
    loading.value = false
  }
}

function extractRows(input: unknown): Record<string, unknown>[] {
  if (Array.isArray(input)) return input.map(normalizeRow)
  if (input && typeof input === 'object') {
    const obj = input as Record<string, unknown>
    for (const key of ['list', 'records', 'rows', 'items', 'data']) {
      const value = obj[key]
      if (Array.isArray(value)) return value.map(normalizeRow)
    }
    return [normalizeRow(obj)]
  }
  return []
}

function normalizeRow(row: unknown): Record<string, unknown> {
  if (!row || typeof row !== 'object') return { value: row }
  const normalized: Record<string, unknown> = {}
  Object.entries(row as Record<string, unknown>).forEach(([key, value]) => {
    normalized[key] = value && typeof value === 'object' ? JSON.stringify(value) : value
  })
  return normalized
}

function collectColumns(data: Record<string, unknown>[]) {
  const preferred = ['id', 'name', 'username', 'nickname', 'title', 'status', 'createTime', 'updateTime']
  const keys = Array.from(new Set(data.flatMap(row => Object.keys(row))))
  return [...preferred.filter(key => keys.includes(key)), ...keys.filter(key => !preferred.includes(key))].slice(0, 10)
}

function columnLabel(key: string) {
  const map: Record<string, string> = {
    id: 'ID', name: '名称', username: '用户名', nickname: '昵称', title: '标题', status: '状态', createTime: '创建时间', updateTime: '更新时间', code: '编码', type: '类型', amount: '金额', price: '价格'
  }
  return map[key] || key
}

function isStatusColumn(key: string) {
  return key.toLowerCase().includes('status')
}

function statusType(value: unknown) {
  if (value === 1 || value === '1' || value === 'ACTIVE' || value === 'SUCCESS' || value === 'PUBLISHED') return 'success'
  if (value === 0 || value === '0' || value === 'DISABLED' || value === 'FAILED') return 'danger'
  return 'warning'
}

function formatCell(value: unknown) {
  if (value === null || value === undefined || value === '') return '-'
  return String(value)
}
</script>
