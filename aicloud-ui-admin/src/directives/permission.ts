import type { App, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

function mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const auth = useAuthStore()
  const values = Array.isArray(binding.value) ? binding.value : [binding.value]
  const allowed = values.some(permission => auth.hasPermission(permission))
  if (!allowed) el.parentElement?.removeChild(el)
}

export default {
  install(app: App) {
    app.directive('permission', { mounted })
  }
}
