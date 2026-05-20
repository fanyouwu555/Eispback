import request from '@/utils/request'

// 字典类型管理
export function listDictTypes(params) {
  return request.get('/system/dict-types', { params })
}

export function listAllDictTypes() {
  return request.get('/system/dict-types/all')
}

export function getDictType(id) {
  return request.get(`/system/dict-types/${id}`)
}

export function createDictType(data) {
  return request.post('/system/dict-types', data)
}

export function updateDictType(id, data) {
  return request.put(`/system/dict-types/${id}`, data)
}

export function deleteDictType(id) {
  return request.delete(`/system/dict-types/${id}`)
}

// 字典数据管理
export function listDictData(dictCode) {
  return request.get(`/system/dict-data/by-code/${dictCode}`)
}

export function createDictData(data) {
  return request.post('/system/dict-data', data)
}

export function updateDictData(id, data) {
  return request.put(`/system/dict-data/${id}`, data)
}

export function deleteDictData(id) {
  return request.delete(`/system/dict-data/${id}`)
}