import { hasPermission } from '@/utils/permission'

export default {
  mounted(el, binding) {
    const { value } = binding
    if (!value || !Array.isArray(value)) return
    const has = value.some(p => hasPermission(p))
    if (!has) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
