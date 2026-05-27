<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="存储路径" name="storage" />
      <el-tab-pane label="云端备份" name="backup" />
      <el-tab-pane label="平台信息" name="platform" />
      <el-tab-pane label="超时参数" name="timeout" />
      <el-tab-pane label="阈值预警" name="threshold" />
    </el-tabs>

    <el-table :data="configList" stripe style="width: 100%">
      <el-table-column label="配置项" prop="description" min-width="180" />
      <el-table-column label="配置值" min-width="300">
        <template #default="{ row }">
          <el-switch
            v-if="row.fieldType === 'boolean'"
            v-model="row.configValue"
            :disabled="row.isEditable === 0"
            active-value="true"
            inactive-value="false"
            @change="handleQuickSave(row)"
          />
          <el-input-number
            v-else-if="row.fieldType === 'number'"
            v-model="row.configValue"
            :disabled="row.isEditable === 0"
            controls-position="right"
            @change="handleQuickSave(row)"
          />
          <el-input
            v-else
            v-model="row.configValue"
            :disabled="row.isEditable === 0"
            @blur="handleQuickSave(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :disabled="row.isEditable === 0" @click="handleEdit(row)">编辑</el-button>
          <el-button type="info" link @click="handleShowHistory(row)">历史</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑配置" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="配置键">
          <el-input v-model="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-switch
            v-if="editForm.fieldType === 'boolean'"
            v-model="editForm.configValue"
            active-value="true"
            inactive-value="false"
          />
          <el-input-number
            v-else-if="editForm.fieldType === 'number'"
            v-model="editForm.configValue"
            controls-position="right"
          />
          <el-input v-else v-model="editForm.configValue" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="editForm.description" disabled type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- History Dialog -->
    <el-dialog v-model="historyVisible" title="变更历史" width="700px">
      <el-table :data="historyList" stripe>
        <el-table-column label="原值" prop="oldValue" min-width="150" />
        <el-table-column label="新值" prop="newValue" min-width="150" />
        <el-table-column label="操作人" width="120">
          <template #default="{ row }">
            {{ row.operatorName || row.operatedBy }}
          </template>
        </el-table-column>
        <el-table-column label="操作时间" prop="operatedAt" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listConfigsByCategory, listConfigLogs, updateConfig } from '@/api/system'
import { ElMessage } from 'element-plus'

const activeTab = ref('storage')
const configList = ref([])
const dialogVisible = ref(false)
const historyVisible = ref(false)
const historyList = ref([])
const editForm = ref({
  id: null,
  configKey: '',
  configValue: '',
  description: '',
  fieldType: 'text'
})

const fetchConfigs = async (category) => {
  const res = await listConfigsByCategory(category)
  configList.value = res || []
}

const handleTabClick = () => {
  fetchConfigs(activeTab.value)
}

const handleQuickSave = async (row) => {
  if (row.isEditable === 0) return
  try {
    await updateConfig(row.configKey, { configValue: String(row.configValue) })
    ElMessage.success('已保存')
  } catch {
    fetchConfigs(activeTab.value)
  }
}

const handleEdit = (row) => {
  if (row.isEditable === 0) return
  editForm.value = {
    id: row.id,
    configKey: row.configKey,
    configValue: row.configValue,
    description: row.description,
    fieldType: row.fieldType
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  await updateConfig(editForm.value.configKey, { configValue: String(editForm.value.configValue) })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchConfigs(activeTab.value)
}

const handleShowHistory = async (row) => {
  const res = await listConfigLogs('config', row.id)
  historyList.value = res || []
  historyVisible.value = true
}

onMounted(() => {
  fetchConfigs(activeTab.value)
})
</script>