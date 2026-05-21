<template>
  <div class="metric-card">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
    <small>{{ hint }}</small>
  </div>
</template>

<script setup lang="ts">
defineProps<{ label: string; value: string | number; hint: string }>()
</script>
