import request from '@/utils/request'

export function listProjects(params) {
  return request.get('/projects', { params })
}

export function getProject(id) {
  return request.get(`/projects/${id}`)
}

export function archiveProject(id) {
  return request.post(`/projects/${id}/archive`)
}

export function deleteProject(id) {
  return request.delete(`/projects/${id}`)
}