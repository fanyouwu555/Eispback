import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import { getUserRoutes } from '@/api/system/menu'
import { buildRoutes } from '@/utils/dynamic-routes'

export const useMenuStore = defineStore('menu', () => {
  const dynamicRoutes = shallowRef([])
  const menuTree = ref([])
  const permissions = ref([])

  async function fetchRoutes() {
    const data = await getUserRoutes()
    menuTree.value = data.menus || []
    permissions.value = data.permissions || []
    dynamicRoutes.value = buildRoutes(data.menus || [])
  }

  return { dynamicRoutes, menuTree, permissions, fetchRoutes }
})