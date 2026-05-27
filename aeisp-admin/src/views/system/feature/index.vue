<template>
  <div class="app-container">
    <!-- Maintenance Mode Card -->
    <el-card class="maintenance-card" :class="{ 'maintenance-active': maintenanceEnabled }" shadow="hover">
      <div class="maintenance-header">
        <div>
          <h3 style="margin: 0">全站维护模式</h3>
          <p class="maintenance-desc">开启后前台仅展示维护提示，禁止用户操作</p>
        </div>
        <el-switch
          v-model="maintenanceEnabled"
          active-color="#F56C6C"
          @change="handleMaintenanceChange"
        />
      </div>
    </el-card>

    <el-divider />

    <!-- Feature Switch Cards -->
    <div class="feature-grid">
      <el-card v-for="group in featureGroups" :key="group.category" class="feature-card">
        <template #header>
          <span class="card-title">{{ group.label }}</span>
        </template>
        <div v-for="item in group.items" :key="item.id" class="feature-item">
          <div class="feature-info">
            <span class="feature-name">{{ item.featureName }}</span>
            <span v-if="item.description" class="feature-desc">{{ item.description }}</span>
          </div>
          <el-switch
            :model-value="item.enabled"
            :disabled="maintenanceEnabled && item.category === 'business'"
            @change="handleToggle(item)"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listFeatures, toggleFeature, toggleMaintenance } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const features = ref([])
const maintenanceEnabled = ref(false)

const featureGroups = computed(() => {
  const groups = {
    business: { category: 'business', label: '核心业务开关', items: [] },
    commercial: { category: 'commercial', label: '商业化功能开关', items: [] },
    security: { category: 'security', label: '安全机制开关', items: [] },
    message: { category: 'message', label: '消息推送开关', items: [] }
  }
  features.value.forEach(f => {
    if (f.category !== 'maintenance' && groups[f.category]) {
      groups[f.category].items.push(f)
    }
  })
  return Object.values(groups)
})

const fetchFeatures = async () => {
  const res = await listFeatures()
  features.value = res || []
  const maintenance = features.value.find(f => f.featureKey === 'maintenance')
  if (maintenance) {
    maintenanceEnabled.value = maintenance.enabled
  }
}

const handleToggle = async (item) => {
  await toggleFeature(item.id)
  item.enabled = !item.enabled
  ElMessage.success(`${item.enabled ? '开启' : '关闭'}成功`)
}

const handleMaintenanceChange = (val) => {
  const action = val ? '开启' : '关闭'
  ElMessageBox.confirm(
    `确定要${action}全站维护模式吗？${val ? '开启后将禁用所有核心业务功能。' : '关闭后将恢复所有核心业务功能。'}`,
    '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await toggleMaintenance(val)
    ElMessage.success(`${action}成功`)
    fetchFeatures()
  }).catch(() => {
    maintenanceEnabled.value = !val
  })
}

onMounted(fetchFeatures)
</script>

<style scoped>
.maintenance-card {
  margin-bottom: 16px;
}
.maintenance-active {
  border-color: #F56C6C;
}
.maintenance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.maintenance-desc {
  color: #909399;
  font-size: 13px;
  margin: 4px 0 0;
}
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}
.feature-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.feature-item:last-child {
  border-bottom: none;
}
.feature-info {
  display: flex;
  flex-direction: column;
}
.feature-name {
  font-size: 14px;
  font-weight: 500;
}
.feature-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.card-title {
  font-weight: 600;
}
</style>