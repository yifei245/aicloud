import { computed } from 'vue'
import { moduleCatalog } from '@/api/modules'

export function useModule(key: string) {
  return computed(() => moduleCatalog.find(item => item.key === key) || moduleCatalog[0])
}
