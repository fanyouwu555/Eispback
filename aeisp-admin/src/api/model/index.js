import request from '@/utils/request'

// ===== 模型管理 =====
export function listModels(params) {
  return request.get('/models', { params })
}

export function getModel(id) {
  return request.get(`/models/${id}`)
}

export function createModel(data) {
  return request.post('/models', data)
}

export function updateModel(id, data) {
  return request.put(`/models/${id}`, data)
}

export function deleteModel(id) {
  return request.delete(`/models/${id}`)
}

export function toggleModelStatus(id, status) {
  return request.post(`/models/${id}/status`, null, { params: { status } })
}

export function updateModelSortOrder(id, sortOrder) {
  return request.post(`/models/${id}/sort-order`, null, { params: { sortOrder } })
}

export function testModel(id, data) {
  return request.post(`/models/${id}/test`, data)
}

export function getModelStats(id) {
  return request.get(`/models/${id}/stats`)
}