<template>
  <div class="login-scene">
    <section class="login-poster">
      <span class="poster-kicker">AICloud Console</span>
      <h1>一个能长大的微服务管理端</h1>
      <p>动态菜单、按钮权限、多终端认证、接口文档和业务模块调试集中到同一个控制台。</p>
      <div class="poster-grid">
        <div>Gateway</div><div>Nacos</div><div>SSO</div><div>Knife4j</div>
      </div>
    </section>
    <el-card class="login-card" shadow="never">
      <h2>管理员登录</h2>
      <p>默认账号：admin / 123456</p>
      <el-form :model="form" size="large" @keyup.enter="submit">
        <el-form-item><el-input v-model="form.username" prefix-icon="User" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" prefix-icon="Lock" type="password" show-password placeholder="密码" /></el-form-item>
        <el-form-item><el-input-number v-model="form.tenantId" :min="1" controls-position="right" class="full" /></el-form-item>
        <el-button class="login-button" type="primary" :loading="loading" @click="submit">进入管理端</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ tenantId: 1, username: 'admin', password: '123456', terminal: 'ADMIN' as const })

async function submit() {
  loading.value = true
  try {
    await auth.login(form)
    router.replace(String(route.query.redirect || '/dashboard'))
  } finally {
    loading.value = false
  }
}
</script>
