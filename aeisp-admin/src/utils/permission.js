import { useUserStore } from '@/stores/user'

export function hasPermission(permission) {
  const userStore = useUserStore()
  const permissions = userStore.permissions || []
  return permissions.includes(permission)
}

export function hasAnyPermission(perms) {
  const userStore = useUserStore()
  const permissions = userStore.permissions || []
  return perms.some(p => permissions.includes(p))
}
