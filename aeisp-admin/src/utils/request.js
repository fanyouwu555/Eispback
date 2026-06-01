import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  getToken,
  getRefreshToken,
  setToken,
  setRefreshToken,
  removeToken,
  removeRefreshToken
} from './auth'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api/v1',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

let isRefreshing = false
let refreshSubscribers = []

function onRefreshed(token) {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

function addRefreshSubscriber(cb) {
  refreshSubscribers.push(cb)
}

async function doRefreshToken(originalConfig) {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    removeToken()
    removeRefreshToken()
    window.location.href = '/#/login'
    return Promise.reject(new Error('登录已过期'))
  }
  if (!isRefreshing) {
    isRefreshing = true
    try {
      const refreshRes = await axios.post(
        (import.meta.env.VITE_APP_BASE_API || '/api/v1') + '/auth/refresh',
        null,
        { headers: { 'X-Refresh-Token': refreshToken } }
      )
      if (refreshRes.data.code === 200) {
        const { accessToken, refreshToken: newRefreshToken } = refreshRes.data.data
        setToken(accessToken)
        setRefreshToken(newRefreshToken)
        onRefreshed(accessToken)
        isRefreshing = false
        if (originalConfig) {
          originalConfig.headers['Authorization'] = 'Bearer ' + accessToken
          return service(originalConfig)
        }
        return accessToken
      }
    } catch {
      ElMessage.error('登录已过期，请重新登录')
    }
    removeToken()
    removeRefreshToken()
    window.location.href = '/#/login'
    isRefreshing = false
    refreshSubscribers = []
    return Promise.reject(new Error('登录已过期'))
  }
  return new Promise(resolve => {
    addRefreshSubscriber(token => {
      if (originalConfig) {
        originalConfig.headers['Authorization'] = 'Bearer ' + token
        resolve(service(originalConfig))
      } else {
        resolve(token)
      }
    })
  })
}

service.interceptors.response.use(
  response => {
    // Blob 响应直接返回数据，不按 JSON 解析
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      // 401：Token 过期或无效，尝试刷新后重试
      if (res.code === 401) {
        return doRefreshToken(response.config)
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  async error => {
    const { response, config } = error
    if (response && response.status === 401) {
      return doRefreshToken(config)
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
