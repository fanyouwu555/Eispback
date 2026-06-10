import request from '@/utils/request'

// ===== 库资源管理 =====
export function listLibraries(params) {
  return request.get('/library-resources', { params })
}

export function listOnlineLibraries() {
  return request.get('/library-resources/public')
}

export function createLibrary(data) {
  return request.post('/library-resources', data)
}

export function updateLibrary(id, data) {
  return request.put(`/library-resources/${id}`, data)
}

export function deleteLibrary(id) {
  return request.delete(`/library-resources/${id}`)
}

export function getLibraryDetail(id) {
  return request.get(`/library-resources/${id}`)
}

// ===== 模板关联库资源 =====
export function setTemplateLibraries(id, data) {
  return request.post(`/templates/${id}/libraries`, data)
}

export function getTemplateLibraries(id) {
  return request.get(`/templates/${id}/libraries`)
}
