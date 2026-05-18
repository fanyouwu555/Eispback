<template>
  <div class="app-container">
    <el-row :gutter="20" class="mb8">
      <el-col :span="6"><el-statistic title="总消耗时长" :value="totalDuration" /></el-col>
      <el-col :span="6"><el-statistic title="今日消耗" :value="todayDuration" /></el-col>
    </el-row>
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="用户名"><el-input v-model="queryParams.username" clearable /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="queryParams.type" clearable>
          <el-option label="模型调用" value="model" />
          <el-option label="仿真运行" value="simulation" />
          <el-option label="编译调试" value="debug" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="logList" border>
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="消耗时间" prop="consumeTime" width="180" />
      <el-table-column label="类型" prop="type" />
      <el-table-column label="消耗时长" prop="duration" />
      <el-table-column label="关联项目" prop="projectName" />
      <el-table-column label="备注" prop="remark" />
    </el-table>
    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listDurationLogs } from '@/api/user'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const totalDuration = ref(0)
const todayDuration = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, username: undefined, type: undefined })

async function getList(pagination = null) {
  loading.value = true
  if (pagination) { queryParams.pageNum = pagination.page; queryParams.pageSize = pagination.limit }
  const res = await listDurationLogs(0, queryParams)
  logList.value = res.list || []
  total.value = res.total || 0
  loading.value = false
}
function handleQuery() { queryParams.pageNum = 1; getList() }
onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
.mb8 { margin-bottom: 16px; }
</style>
