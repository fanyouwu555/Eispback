<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="用户名"><el-input v-model="queryParams.username" clearable /></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="logList" border>
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="登录IP" prop="ip" />
      <el-table-column label="设备" prop="device" show-overflow-tooltip />
      <el-table-column label="结果" align="center">
        <template #default="{ row }">
          <el-tag :type="row.result === 1 ? 'success' : 'danger'">{{ row.result === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="登录时间" prop="loginTime" width="180" />
    </el-table>
    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listLoginLogs } from '@/api/user'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, username: undefined })

async function getList(pagination = null) {
  loading.value = true
  if (pagination) { queryParams.pageNum = pagination.page; queryParams.pageSize = pagination.limit }
  const res = await listLoginLogs(0, queryParams)
  logList.value = res.list || []
  total.value = res.total || 0
  loading.value = false
}
function handleQuery() { queryParams.pageNum = 1; getList() }
onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
