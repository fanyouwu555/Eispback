<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter="handleQuery" style="width: 140px" />
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
      <el-table-column prop="amount" label="金额" width="100" align="center">
        <template #default="{ row }">{{ (row.amount / 100).toFixed(2) }} 元</template>
      </el-table-column>
      <el-table-column prop="durationHours" label="时长" width="80" align="center">
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
      <el-table-column prop="orderTime" label="下单时间" width="170" align="center" />
      <el-table-column prop="payTime" label="支付时间" width="170" align="center">
        <template #default="{ row }">{{ row.payTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="View" @click="handleView(row)">详情</el-button>
          <el-button
            v-if="row.status === 1"
            type="danger"
            link
            size="small"
            icon="Coin"
            @click="handleRefund(row)"
            v-permission="'order:refund'"
          >退款</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 详情弹窗 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ (currentDetail.amount / 100).toFixed(2) }} 元</el-descriptions-item>
        <el-descriptions-item label="时长">{{ currentDetail.durationHours }} 小时</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentDetail.payType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentDetail.status === 0" type="warning" size="small">待支付</el-tag>
          <el-tag v-else-if="currentDetail.status === 1" type="success" size="small">已支付</el-tag>
          <el-tag v-else-if="currentDetail.status === 2" type="danger" size="small">已退款</el-tag>
          <el-tag v-else type="info" size="small">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentDetail.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentDetail.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.refundReason" label="退款原因" :span="2">{{ currentDetail.refundReason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 退款弹窗 -->
    <el-dialog title="订单退款" v-model="refundVisible" width="450px">
      <el-form :model="refundForm" ref="refundFormRef" label-width="100px" :rules="refundRules">
        <el-alert title="退款操作需要输入当前管理员密码" type="warning" :closable="false" class="mb-3" />
        <el-form-item label="订单号">
          <el-input :model-value="currentOrderNo" disabled />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" placeholder="请输入退款原因" maxlength="200" />
        </el-form-item>
        <el-form-item label="管理员密码" prop="adminPassword">
          <el-input v-model="refundForm.adminPassword" type="password" placeholder="请输入当前管理员密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="danger" @click="handleRefundSubmit" :loading="refundLoading">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOrders, refundOrder } from '@/api/recharge'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentDetail = ref({})
const refundVisible = ref(false)
const refundLoading = ref(false)
const currentOrderNo = ref('')
const currentRow = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: undefined,
  userId: undefined,
  status: undefined
})

const refundForm = reactive({
  reason: '',
  adminPassword: ''
})

const refundRules = {
  reason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }],
  adminPassword: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }]
}

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
  queryParams.orderNo = undefined
  queryParams.userId = undefined
  queryParams.status = undefined
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

function handleView(row) {
  currentDetail.value = row
  detailVisible.value = true
}

function handleRefund(row) {
  currentRow.value = row
  currentOrderNo.value = row.orderNo
  refundForm.reason = ''
  refundForm.adminPassword = ''
  refundVisible.value = true
}

function handleRefundSubmit() {
  refundLoading.value = true
  refundOrder(currentOrderNo.value, {
    reason: refundForm.reason,
    adminPassword: refundForm.adminPassword
  }).then(() => {
    ElMessage.success('退款成功')
    refundVisible.value = false
    getList()
  }).catch(err => {
    console.error('退款失败:', err)
  }).finally(() => {
    refundLoading.value = false
  })
}

onMounted(() => getList())
</script>