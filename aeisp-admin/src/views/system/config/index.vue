<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="configList" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="配置名称" prop="configKey" />
      <el-table-column label="配置值" prop="configValue" />
      <el-table-column label="备注" prop="description" />
      <el-table-column label="操作" align="center" width="120">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="open" title="编辑配置" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置名称">
          <el-input v-model="form.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="form.configValue" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.description" type="textarea" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listConfigs, updateConfig } from '@/api/system'

const loading = ref(false)
const configList = ref([])
const open = ref(false)
const form = reactive({ configKey: '', configValue: '', description: '' })

async function getList() {
  loading.value = true
  try {
    const res = await listConfigs()
    configList.value = res.list || []
  } finally {
    loading.value = false
  }
}

function handleUpdate(row) {
  Object.assign(form, row)
  open.value = true
}

async function submitForm() {
  await updateConfig(form.configKey, { configValue: form.configValue })
  ElMessage.success('修改成功')
  open.value = false
  getList()
}

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
