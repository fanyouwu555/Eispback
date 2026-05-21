<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="操作人" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入操作人" clearable />
      </el-form-item>
      <el-form-item label="所属模块" prop="module">
        <el-input v-model="queryParams.module" placeholder="请输入模块名称" clearable />
      </el-form-item>
      <el-form-item label="操作类型" prop="operationType">
        <el-select v-model="queryParams.operationType" clearable placeholder="请选择">
          <el-option label="新增" value="新增" />
          <el-option label="编辑" value="编辑" />
          <el-option label="删除" value="删除" />
          <el-option label="导出" value="导出" />
          <el-option label="配置" value="配置" />
        </el-select>
      </el-form-item>
      <el-form-item label="结果" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Download" @click="handleExport">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="logList" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="操作人" prop="operatorUsername" width="120" />
      <el-table-column label="角色" prop="roleName" width="120" />
      <el-table-column label="操作对象" width="130">
        <template #default="{ row }">
          <span>{{ row.targetType ? row.targetType + (row.targetId ? '#' + row.targetId : '') : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" prop="operationTypeLabel" />
      <el-table-column label="IP地址" prop="ipAddress" width="140" />
      <el-table-column label="结果" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗时(ms)" prop="duration" width="100" />
      <el-table-column label="操作时间" prop="createdAt" width="180" />
      <el-table-column label="操作" align="center" width="80">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" title="操作详情" width="650px">
      <el-form label-width="100px">
        <el-form-item label="操作人">{{ detailRow?.operatorUsername }}</el-form-item>
        <el-form-item label="角色">{{ detailRow?.roleName || '-' }}</el-form-item>
        <el-form-item label="请求方法">
          <el-tag>{{ detailRow?.requestMethod || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="请求URL">
          <el-input :model-value="detailRow?.requestUrl" readonly type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="请求参数">
          <el-input :model-value="formatJson(detailRow?.requestParams)" readonly type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="响应结果">
          <el-input :model-value="formatJson(detailRow?.responseData)" readonly type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item v-if="detailRow?.errorMsg" label="错误信息">
          <el-input :model-value="detailRow?.errorMsg" readonly type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="执行耗时">{{ detailRow?.duration }} ms</el-form-item>
        <el-form-item label="操作时间">{{ detailRow?.createdAt }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listLogs, exportLogs } from '@/api/system'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])
const queryRef = ref(null)
const detailOpen = ref(false)
const detailRow = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: undefined,
  module: undefined,
  operationType: undefined,
  status: undefined
})

function formatJson(val) {
  if (!val) return '-'
  try {
    return typeof val === 'string' ? JSON.stringify(JSON.parse(val), null, 2) : JSON.stringify(val, null, 2)
  } catch {
    return val
  }
}

function handleDetail(row) {
  detailRow.value = row
  detailOpen.value = true
}

async function getList(pagination = null) {
  loading.value = true
  try {
    if (pagination) {
      queryParams.pageNum = pagination.page
      queryParams.pageSize = pagination.limit
    }
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0] + ' 00:00:00'
      params.endTime = dateRange.value[1] + ' 23:59:59'
    }
    const res = await listLogs(params)
    logList.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取操作日志失败:', e)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  dateRange.value = []
  handleQuery()
}

async function handleExport() {
  const params = { ...queryParams }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0] + ' 00:00:00'
    params.endTime = dateRange.value[1] + ' 23:59:59'
  }
  try {
    const res = await exportLogs(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `操作日志_${new Date().toISOString().slice(0, 19).replace(/[T:]/g, '')}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('导出失败:', e)
  }
}

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>