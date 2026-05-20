import request from '@/utils/request'

export function listNotifications(params) {
  return request.get('/messages', { params })
}

export function getNotification(id) {
  return request.get(`/messages/${id}`)
}

export function createNotification(data) {
  return request.post('/messages', data)
}

export function pushNotification(id) {
  return request.post(`/messages/${id}/push`)
}

export function revokeNotification(id) {
  return request.post(`/messages/${id}/revoke`)
}

export function archiveNotification(id) {
  return request.post(`/messages/${id}/archive`)
}

export function toggleTop(id, isTop) {
  return request.post(`/messages/${id}/top`, null, { params: { isTop } })
}