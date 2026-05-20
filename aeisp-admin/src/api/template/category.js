import request from '@/utils/request'

export function getCategoryTree() {
  return request.get('/template-categories/tree')
}

export function getCategory(id) {
  return request.get(`/template-categories/${id}`)
}

export function createCategory(data) {
  return request.post('/template-categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/template-categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/template-categories/${id}`)
}