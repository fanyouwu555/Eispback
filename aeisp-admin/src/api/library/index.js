import request from '@/utils/request'

export function listLibraries(params) {
  return request.get('/library-resources', { params })
}

export function listOnlineLibraries() {
  return request.get('/library-resources/public')
}

export function createLibrary(data) {
  return request.post('/library-resources', data, {
    timeout: 120000
  })
}

export function updateLibrary(id, data) {
  return request.put(`/library-resources/${id}`, data)
}

export function uploadLibraryVersion(id, data) {
  return request.post(`/library-resources/${id}/versions`, data, {
    timeout: 120000
  })
}

export function rollbackLibraryVersion(id, versionId) {
  return request.post(`/library-resources/${id}/rollback/${versionId}`)
}

export function toggleLibraryStatus(id, status) {
  return request.post(`/library-resources/${id}/status?status=${status}`)
}

export function deleteLibrary(id) {
  return request.delete(`/library-resources/${id}`)
}

export function getLibraryDetail(id) {
  return request.get(`/library-resources/${id}`)
}

export function getLibraryFiles(id, versionNo) {
  return request.get(`/library-resources/${id}/files`, { params: { versionNo } })
}

export function setTemplateLibraries(id, data) {
  return request.post(`/templates/${id}/libraries`, data)
}

export function getTemplateLibraries(id) {
  return request.get(`/templates/${id}/libraries`)
}
