<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择">
          <el-option
            v-for="item in notificationStatusOptions"
            :key="item.itemValue"
            :label="item.itemLabel"
            :value="Number(item.itemValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增通告</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="标题" prop="title" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" prop="msgTypeLabel" width="80" />
      <el-table-column label="推送范围" prop="pushScopeLabel" width="100" />
      <el-table-column label="状态" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="notificationStatusColor(row.status) || 'info'">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="推送时间" prop="pushTime" width="170" />
      <el-table-column label="已读/总数" align="center" width="100">
        <template #default="{ row }">{{ row.readCount }}/{{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column label="置顶" align="center" width="60">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="warning">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createdAt" width="170" />
      <el-table-column label="操作" align="center" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.status === 0" link type="success" icon="Promotion" @click="handlePush(row)">发布</el-button>
          <el-button v-if="row.status === 1" link type="warning" icon="SwitchButton" @click="handleRevoke(row)">撤回</el-button>
          <el-button v-if="row.status !== 3" link type="info" icon="Folder" @click="handleArchive(row)">归档</el-button>
          <el-button link type="primary" icon="Top" @click="handleToggleTop(row)">{{ row.isTop === 1 ? '取消置顶' : '置顶' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />

    <!-- 新增弹窗 -->
    <el-dialog v-model="createOpen" title="新增通告" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="消息类型" prop="msgType">
          <el-select v-model="form.msgType">
            <el-option
              v-for="item in msgTypeOptions"
              :key="item.itemValue"
              :label="item.itemLabel"
              :value="Number(item.itemValue)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="推送范围" prop="pushScope">
          <el-select v-model="form.pushScope">
            <el-option
              v-for="item in pushScopeOptions"
              :key="item.itemValue"
              :label="item.itemLabel"
              :value="Number(item.itemValue)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.pushScope && form.pushScope !== 1" label="推送目标">
          <el-input v-model="form.pushTarget" placeholder="输入用户/角色 ID，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="推送方式" prop="pushType">
          <el-select v-model="form.pushType">
            <el-option
              v-for="item in pushTypeOptions"
              :key="item.itemValue"
              :label="item.itemLabel"
              :value="Number(item.itemValue)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.pushType === 2" label="定时时间" prop="pushTime">
          <el-date-picker v-model="form.pushTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择推送时间" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择过期时间" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="消息内容（支持 HTML）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitCreate">创建并保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" title="通告详情" width="700px">
      <el-form label-width="100px">
        <el-form-item label="标题">{{ detail?.title }}</el-form-item>
        <el-form-item label="类型">{{ detail?.msgTypeLabel }}</el-form-item>
        <el-form-item label="状态"><el-tag :type="notificationStatusColor(detail?.status) || 'info'">{{ detail?.statusLabel }}</el-tag></el-form-item>
        <el-form-item label="推送范围">{{ detail?.pushScopeLabel }}</el-form-item>
        <el-form-item label="推送方式">{{ detail?.pushTypeLabel }}</el-form-item>
        <el-form-item label="推送时间">{{ detail?.pushTime || '-' }}</el-form-item>
        <el-form-item label="过期时间">{{ detail?.expireTime || '-' }}</el-form-item>
        <el-form-item label="已读/总数">{{ detail?.readCount }}/{{ detail?.totalCount }}</el-form-item>
        <el-form-item label="置顶状态">{{ detail?.isTop === 1 ? '置顶' : '正常' }}</el-form-item>
        <el-form-item label="内容">
          <div v-html="detail?.content" style="border:1px solid #dcdfe6;padding:10px;min-height:80px;border-radius:4px;"></div>
        </el-form-item>
        <el-form-item label="创建时间">{{ detail?.createdAt }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listNotifications, getNotification, createNotification, pushNotification, revokeNotification, archiveNotification, toggleTop } from '@/api/message'
import { useDict } from '@/composables/useDict'
import Pagination from '@/components/Pagination.vue'

const { options: notificationStatusOptions, label: notificationStatusLabel, color: notificationStatusColor } = useDict('notification_status')
const { options: msgTypeOptions } = useDict('msg_type')
const { options: pushScopeOptions } = useDict('push_scope')
const { options: pushTypeOptions } = useDict('push_type')

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryRef = ref(null)
const formRef = ref(null)
const dateRange = ref([])
const createOpen = ref(false)
const detailOpen = ref(false)
const detail = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined
})

const form = reactive({
  title: '',
  content: '',
  msgType: undefined,
  pushScope: undefined,
  pushTarget: '',
  pushType: 1,
  pushTime: undefined,
  expireTime: undefined
})

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  msgType: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  pushScope: [{ required: true, message: '请选择推送范围', trigger: 'change' }],
  pushType: [{ required: true, message: '请选择推送方式', trigger: 'change' }]
}

async function getList(pagination = null) {
  loading.value = true
  try {
    if (pagination) { queryParams.pageNum = pagination.page; queryParams.pageSize = pagination.limit }
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await listNotifications(params)
    list.value = res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() {
  queryRef.value?.resetFields()
  dateRange.value = []
  handleQuery()
}

function handleAdd() {
  Object.assign(form, { title: '', content: '', msgType: undefined, pushScope: undefined, pushTarget: '', pushType: 1, pushTime: undefined, expireTime: undefined })
  createOpen.value = true
}

async function submitCreate() {
  try {
    await formRef.value.validate()
    const data = { ...form }
    if (!data.pushTarget) data.pushTarget = undefined
    await createNotification(data)
    ElMessage.success('创建成功')
    createOpen.value = false
    getList()
  } catch {}
}

async function handlePush(row) {
  await ElMessageBox.confirm(`确认发布通告 "${row.title}" 吗？`, '提示', { type: 'info' })
  await pushNotification(row.id)
  ElMessage.success('发布成功')
  getList()
}

async function handleRevoke(row) {
  await ElMessageBox.confirm(`确认撤回通告 "${row.title}" 吗？`, '提示', { type: 'warning' })
  await revokeNotification(row.id)
  ElMessage.success('已撤回')
  getList()
}

async function handleArchive(row) {
  await ElMessageBox.confirm(`确认归档通告 "${row.title}" 吗？`, '提示', { type: 'info' })
  await archiveNotification(row.id)
  ElMessage.success('归档成功')
  getList()
}

async function handleToggleTop(row) {
  const newVal = row.isTop === 1 ? 0 : 1
  await toggleTop(row.id, newVal)
  ElMessage.success(newVal === 1 ? '已置顶' : '已取消置顶')
  getList()
}

async function handleDetail(row) {
  const res = await getNotification(row.id)
  detail.value = res || row
  detailOpen.value = true
}

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
.mb8 { margin-bottom: 8px; }
</style>