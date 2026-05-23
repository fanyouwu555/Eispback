import request from '@/utils/request'

// ===== 模板管理 =====
export function listTemplates(params) {
  return request.get('/templates', { params })
}

export function getTemplate(id) {
  return request.get(`/templates/${id}`)
}

export function createTemplate(data) {
  return request.post('/templates', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function updateTemplate(id, data) {
  return request.put(`/templates/${id}`, data)
}

export function deleteTemplate(id) {
  return request.delete(`/templates/${id}`)
}

export function toggleTemplateStatus(id, status) {
  return request.post(`/templates/${id}/status`, null, { params: { status } })
}

// ===== 版本管理 =====
export function uploadTemplateVersion(id, data) {
  return request.post(`/templates/${id}/versions`, data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function rollbackTemplateVersion(id, versionId) {
  return request.post(`/templates/${id}/rollback/${versionId}`)
}

// ===== 文件管理 =====
export function listTemplateFiles(id, versionNo) {
  return request.get(`/templates/${id}/files`, { params: { versionNo } })
}

export function getTemplateFileContent(id, versionNo, filePath) {
  return request.get(`/templates/${id}/files/content`, {
    params: { versionNo, filePath },
    responseType: 'blob'
  })
}

export function downloadTemplateZip(id, versionNo) {
  return request.get(`/templates/${id}/download`, {
    params: { versionNo },
    responseType: 'blob'
  })
}

// ===== 统计 =====
export function getTemplateStatistics() {
  return request.get('/templates/statistics')
}

export function getUserTemplateUsageLogs(userId) {
  return request.get(`/templates/usage-logs/${userId}`)
}

// ===== 封面图上传 =====
export function uploadTemplateCover(id, data) {
  return request.post(`/templates/${id}/cover`, data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ===== 违规标记 =====
export function markTemplateViolation(id, reason) {
  return request.post(`/templates/${id}/violation`, null, { params: { reason } })
}

// ===== 购买与权限 =====
export function purchaseTemplate(id) {
  return request.post(`/templates/${id}/purchase`)
}

export function checkTemplateAccess(id) {
  return request.get(`/templates/${id}/access`)
}

// ===== 预览图片 =====
export function listPreviewImages(templateId) {
  return request.get(`/templates/${templateId}/preview-images`)
}

export function addPreviewImage(templateId, data) {
  return request.post(`/templates/${templateId}/preview-images`, null, { params: data })
}

export function deletePreviewImage(templateId, id) {
  return request.delete(`/templates/${templateId}/preview-images/${id}`)
}

export function updatePreviewImageSort(templateId, id, sortOrder) {
  return request.put(`/templates/${templateId}/preview-images/${id}/sort`, null, { params: { sortOrder } })
}