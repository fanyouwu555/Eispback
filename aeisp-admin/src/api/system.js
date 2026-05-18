import request from '@/utils/request'

// 用户管理
export function listSysUsers(params) {
  return request.get('/system/users', { params })
}
export function getSysUser(id) {
  return request.get(`/system/users/${id}`)
}
export function addSysUser(data) {
  return request.post('/system/users', data)
}
export function updateSysUser(id, data) {
  return request.put(`/system/users/${id}`, data)
}
export function deleteSysUser(id) {
  return request.delete(`/system/users/${id}`)
}

// 角色管理
export function listRoles(params) {
  return request.get('/system/roles', { params })
}
export function addRole(data) {
  return request.post('/system/roles', data)
}
export function updateRole(id, data) {
  return request.put(`/system/roles/${id}`, data)
}
export function deleteRole(id) {
  return request.delete(`/system/roles/${id}`)
}

// 权限管理
export function listPermissions(params) {
  return request.get('/system/permissions', { params })
}

// 操作日志
export function listLogs(params) {
  return request.get('/system/logs', { params })
}

// 系统配置
export function listConfigs(params) {
  return request.get('/system/configs', { params })
}
export function updateConfig(key, data) {
  return request.put(`/system/configs/${key}`, data)
}
