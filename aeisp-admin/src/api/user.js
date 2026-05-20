import request from '@/utils/request'

// 用户列表
export function listUsers(params) {
  return request.get('/users', { params })
}
export function getUser(id) {
  return request.get(`/users/${id}`)
}
export function createUser(data) {
  return request.post('/users', data)
}
export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}
export function updateUserStatus(id, data) {
  return request.patch(`/users/${id}/status`, data)
}
export function resetUserPassword(id, data) {
  return request.post(`/users/${id}/reset-password`, data)
}
export function adjustDuration(id, data) {
  return request.post(`/users/${id}/adjust-duration`, data)
}

// 日志
export function listLoginLogs(userId, params) {
  return request.get(`/users/${userId}/login-logs`, { params })
}
export function listDurationLogs(userId, params) {
  return request.get(`/users/${userId}/duration-logs`, { params })
}

// 统计
export function getStatistics() {
  return request.get('/users/statistics')
}
export function getTrend(params) {
  return request.get('/users/statistics/trend', { params })
}

// 导入导出
export function importUsers(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/users/import-excel', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export function exportUsers(params) {
  return request.get('/users/export-excel', { params, responseType: 'blob' })
}