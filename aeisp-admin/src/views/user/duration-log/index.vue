<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="操作类型">
        <el-select v-model="queryParams.operationType" clearable>
          <el-option label="注册赠送" value="REGISTER_GRANT" />
          <el-option label="充值" value="RECHARGE" />
          <el-option label="管理员增加" value="ADMIN_ADD" />
          <el-option label="管理员扣减" value="ADMIN_SUBTRACT" />
          <el-option label="管理员设置" value="ADMIN_SET" />
          <el-option label="消费扣减" value="CONSUME" />
          <el-option label="退款扣减" value="REFUND_DEDUCT" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作者类型">
        <el-select v-model="queryParams.operatorType" clearable>
          <el-option label="系统自动" :value="1" />
          <el-option label="管理员" :value="2" />
          <el-option label="用户本人" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="logList" border>
      <el-table-column label="操作类型" prop="operationTypeLabel" width="140" />
      <el-table-column label="变更时长(分钟)" prop="changeMinutes" width="130" align="center">
        <template #default="{ row }">
          <el-tag :type="row.changeMinutes > 0 ? 'success' : 'danger'">
            {{ row.changeMinutes > 0 ? '+' : '' }}{{ row.changeMinutes }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="变更前" prop="previousRemaining" width="100" align="center" />
      <el-table-column label="变更后" prop="currentRemaining" width="100" align="center" />
      <el-table-column label="操作者" prop="operatorTypeLabel" width="120" />
      <el-table-column label="原因" prop="reason" show-overflow-tooltip />
      <el-table-column label="关联订单" prop="relatedOrderId" width="160" />
      <el-table-column label="变更时间" prop="createdAt" width="180" />
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
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  operationType: undefined,
  operatorType: undefined
})

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
</style>
