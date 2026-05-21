<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="用户ID"><el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable style="width:160px" /></el-form-item>
      <el-form-item label="登录账号"><el-input v-model="queryParams.loginAccount" clearable /></el-form-item>
      <el-form-item label="登录时间">
        <el-date-picker
          v-model="timeRange"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="onTimeRangeChange"
        />
      </el-form-item>
      <el-form-item label="登录类型">
        <el-select v-model="queryParams.loginType" clearable>
          <el-option
            v-for="item in loginTypeOptions"
            :key="item.itemValue"
            :label="item.itemLabel"
            :value="Number(item.itemValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="登录结果">
        <el-select v-model="queryParams.loginResult" clearable>
          <el-option
            v-for="item in loginResultOptions"
            :key="item.itemValue"
            :label="item.itemLabel"
            :value="Number(item.itemValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="logList" border :row-class-name="getRowClass">
      <el-table-column label="登录账号" prop="loginAccount" />
      <el-table-column label="登录时间" prop="createdAt" width="180" />
      <el-table-column label="登录类型" prop="loginType" width="120">
        <template #default="{ row }">
          <el-tag :type="loginTypeColor(row.loginType) || ''">{{ loginTypeLabel(row.loginType) || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="登录IP" prop="ipAddress" width="140" />
      <el-table-column label="设备类型" prop="deviceType" width="120" />
      <el-table-column label="浏览器" prop="browserInfo" show-overflow-tooltip />
      <el-table-column label="结果" align="center" width="120">
        <template #default="{ row }">
          <el-tag :type="loginResultColor(row.loginResult) || 'info'">{{ loginResultLabel(row.loginResult) || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="80">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" title="登录详情" width="500px">
      <el-form label-width="120px">
        <el-form-item label="登录账号">{{ detailRow?.loginAccount }}</el-form-item>
        <el-form-item label="登录时间">{{ detailRow?.createdAt }}</el-form-item>
        <el-form-item label="登录结果">
          <el-tag :type="loginResultColor(detailRow?.loginResult) || 'info'">{{ loginResultLabel(detailRow?.loginResult) || '未知' }}</el-tag>
        </el-form-item>
        <el-form-item label="IP 地址">{{ detailRow?.ipAddress }}</el-form-item>
        <el-form-item label="设备类型">{{ detailRow?.deviceType || '-' }}</el-form-item>
        <el-form-item label="操作系统">{{ detailRow?.osInfo || '-' }}</el-form-item>
        <el-form-item label="浏览器">{{ detailRow?.browserInfo || '-' }}</el-form-item>
        <el-form-item label="设备ID">{{ detailRow?.deviceId || '-' }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listLoginLogs } from '@/api/user'
import { useDict } from '@/composables/useDict'
import Pagination from '@/components/Pagination.vue'

const { options: loginTypeOptions, label: loginTypeLabel, color: loginTypeColor } = useDict('login_type')
const { options: loginResultOptions, label: loginResultLabel, color: loginResultColor } = useDict('login_result')

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const detailOpen = ref(false)
const detailRow = ref(null)
const timeRange = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userId: undefined,
  loginAccount: undefined,
  loginType: undefined,
  loginResult: undefined,
  createdAtStart: undefined,
  createdAtEnd: undefined
})

function onTimeRangeChange(range) {
  if (range) {
    queryParams.createdAtStart = range[0]
    queryParams.createdAtEnd = range[1]
  } else {
    queryParams.createdAtStart = undefined
    queryParams.createdAtEnd = undefined
  }
}

function getRowClass({ row }) {
  return row.loginResult && row.loginResult !== 1 ? 'danger-row' : ''
}

function handleDetail(row) {
  detailRow.value = row
  detailOpen.value = true
}

async function getList(pagination = null) {
  if (!queryParams.userId) return
  loading.value = true
  if (pagination) { queryParams.pageNum = pagination.page; queryParams.pageSize = pagination.limit }
  const res = await listLoginLogs(queryParams.userId, queryParams)
  logList.value = res.list || []
  total.value = res.total || 0
  loading.value = false
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() {
  timeRange.value = null
  queryParams.createdAtStart = undefined
  queryParams.createdAtEnd = undefined
  queryParams.userId = undefined
  queryParams.loginAccount = undefined
  queryParams.loginType = undefined
  queryParams.loginResult = undefined
  handleQuery()
}
onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
<style>
.el-table .danger-row { background-color: #fef0f0; }
</style>