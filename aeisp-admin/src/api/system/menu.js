import request from '@/utils/request'

export function getMenuTree() {
  return request.get('/system/menus/tree')
}

export function getRoleMenuIds(roleId) {
  return request.get(`/system/menus/role-menus/${roleId}`)
}

export function getMenu(id) {
  return request.get(`/system/menus/${id}`)
}

export function createMenu(data) {
  return request.post('/system/menus', data)
}

export function updateMenu(id, data) {
  return request.put(`/system/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/system/menus/${id}`)
}

export function getUserRoutes() {
  return request.get('/system/menus/routes')
}