<template>
  <section class="module-page dict-admin-page">
    <div class="module-hero" style="--accent: #8b5d33">
      <div>
        <span class="eyebrow">System Dictionary</span>
        <h1>字典管理</h1>
        <p>维护字典类型和字典数据，供状态、枚举、业务下拉等配置使用。</p>
      </div>
      <el-icon class="hero-icon"><Collection /></el-icon>
    </div>

    <div class="dict-layout">
      <el-card class="result-card" shadow="never">
        <template #header>
          <div class="result-header user-toolbar">
            <div><strong>字典类型</strong><small>GET /system/dict/type/list</small></div>
            <div class="result-actions user-actions compact-actions">
              <el-input v-model="typeQuery.keyword" clearable placeholder="类型 / 名称" prefix-icon="Search" @keyup.enter="loadTypes" />
              <el-select v-model="typeQuery.status" clearable placeholder="状态" class="status-filter">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
              <el-button :loading="typeLoading" @click="loadTypes"><el-icon><Refresh /></el-icon></el-button>
              <el-button type="primary" @click="openType()"><el-icon><Plus /></el-icon>新增</el-button>
            </div>
          </div>
        </template>
        <el-table v-loading="typeLoading" :data="types" stripe border height="560" highlight-current-row @current-change="selectType">
          <el-table-column prop="dictName" label="名称" min-width="130" />
          <el-table-column prop="dictType" label="类型" min-width="170" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click.stop="openType(row)">编辑</el-button><el-button link type="danger" @click.stop="removeType(row)">删除</el-button></template></el-table-column>
        </el-table>
      </el-card>

      <el-card class="result-card" shadow="never">
        <template #header>
          <div class="result-header user-toolbar">
            <div><strong>字典数据</strong><small>{{ currentType?.dictType || '请选择字典类型' }}</small></div>
            <div class="result-actions user-actions compact-actions">
              <el-select v-model="dataStatus" clearable placeholder="状态" class="status-filter" @change="loadData">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
              <el-button :disabled="!currentType" :loading="dataLoading" @click="loadData"><el-icon><Refresh /></el-icon></el-button>
              <el-button type="primary" :disabled="!currentType" @click="openData()"><el-icon><Plus /></el-icon>新增数据</el-button>
            </div>
          </div>
        </template>
        <el-empty v-if="!currentType" description="请先选择左侧字典类型" />
        <el-table v-else v-loading="dataLoading" :data="dictData" stripe border height="560">
          <el-table-column prop="dictLabel" label="标签" min-width="150" />
          <el-table-column prop="dictValue" label="值" min-width="150" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="130"><template #default="{ row }"><el-button link type="primary" @click="openData(row)">编辑</el-button><el-button link type="danger" @click="removeData(row)">删除</el-button></template></el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="typeVisible" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="460px">
      <el-form ref="typeRef" :model="typeForm" :rules="typeRules" label-width="96px">
        <el-form-item label="字典名称" prop="dictName"><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="字典类型" prop="dictType"><el-input v-model="typeForm.dictType" :disabled="Boolean(typeForm.id)" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="typeForm.status"><el-radio-button :label="1">启用</el-radio-button><el-radio-button :label="0">禁用</el-radio-button></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="typeVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitType">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dataVisible" :title="dataForm.id ? '编辑字典数据' : '新增字典数据'" width="460px">
      <el-form ref="dataRef" :model="dataForm" :rules="dataRules" label-width="96px">
        <el-form-item label="字典类型"><el-input v-model="dataForm.dictType" disabled /></el-form-item>
        <el-form-item label="数据标签" prop="dictLabel"><el-input v-model="dataForm.dictLabel" /></el-form-item>
        <el-form-item label="数据值" prop="dictValue"><el-input v-model="dataForm.dictValue" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="dataForm.status"><el-radio-button :label="1">启用</el-radio-button><el-radio-button :label="0">禁用</el-radio-button></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="dataVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitData">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/utils/request'

interface DictType { id?: number; dictType: string; dictName: string; status: number; createTime?: string; updateTime?: string }
interface DictData { id?: number; dictType: string; dictLabel: string; dictValue: string; sort: number; status: number }
const typeLoading = ref(false), dataLoading = ref(false), saving = ref(false)
const typeVisible = ref(false), dataVisible = ref(false)
const types = ref<DictType[]>([]), dictData = ref<DictData[]>([]), currentType = ref<DictType>()
const typeRef = ref<FormInstance>(), dataRef = ref<FormInstance>()
const typeQuery = reactive<{ keyword: string; status?: number }>({ keyword: '' })
const dataStatus = ref<number>()
const typeForm = reactive<DictType>({ dictType: '', dictName: '', status: 1 })
const dataForm = reactive<DictData>({ dictType: '', dictLabel: '', dictValue: '', sort: 0, status: 1 })
const typeRules: FormRules<DictType> = { dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }], dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }] }
const dataRules: FormRules<DictData> = { dictLabel: [{ required: true, message: '请输入数据标签', trigger: 'blur' }], dictValue: [{ required: true, message: '请输入数据值', trigger: 'blur' }] }

onMounted(loadTypes)
async function loadTypes() { typeLoading.value = true; try { types.value = await request<DictType[]>({ url: '/system/dict/type/list', method: 'GET', params: { keyword: typeQuery.keyword || undefined, status: typeQuery.status } }); if (!currentType.value && types.value[0]) selectType(types.value[0]) } finally { typeLoading.value = false } }
async function loadData() { if (!currentType.value) return; dataLoading.value = true; try { dictData.value = await request<DictData[]>({ url: '/system/dict/data/list', method: 'GET', params: { dictType: currentType.value.dictType, status: dataStatus.value } }) } finally { dataLoading.value = false } }
function selectType(row?: DictType) { if (!row) return; currentType.value = row; loadData() }
function openType(row?: DictType) { Object.assign(typeForm, row || { id: undefined, dictType: '', dictName: '', status: 1 }); typeVisible.value = true }
function openData(row?: DictData) { if (!currentType.value) return; Object.assign(dataForm, row || { id: undefined, dictType: currentType.value.dictType, dictLabel: '', dictValue: '', sort: 0, status: 1 }); dataVisible.value = true }
async function submitType() { await typeRef.value?.validate(); saving.value = true; try { await request({ url: '/system/dict/type/save', method: 'POST', data: typeForm }); ElMessage.success('字典类型已保存'); typeVisible.value = false; await loadTypes() } finally { saving.value = false } }
async function submitData() { await dataRef.value?.validate(); saving.value = true; try { await request({ url: '/system/dict/data/save', method: 'POST', data: dataForm }); ElMessage.success('字典数据已保存'); dataVisible.value = false; await loadData() } finally { saving.value = false } }
async function removeType(row: DictType) { await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」及其数据？`, '删除确认', { type: 'warning' }); await request({ url: `/system/dict/type/${row.id}`, method: 'DELETE' }); currentType.value = undefined; dictData.value = []; await loadTypes() }
async function removeData(row: DictData) { await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」？`, '删除确认', { type: 'warning' }); await request({ url: `/system/dict/data/${row.id}`, method: 'DELETE' }); await loadData() }
</script>
