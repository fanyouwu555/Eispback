import { hasPermission } from '@/utils/permission'

export default {
  mounted(el, binding) {
    const { value } = binding
    if (!value) return
    // 兼容字符串和数组两种写法：v-permission="'perm:code'" 或 v-permission="['perm:a', 'perm:b']"
    const perms = Array.isArray(value) ? value : [value]
    if (perms.length === 0) return
    const has = perms.some(p => hasPermission(p))
    if (!has) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
