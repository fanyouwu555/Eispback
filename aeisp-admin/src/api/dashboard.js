import request from '@/utils/request'

/**
 * 获取仪表盘汇总数据。
 * @returns {Promise<{user:object, asset:object, project:object, template:object, ai:object}>}
 */
export function getDashboardSummary() {
  return request.get('/dashboard/summary')
}

/**
 * 获取趋势数据。
 * @param {string} category user/recharge/project/ai
 * @param {number} days 天数，默认30
 * @returns {Promise<{dates:string[], ...}>}
 */
export function getDashboardTrends(category, days = 30) {
  return request.get('/dashboard/trends', { params: { category, days } })
}