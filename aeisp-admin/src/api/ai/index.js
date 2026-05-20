import request from '@/utils/request'

// ===== AI会话管理 =====
export function listAiSessions(params) {
  return request.get('/ai/sessions', { params })
}

export function getAiSession(id) {
  return request.get(`/ai/sessions/${id}`)
}

export function listAiMessages(sessionId) {
  return request.get(`/ai/sessions/${sessionId}/messages`)
}

export function archiveAiSession(id) {
  return request.post(`/ai/sessions/${id}/archive`)
}

export function deleteAiSession(id) {
  return request.delete(`/ai/sessions/${id}`)
}