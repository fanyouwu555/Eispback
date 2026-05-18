<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="操作人" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入操作人" clearable />
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="logList" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="操作人" prop="username" width="120" />
      <el-table-column label="IP地址" prop="ip" width="140" />
      <el-table-column label="操作模块" prop="module" />
      <el-table-column label="操作类型" prop="operation" />
      <el-table-column label="结果" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗时(ms)" prop="duration" width="100" />
      <el-table-column label="操作时间" prop="createdAt" width="180" />
    </el-table>

    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listLogs } from '@/api/system'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])
const queryRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: undefined
})

async function getList(pagination = null) {
  loading.value = true
  try {
    if (pagination) {
      queryParams.pageNum = pagination.page
      queryParams.pageSize = pagination.limit
    }
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await listLogs(params)
    logList.value = res.list || []
    total.value = res.total || 0
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

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
