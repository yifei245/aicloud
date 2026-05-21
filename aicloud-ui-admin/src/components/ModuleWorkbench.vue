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

    <div class="action-grid">
      <article v-for="action in module.endpoints" :key="action.path + action.label" class="action-card">
        <div>
          <strong>{{ action.label }}</strong>
          <code>{{ action.method }} {{ action.path }}</code>
        </div>
        <el-button type="primary" :disabled="!auth.hasPermission(action.permission)" @click="run(action)">
          调用接口
        </el-button>
      </article>
    </div>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header">
          <span>接口返回</span>
          <el-tag v-if="lastAction" effect="dark">{{ lastAction.method }} {{ lastAction.path }}</el-tag>
        </div>
      </template>
      <el-skeleton v-if="loading" :rows="8" animated />
      <pre v-else>{{ formattedResult }}</pre>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { callEndpoint, type EndpointAction, type ModuleCard } from '@/api/modules'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ module: ModuleCard }>()
const auth = useAuthStore()
const loading = ref(false)
const result = ref<unknown>({ tip: '点击上方接口按钮，可直接联调后端真实数据。' })
const lastAction = ref<EndpointAction>()

const formattedResult = computed(() => JSON.stringify(result.value, null, 2))

async function run(action: EndpointAction) {
  loading.value = true
  lastAction.value = action
  try {
    result.value = await callEndpoint(action)
    ElMessage.success(`${props.module.title} / ${action.label} 调用成功`)
  } finally {
    loading.value = false
  }
}
</script>
