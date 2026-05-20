<template>
  <div class="app-container">
    <!-- 查询用户 -->
    <el-form :inline="true">
      <el-form-item label="用户ID">
        <el-input v-model="userId" placeholder="请输入用户ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" :disabled="!userId" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 余额信息卡片 -->
    <el-card v-if="queried" class="mb-4" shadow="hover">
      <template #header>
        <span><strong>用户余额信息</strong> — 用户ID: {{ userId }}</span>
      </template>
      <el-row :gutter="24">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">当前余额</div>
            <div class="stat-value text-primary">{{ (balanceInfo.balance / 100).toFixed(2) }} 元</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">剩余时长</div>
            <div class="stat-value text-success">{{ balanceInfo.remainingDuration || 0 }} 分钟</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">&nbsp;</div>
            <div class="stat-value">
              <el-button type="primary" icon="Plus" @click="openRecharge">充值</el-button>
              <el-button type="warning" icon="Remove" @click="openDeduct" v-permission="'finance:balance:adjust'">扣减</el-button>
              <el-button type="info" icon="Edit" @click="openAdjust" v-permission="'finance:balance:adjust'">调整</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 充值弹窗 -->
    <el-dialog title="余额充值" v-model="rechargeVisible" width="400px">
      <el-form :model="rechargeForm" label-width="100px" :rules="rechargeRules" ref="rechargeFormRef">
        <el-form-item label="用户ID">
          <el-input :model-value="userId" disabled />
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="1" :step="100" style="width: 200px" />
          <span class="ml-2">分</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge" :loading="rechargeLoading">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- 扣减弹窗 -->
    <el-dialog title="余额扣减" v-model="deductVisible" width="420px">
      <el-form :model="deductForm" label-width="100px" :rules="deductRules" ref="deductFormRef">
        <el-alert title="扣减操作需要输入当前管理员密码" type="warning" :closable="false" class="mb-3" />
        <el-form-item label="用户ID">
          <el-input :model-value="userId" disabled />
        </el-form-item>
        <el-form-item label="扣减金额" prop="amount">
          <el-input-number v-model="deductForm.amount" :min="1" :step="100" style="width: 200px" />
          <span class="ml-2">分</span>
        </el-form-item>
        <el-form-item label="扣减原因" prop="reason">
          <el-input v-model="deductForm.reason" type="textarea" :rows="2" placeholder="请输入扣减原因" maxlength="200" />
        </el-form-item>
        <el-form-item label="管理员密码" prop="adminPassword">
          <el-input v-model="deductForm.adminPassword" type="password" placeholder="请输入当前管理员密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deductVisible = false">取消</el-button>
        <el-button type="warning" @click="handleDeduct" :loading="deductLoading">确认扣减</el-button>
      </template>
    </el-dialog>

    <!-- 手动调整弹窗 -->
    <el-dialog title="手动调整余额" v-model="adjustVisible" width="420px">
      <el-form :model="adjustForm" label-width="100px" :rules="adjustRules" ref="adjustFormRef">
        <el-alert title="调整操作需要输入当前管理员密码。正数增加，负数减少。" type="warning" :closable="false" class="mb-3" />
        <el-form-item label="用户ID">
          <el-input :model-value="userId" disabled />
        </el-form-item>
        <el-form-item label="调整金额" prop="delta">
          <el-input-number v-model="adjustForm.delta" :step="100" style="width: 200px" />
          <span class="ml-2">分</span>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="2" placeholder="请输入调整原因" maxlength="200" />
        </el-form-item>
        <el-form-item label="操作人ID" prop="operatorId">
          <el-input-number v-model="adjustForm.operatorId" :min="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="管理员密码" prop="adminPassword">
          <el-input v-model="adjustForm.adminPassword" type="password" placeholder="请输入当前管理员密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjust" :loading="adjustLoading">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getBalance, rechargeBalance, deductBalance, adjustBalance } from '@/api/recharge'

const userId = ref('')
const queried = ref(false)
const balanceInfo = ref({})
const rechargeVisible = ref(false)
const rechargeLoading = ref(false)
const deductVisible = ref(false)
const deductLoading = ref(false)
const adjustVisible = ref(false)
const adjustLoading = ref(false)

const rechargeForm = reactive({ amount: 100 })
const deductForm = reactive({ amount: 100, reason: '', adminPassword: '' })
const adjustForm = reactive({ delta: 0, reason: '', operatorId: 1, adminPassword: '' })

const rechargeRules = { amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }] }
const deductRules = {
  amount: [{ required: true, message: '请输入扣减金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入扣减原因', trigger: 'blur' }],
  adminPassword: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }]
}
const adjustRules = {
  delta: [{ required: true, message: '请输入调整金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }],
  operatorId: [{ required: true, message: '请输入操作人ID', trigger: 'blur' }],
  adminPassword: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }]
}

function handleQuery() {
  if (!userId.value) return
  getBalance(userId.value).then(res => {
    balanceInfo.value = res
    queried.value = true
  })
}

function handleReset() {
  userId.value = ''
  queried.value = false
  balanceInfo.value = {}
}

function openRecharge() {
  rechargeForm.amount = 100
  rechargeVisible.value = true
}

function handleRecharge() {
  rechargeLoading.value = true
  rechargeBalance(userId.value, { amount: rechargeForm.amount }).then(() => {
    ElMessage.success('充值成功')
    rechargeVisible.value = false
    handleQuery()
  }).finally(() => {
    rechargeLoading.value = false
  })
}

function openDeduct() {
  deductForm.amount = 100
  deductForm.reason = ''
  deductForm.adminPassword = ''
  deductVisible.value = true
}

function handleDeduct() {
  deductLoading.value = true
  deductBalance(userId.value, {
    amount: deductForm.amount,
    reason: deductForm.reason,
    adminPassword: deductForm.adminPassword
  }).then(() => {
    ElMessage.success('扣减成功')
    deductVisible.value = false
    handleQuery()
  }).catch(() => {}).finally(() => {
    deductLoading.value = false
  })
}

function openAdjust() {
  adjustForm.delta = 0
  adjustForm.reason = ''
  adjustForm.operatorId = 1
  adjustForm.adminPassword = ''
  adjustVisible.value = true
}

function handleAdjust() {
  adjustLoading.value = true
  adjustBalance(userId.value, {
    delta: adjustForm.delta,
    reason: adjustForm.reason,
    operatorId: adjustForm.operatorId,
    adminPassword: adjustForm.adminPassword
  }).then(() => {
    ElMessage.success('调整成功')
    adjustVisible.value = false
    handleQuery()
  }).catch(() => {}).finally(() => {
    adjustLoading.value = false
  })
}
</script>

<style scoped>
.stat-item { text-align: center; padding: 12px 0; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; }
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }
.mb-4 { margin-bottom: 16px; }
.mb-3 { margin-bottom: 12px; }
.ml-2 { margin-left: 8px; }
</style>