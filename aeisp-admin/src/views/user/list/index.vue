<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
          <el-option label="冻结" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="userList" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="手机号" prop="phone" />
      <el-table-column label="邮箱" prop="email" />
      <el-table-column label="状态" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'">
            {{ row.status === 1 ? '正常' : row.status === 2 ? '冻结' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="剩余时长" prop="remainingDuration" />
      <el-table-column label="充值总额" prop="totalRecharge" />
      <el-table-column label="最后登录" prop="lastLoginTime" width="180" />
      <el-table-column label="操作" align="center" width="280">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="warning" icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
          <el-button link type="success" icon="Timer" @click="handleAdjust(row)">调整时长</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑弹窗 -->
    <el-dialog v-model="open" title="编辑用户" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
            <el-option label="冻结" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitUpdate">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdOpen" title="重置密码" width="400px">
      <p>确认重置用户 "{{ currentUser?.username }}" 的密码？</p>
      <p v-if="newPassword" style="color: #f56c6c; margin-top: 10px;">新密码：{{ newPassword }}</p>
      <template #footer>
        <el-button @click="pwdOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitReset">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 调整时长弹窗 -->
    <el-dialog v-model="durationOpen" title="调整时长" width="400px">
      <el-form :model="durationForm" label-width="80px">
        <el-form-item label="调整数值">
          <el-input-number v-model="durationForm.amount" :min="-99999" :max="99999" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="durationForm.reason" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="durationOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitAdjust">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers, updateUser, resetUserPassword, adjustDuration } from '@/api/user'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const open = ref(false)
const pwdOpen = ref(false)
const durationOpen = ref(false)
const queryRef = ref(null)
const currentUser = ref(null)
const newPassword = ref('')

const queryParams = reactive({ pageNum: 1, pageSize: 10, username: undefined, phone: undefined, status: undefined })
const form = reactive({ id: undefined, username: '', phone: '', email: '', status: 1 })
const durationForm = reactive({ amount: 0, reason: '' })

async function getList(pagination = null) {
  loading.value = true
  try {
    if (pagination) { queryParams.pageNum = pagination.page; queryParams.pageSize = pagination.limit }
    const res = await listUsers(queryParams)
    userList.value = res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

function handleUpdate(row) { Object.assign(form, row); open.value = true }
async function submitUpdate() {
  await updateUser(form.id, { phone: form.phone, email: form.email, status: form.status })
  ElMessage.success('修改成功'); open.value = false; getList()
}

function handleResetPwd(row) { currentUser.value = row; newPassword.value = ''; pwdOpen.value = true }
async function submitReset() {
  const res = await resetUserPassword(currentUser.value.id)
  newPassword.value = res || '已重置'
  ElMessage.success('密码重置成功')
}

function handleAdjust(row) { currentUser.value = row; durationForm.amount = 0; durationForm.reason = ''; durationOpen.value = true }
async function submitAdjust() {
  await adjustDuration(currentUser.value.id, durationForm)
  ElMessage.success('时长调整成功'); durationOpen.value = false; getList()
}

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
