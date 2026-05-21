<template>
  <ModuleWorkbench :module="module" />
</template>

<script setup lang="ts">
import ModuleWorkbench from '@/components/ModuleWorkbench.vue'
import { useModule } from '@/composables/useModule'

const module = useModule('report')
</script>
