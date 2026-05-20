import request from '@/utils/request'

// ===== 套餐管理 =====
export function listPackages(params) {
  return request.get('/recharge/packages', { params })
}

export function getPackage(id) {
  return request.get(`/recharge/packages/${id}`)
}

export function createPackage(data) {
  return request.post('/recharge/packages', data)
}

export function updatePackage(id, data) {
  return request.put(`/recharge/packages/${id}`, data)
}

export function deletePackage(id) {
  return request.delete(`/recharge/packages/${id}`)
}

export function listActivePackages() {
  return request.get('/recharge/packages/active')
}

// ===== 订单管理 =====
export function listOrders(params) {
  return request.get('/recharge/orders', { params })
}

export function refundOrder(orderNo, data) {
  return request.post(`/recharge/orders/${orderNo}/refund`, null, { params: data })
}

// ===== 余额管理 =====
export function getBalance(userId) {
  return request.get(`/recharge/balance/${userId}`)
}

export function rechargeBalance(userId, data) {
  return request.post(`/recharge/balance/${userId}/recharge`, null, { params: data })
}

export function deductBalance(userId, data) {
  return request.post(`/recharge/balance/${userId}/deduct`, null, { params: data })
}

export function adjustBalance(userId, data) {
  return request.post(`/recharge/balance/${userId}/adjust`, null, { params: data })
}

// ===== 时长管理 =====
export function getDurationStats(userId) {
  return request.get(`/recharge/duration/${userId}/stats`)
}

export function listDurationConsumes(userId, params) {
  return request.get(`/recharge/duration/${userId}/consumes`, { params })
}