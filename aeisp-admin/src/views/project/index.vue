<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="项目名称" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="请输入项目名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="项目状态" clearable style="width: 140px">
          <el-option label="进行中" :value="0" />
          <el-option label="已完成" :value="1" />
          <el-option label="已归档" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateRange"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="projectList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="projectName" label="项目名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column prop="runTimeSeconds" label="运行时长" width="120" align="center">
        <template #default="{ row }">
          {{ formatDuration(row.runTimeSeconds) }}
        </template>
      </el-table-column>
      <el-table-column prop="isPinned" label="置顶" width="70" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isPinned === 1" type="warning" size="small">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="primary" size="small">{{ row.statusLabel }}</el-tag>
          <el-tag v-else-if="row.status === 1" type="success" size="small">{{ row.statusLabel }}</el-tag>
          <el-tag v-else-if="row.status === 2" type="info" size="small">{{ row.statusLabel }}</el-tag>
          <span v-else>{{ row.statusLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
      <el-table-column prop="archivedAt" label="归档时间" width="170" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="View" @click="handleView(row)">详情</el-button>
          <el-button
            v-if="row.status !== 2"
            type="warning"
            link
            size="small"
            icon="Folder"
            @click="handleArchive(row)"
          >归档</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-show="total > 0"
      :total="total"
      :page-num="queryParams.pageNum"
      :page-size="queryParams.pageSize"
      @pagination="handlePagination"
    />

    <!-- 详情弹窗 -->
    <el-dialog title="项目详情" v-model="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="ID" :span="1">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="项目名称" :span="1">{{ currentDetail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="用户ID" :span="1">{{ currentDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="1">{{ currentDetail.statusLabel }}</el-descriptions-item>
        <el-descriptions-item label="置顶" :span="1">{{ currentDetail.isPinned === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="运行时长" :span="1">{{ formatDuration(currentDetail.runTimeSeconds) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentDetail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="归档时间" :span="2">{{ currentDetail.archivedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.description" label="描述" :span="2">{{ currentDetail.description }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.remark" label="备注" :span="2">{{ currentDetail.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProjects, getProject, archiveProject, deleteProject } from '@/api/project'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const projectList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentDetail = ref(null)
const dateRange = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  userId: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined
})

function handleDateRange(value) {
  if (value) {
    queryParams.startTime = value[0]
    queryParams.endTime = value[1]
  } else {
    queryParams.startTime = undefined
    queryParams.endTime = undefined
  }
}

function formatDuration(seconds) {
  if (!seconds && seconds !== 0) return '-'
  if (seconds < 60) return seconds + '秒'
  if (seconds < 3600) return Math.floor(seconds / 60) + '分' + (seconds % 60) + '秒'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return h + '时' + m + '分'
}

function getList(page) {
  loading.value = true
  if (page) {
    queryParams.pageNum = page.pageNum
    queryParams.pageSize = page.pageSize
  }
  listProjects(queryParams).then(res => {
    projectList.value = res.list
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
  queryParams.keyword = undefined
  queryParams.userId = undefined
  queryParams.status = undefined
  queryParams.startTime = undefined
  queryParams.endTime = undefined
  dateRange.value = []
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

function handleView(row) {
  getProject(row.id).then(res => {
    currentDetail.value = res
    detailVisible.value = true
  })
}

function handleArchive(row) {
  ElMessageBox.confirm(`确认归档项目「${row.projectName}」？`, '归档确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    archiveProject(row.id).then(() => {
      ElMessage.success('归档成功')
      getList()
    })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除项目「${row.projectName}」？此操作不可恢复。`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  }).then(() => {
    deleteProject(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => getList())
</script>