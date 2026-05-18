<template>
  <div class="app-container">
    <el-table :data="permissionList" border row-key="id" default-expand-all v-loading="loading">
      <el-table-column label="权限名称" prop="permissionName" />
      <el-table-column label="权限编码" prop="permissionCode" />
      <el-table-column label="类型" align="center" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.type === 1">菜单</el-tag>
          <el-tag v-else type="warning">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" align="center" width="80" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listPermissions } from '@/api/system'

const loading = ref(false)
const permissionList = ref([])

async function getList() {
  loading.value = true
  try {
    const res = await listPermissions()
    permissionList.value = res || []
  } finally {
    loading.value = false
  }
}

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
