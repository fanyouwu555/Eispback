import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getToken,
  setToken,
  setRefreshToken,
  removeToken,
  removeRefreshToken
} from '@/utils/auth'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(null)
  const permissions = ref([])
  const roles = ref([])

  const isLoggedIn = computed(() => !!token.value)

  async function login(loginData) {
    const data = await request.post('/auth/login', loginData)
    token.value = data.accessToken
    setToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    userInfo.value = data.userInfo
    roles.value = data.userInfo?.roles || []
    return data
  }

  async function getInfo() {
    const data = await request.get('/auth/info')
    userInfo.value = data.userInfo || data
    permissions.value = data.permissions || []
    roles.value = data.roles || []
    return data
  }

  async function logout() {
    try {
      await request.post('/auth/logout')
    } finally {
      token.value = ''
      userInfo.value = null
      permissions.value = []
      roles.value = []
      removeToken()
      removeRefreshToken()
    }
  }

  return {
    token,
    userInfo,
    permissions,
    roles,
    isLoggedIn,
    login,
    getInfo,
    logout
  }
})
