<template>
  <div class="app-container">
    <!-- 选择用户 -->
    <el-form :inline="true">
      <el-form-item label="用户ID">
        <el-input v-model="userId" placeholder="请输入用户ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" :disabled="!userId" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 权限定义列表 -->
    <el-card v-if="permissionKeys.length > 0" class="mb-4">
      <template #header>权限模板定义</template>
      <el-table :data="permissionKeys" stripe border size="small">
        <el-table-column prop="key" label="权限键" width="220" />
        <el-table-column prop="label" label="权限名称" min-width="160" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'boolean' ? 'primary' : 'warning'" size="small">{{ row.type === 'boolean' ? '开关' : '数值' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultValue" label="默认值" width="100" />
      </el-table>
    </el-card>

    <!-- 用户权限编辑 -->
    <el-card v-if="queried" v-loading="loading">
      <template #header>
        <span><strong>用户权限编辑</strong> — 用户ID: {{ userId }}</span>
      </template>

      <el-form :model="permForm" label-width="180px">
        <el-form-item
          v-for="keyDef in permissionKeys"
          :key="keyDef.key"
          :label="keyDef.label"
        >
          <template v-if="keyDef.type === 'boolean'">
            <el-switch
              v-model="permForm[keyDef.key]"
              :active-value="'true'"
              :inactive-value="'false'"
            />
            <span class="ml-2">{{ permForm[keyDef.key] === 'true' ? '开启' : '关闭' }}</span>
          </template>
          <template v-else>
            <el-input-number
              v-model="permForm[keyDef.key]"
              :min="0"
              :max="99999"
              style="width: 160px"
            />
          </template>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Check" @click="handleSave" :loading="saving">保存权限</el-button>
          <el-button icon="Refresh" @click="handleReset">恢复</el-button>
          <el-button type="danger" icon="Delete" @click="handleClear">重置为默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 权限变更日志 -->
    <el-card v-if="queried && permLogs.length > 0" class="mb-4">
      <template #header>权限变更日志</template>
      <el-table :data="permLogs" stripe border size="small">
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="permKey" label="权限键" width="180" />
        <el-table-column prop="oldValue" label="变更前" width="120" />
        <el-table-column prop="newValue" label="变更后" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.operationType === 'UPDATE'" size="small">修改</el-tag>
            <el-tag v-else-if="row.operationType === 'RESET'" size="small" type="danger">重置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
      </el-table>
    </el-card>

    <el-empty v-if="queried && permissionKeys.length === 0" description="未加载到权限定义" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserPermissions, updateUserPermissions, resetUserPermissions, getPermissionLogs, getPermissionKeys } from '@/api/user/permission'

const userId = ref('')
const queried = ref(false)
const loading = ref(false)
const saving = ref(false)
const permissionKeys = ref([])
const permForm = reactive({})
const originalForm = reactive({})
const permLogs = ref([])

onMounted(() => {
  getPermissionKeys().then(res => {
    permissionKeys.value = res || []
  })
})

function handleQuery() {
  if (!userId.value) return
  loading.value = true
  queried.value = true
  Promise.all([
    getUserPermissions(userId.value),
    getPermissionLogs(userId.value)
  ]).then(([permsRes, logsRes]) => {
    const perms = permsRes || []
    permissionKeys.value.forEach(k => {
      const existing = perms.find(p => p.permKey === k.key)
      permForm[k.key] = existing ? existing.permValue : k.defaultValue
      originalForm[k.key] = permForm[k.key]
    })
    permLogs.value = logsRes || []
  }).finally(() => {
    loading.value = false
  })
}

function resetQuery() {
  userId.value = ''
  queried.value = false
  permissionKeys.value.forEach(k => { permForm[k.key] = undefined })
}

function handleSave() {
  saving.value = true
  const permissions = permissionKeys.value.map(k => ({
    permKey: k.key,
    permValue: String(permForm[k.key] ?? k.defaultValue)
  }))
  updateUserPermissions(userId.value, { userId: Number(userId.value), permissions }).then(() => {
    ElMessage.success('权限保存成功')
  }).finally(() => {
    saving.value = false
  })
}

function handleReset() {
  permissionKeys.value.forEach(k => {
    permForm[k.key] = originalForm[k.key]
  })
}

function handleClear() {
  resetUserPermissions(userId.value).then(() => {
    ElMessage.success('权限已重置为默认值')
    permissionKeys.value.forEach(k => {
      permForm[k.key] = k.defaultValue
      originalForm[k.key] = k.defaultValue
    })
    return getPermissionLogs(userId.value)
  }).then(logs => {
    permLogs.value = logs || []
  })
}
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.ml-2 { margin-left: 8px; }
</style>