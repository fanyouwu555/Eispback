<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="登录账号"><el-input v-model="queryParams.loginAccount" clearable /></el-form-item>
      <el-form-item label="登录类型">
        <el-select v-model="queryParams.loginType" clearable>
          <el-option label="密码登录" :value="1" />
          <el-option label="验证码登录" :value="2" />
          <el-option label="Token刷新" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="登录结果">
        <el-select v-model="queryParams.loginResult" clearable>
          <el-option label="成功" :value="1" />
          <el-option label="密码错误" :value="2" />
          <el-option label="账号不存在" :value="3" />
          <el-option label="账号禁用" :value="4" />
          <el-option label="账号冻结" :value="5" />
          <el-option label="账号锁定" :value="6" />
          <el-option label="验证码错误" :value="7" />
          <el-option label="Token过期" :value="8" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="logList" border>
      <el-table-column label="登录账号" prop="loginAccount" />
      <el-table-column label="登录类型" prop="loginType" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.loginType === 1">密码登录</el-tag>
          <el-tag v-else-if="row.loginType === 2" type="success">验证码登录</el-tag>
          <el-tag v-else-if="row.loginType === 3" type="info">Token刷新</el-tag>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column label="登录IP" prop="ipAddress" width="140" />
      <el-table-column label="设备类型" prop="deviceType" width="120" />
      <el-table-column label="浏览器" prop="browserInfo" show-overflow-tooltip />
      <el-table-column label="结果" align="center" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.loginResult === 1" type="success">成功</el-tag>
          <el-tag v-else-if="row.loginResult === 2" type="danger">密码错误</el-tag>
          <el-tag v-else-if="row.loginResult === 3" type="warning">账号不存在</el-tag>
          <el-tag v-else-if="row.loginResult === 4" type="warning">账号禁用</el-tag>
          <el-tag v-else-if="row.loginResult === 5" type="warning">账号冻结</el-tag>
          <el-tag v-else-if="row.loginResult === 6" type="warning">账号锁定</el-tag>
          <el-tag v-else-if="row.loginResult === 7" type="danger">验证码错误</el-tag>
          <el-tag v-else-if="row.loginResult === 8" type="info">Token过期</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="登录时间" prop="createdAt" width="180" />
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
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  loginAccount: undefined,
  loginType: undefined,
  loginResult: undefined
})

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
