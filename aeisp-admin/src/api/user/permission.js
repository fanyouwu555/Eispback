import request from '@/utils/request'

export function getUserPermissions(userId) {
  return request.get(`/user-permissions/${userId}`)
}

export function updateUserPermissions(userId, data) {
  return request.put(`/user-permissions/${userId}`, data)
}

export function getPermissionKeys() {
  return request.get('/user-permissions/keys')
}