<template>
  <section class="api-doc-page">
    <div class="module-hero doc-hero">
      <div><span class="eyebrow">Knife4j</span><h1>聚合接口文档</h1><p>后端 Gateway 已聚合所有模块文档，可在新窗口调试接口。</p></div>
      <el-button type="primary" size="large" @click="open">打开 Knife4j</el-button>
    </div>
    <iframe :src="url" />
  </section>
</template>
<script setup lang="ts">
const url = import.meta.env.VITE_KNIFE4J_URL || 'http://127.0.0.1:48080/doc.html'
function open() { window.open(url, '_blank') }
</script>
