<template>
  <div class="app-container">
    <el-alert title="购买记录展示了用户通过套餐充值购买的完整订单历史，包括待支付、已支付、已退款和已取消的订单。" type="info" :closable="false" class="mb-4" />

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter="handleQuery" style="width: 140px" />
      </el-form-item>
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="订单状态" clearable style="width: 130px">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已退款" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="orderList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column label="金额" width="100" align="center">
        <template #default="{ row }">{{ (row.amount / 100).toFixed(2) }} 元</template>
      </el-table-column>
      <el-table-column label="到账时长" width="100" align="center">
        <template #default="{ row }">{{ row.durationHours }} 小时</template>
      </el-table-column>
      <el-table-column prop="payType" label="支付方式" width="100" align="center">
        <template #default="{ row }">{{ row.payType || '-' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning" size="small">待支付</el-tag>
          <el-tag v-else-if="row.status === 1" type="success" size="small">已支付</el-tag>
          <el-tag v-else-if="row.status === 2" type="danger" size="small">已退款</el-tag>
          <el-tag v-else type="info" size="small">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="购买时间" width="170" align="center" />
      <el-table-column prop="payTime" label="支付时间" width="170" align="center">
        <template #default="{ row }">{{ row.payTime || '-' }}</template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listOrders } from '@/api/recharge'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const orderList = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userId: undefined,
  orderNo: undefined,
  status: undefined
})

function getList(page) {
  loading.value = true
  if (page) {
    queryParams.pageNum = page.pageNum
    queryParams.pageSize = page.pageSize
  }
  listOrders(queryParams).then(res => {
    orderList.value = res.list
    total.value = res.total
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.userId = undefined
  queryParams.orderNo = undefined
  queryParams.status = undefined
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

onMounted(() => getList())
</script>