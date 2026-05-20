<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="用户ID" clearable @keyup.enter="handleQuery" style="width: 140px" />
      </el-form-item>
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="会话标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="会话状态" clearable style="width: 130px">
          <el-option label="进行中" :value="1" />
          <el-option label="已归档" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="sessionList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="sessionTitle" label="会话标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column prop="modelName" label="模型" width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.modelName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="messageCount" label="消息数" width="80" align="center" />
      <el-table-column prop="totalTokens" label="Token数" width="90" align="center" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="primary" size="small">进行中</el-tag>
          <el-tag v-else type="info" size="small">已归档</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startedAt" label="开始时间" width="170" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="View" @click="handleView(row)">详情</el-button>
          <el-button
            v-if="row.status === 1"
            type="warning"
            link
            size="small"
            icon="Folder"
            @click="handleArchive(row)"
            v-permission="'ai:session:manage'"
          >归档</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)" v-permission="'ai:session:manage'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 详情弹窗 -->
    <el-dialog title="会话详情" v-model="detailVisible" width="720px">
      <template v-if="currentSession">
        <el-descriptions :column="2" border class="mb-4">
          <el-descriptions-item label="会话标题" :span="2">{{ currentSession.sessionTitle }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentSession.userId }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ currentSession.modelName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentSession.status === 1" type="primary" size="small">进行中</el-tag>
            <el-tag v-else type="info" size="small">已归档</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息数">{{ currentSession.messageCount }}</el-descriptions-item>
          <el-descriptions-item label="Token总数">{{ currentSession.totalTokens }}</el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="2">{{ currentSession.startedAt }}</el-descriptions-item>
          <el-descriptions-item v-if="currentSession.endedAt" label="结束时间" :span="2">{{ currentSession.endedAt }}</el-descriptions-item>
        </el-descriptions>

        <!-- 消息列表 -->
        <el-divider>对话记录 ({{ messages.length }})</el-divider>
        <div v-loading="messagesLoading" class="message-list">
          <div v-for="msg in messages" :key="msg.id" :class="['message-item', msg.role]">
            <div class="message-header">
              <el-tag :type="msg.role === 'user' ? '' : 'success'" size="small">
                {{ msg.role === 'user' ? '用户' : msg.role === 'assistant' ? 'AI助手' : '系统' }}
              </el-tag>
              <span class="message-time">{{ msg.createdAt }}</span>
              <span v-if="msg.tokensUsed" class="message-tokens">Token: {{ msg.tokensUsed }}</span>
            </div>
            <div class="message-content">{{ msg.content || '-' }}</div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无对话记录" />
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAiSessions, getAiSession, listAiMessages, archiveAiSession, deleteAiSession } from '@/api/ai'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const sessionList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentSession = ref(null)
const messages = ref([])
const messagesLoading = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userId: undefined,
  keyword: undefined,
  status: undefined
})

function getList(page) {
  loading.value = true
  if (page) { queryParams.pageNum = page.pageNum; queryParams.pageSize = page.pageSize }
  listAiSessions(queryParams).then(res => {
    sessionList.value = res.list
    total.value = res.total
  }).finally(() => { loading.value = false })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryParams.userId = undefined; queryParams.keyword = undefined; queryParams.status = undefined; handleQuery() }
function handlePagination(pageInfo) { getList(pageInfo) }

function handleView(row) {
  getAiSession(row.id).then(res => {
    currentSession.value = res
    detailVisible.value = true
    messagesLoading.value = true
    listAiMessages(row.id).then(msgRes => {
      messages.value = msgRes || []
    }).finally(() => { messagesLoading.value = false })
  })
}

function handleArchive(row) {
  ElMessageBox.confirm(`确认归档会话「${row.sessionTitle || '#' + row.id}」？`, '归档确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    archiveAiSession(row.id).then(() => {
      ElMessage.success('归档成功'); getList()
    })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除会话「${row.sessionTitle || '#' + row.id}」？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger'
  }).then(() => {
    deleteAiSession(row.id).then(() => {
      ElMessage.success('删除成功'); getList()
    })
  }).catch(() => {})
}

onMounted(() => getList())
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.message-list { max-height: 480px; overflow-y: auto; }
.message-item { padding: 12px; margin-bottom: 8px; border-radius: 6px; background: #f5f7fa; }
.message-item.user { border-left: 3px solid #409eff; }
.message-item.assistant { border-left: 3px solid #67c23a; }
.message-item.system { border-left: 3px solid #909399; }
.message-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.message-time { font-size: 12px; color: #909399; }
.message-tokens { font-size: 12px; color: #909399; margin-left: auto; }
.message-content { font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
</style>