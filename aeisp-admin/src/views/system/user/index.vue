<template>
  <div class="app-container">
    <!-- 搜索表单 -->
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
          <el-option label="禁用" :value="2" />
          <el-option label="冻结" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="userList" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="真实姓名" prop="realName" />
      <el-table-column label="手机号" prop="phone" />
      <el-table-column label="邮箱" prop="email" />
      <el-table-column label="绑定角色" width="180">
        <template #default="{ row }">
          <span>{{ row.roles?.map(r => r.roleName).join(', ') || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : (row.status === 2 ? '禁用' : '冻结') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="registerTime" width="180" />
      <el-table-column label="登录信息" width="160">
        <template #default="{ row }">
          <span>{{ row.lastLoginIp || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="360">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="warning" icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="open" :title="title" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password placeholder="留空默认为123456" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="form.id">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="绑定角色" v-if="roleOptions.length > 0">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色">
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdOpen" title="重置密码" width="500px">
      <p>确认重置用户 "{{ currentUser?.username }}" 的密码？</p>
      <div style="margin: 15px 0;">
        <el-input v-model="adminPassword" type="password" show-password placeholder="请输入管理员密码（必填）" />
      </div>
      <div style="margin: 15px 0;">
        <el-input v-model="newPassword" type="password" show-password placeholder="输入新密码（留空自动生成）" />
      </div>
      <p v-if="generatedPassword" style="color: #f56c6c; margin-top: 10px;">
        <el-icon color="#f56c6c"><WarningFilled /></el-icon>
        新密码：{{ generatedPassword }}
      </p>
      <template #footer>
        <el-button @click="pwdOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitResetPwd">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSysUsers, getSysUser, addSysUser, updateSysUser, deleteSysUser } from '@/api/system'
import { listRoles } from '@/api/system'
import Pagination from '@/components/Pagination.vue'
import { WarningFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const queryRef = ref(null)
const formRef = ref(null)
const roleOptions = ref([])
const pwdOpen = ref(false)
const currentUser = ref(null)
const newPassword = ref('')
const adminPassword = ref('')
const generatedPassword = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: undefined,
  phone: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1,
  roleIds: []
})

const rules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }]
  // 密码不需要必填，后端会默认给123456
}

async function getList(pagination = null) {
  loading.value = true
  try {
    if (pagination) {
      queryParams.pageNum = pagination.page
      queryParams.pageSize = pagination.limit
    }
    const res = await listSysUsers(queryParams)
    userList.value = res.list || []
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
  handleQuery()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    status: 1,
    roleIds: []
  })
}

function handleAdd() {
  resetForm()
  open.value = true
  title.value = '新增用户'
}

async function handleUpdate(row) {
  resetForm()
  try {
    const detail = await getSysUser(row.id)
    Object.assign(form, {
      id: detail.id,
      username: detail.username,
      realName: detail.realName,
      phone: detail.phone,
      email: detail.email,
      status: detail.status ?? 1,
      roleIds: detail.roles?.map(r => r.id) || []
    })
  } catch {
    // 如果获取详情失败，使用表格数据
    Object.assign(form, {
      id: row.id,
      username: row.username,
      realName: row.realName,
      phone: row.phone,
      email: row.email,
      status: row.status ?? 1,
      roleIds: row.roles?.map(r => r.id) || []
    })
  }
  open.value = true
  title.value = '编辑用户'
}

function handleResetPwd(row) {
  currentUser.value = row
  newPassword.value = ''
  adminPassword.value = ''
  generatedPassword.value = ''
  pwdOpen.value = true
}

async function submitResetPwd() {
  if (!adminPassword.value) {
    ElMessage.warning('请输入管理员密码')
    return
  }
  const res = await resetUserPassword(currentUser.value.id, {
    adminPassword: adminPassword.value,
    newPassword: newPassword.value || undefined
  })
  if (res) {
    generatedPassword.value = res
    ElMessage.success('密码重置成功')
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSysUser(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  })
}

async function submitForm() {
  try {
    await formRef.value.validate()
    if (form.id) {
      // 更新管理员
      const data = {
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        status: form.status,
        roleIds: form.roleIds.length ? form.roleIds : undefined
      }
      await updateSysUser(form.id, data)
      ElMessage.success('修改成功')
    } else {
      // 新增管理员
      const data = {
        username: form.username,
        password: form.password || undefined,
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        roleIds: form.roleIds.length ? form.roleIds : undefined
      }
      await addSysUser(data)
      ElMessage.success('新增成功')
    }
    open.value = false
    getList()
  } catch (error) {
    console.error('用户操作失败:', error)
    // 显示具体的错误信息给用户
    const errorMessage = error.message || error.response?.data?.message || '操作失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

onMounted(() => {
  getList()
  listRoles().then(res => { roleOptions.value = Array.isArray(res) ? res : (res.list || []) }).catch(() => {})
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.mb8 {
  margin-bottom: 8px;
}
</style>
