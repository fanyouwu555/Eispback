<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="queryParams.modelName" placeholder="请输入模型名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="模型类型" prop="modelType">
        <el-select v-model="queryParams.modelType" placeholder="模型类型" clearable style="width: 140px">
          <el-option label="通用" value="general" />
          <el-option label="代码优化" value="code_opt" />
          <el-option label="仿生适配" value="bio_adapt" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="模型状态" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="Plus" @click="handleAdd" v-permission="'model:create'">新增模型</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="modelList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="modelName" label="模型名称" min-width="130" />
      <el-table-column prop="modelType" label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.modelType === 'general'" type="primary" size="small">通用</el-tag>
          <el-tag v-else-if="row.modelType === 'code_opt'" type="success" size="small">代码优化</el-tag>
          <el-tag v-else-if="row.modelType === 'bio_adapt'" type="warning" size="small">仿生适配</el-tag>
          <span v-else>{{ row.modelType }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
          <el-tag v-else type="danger" size="small">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="weight" label="权重" width="70" align="center" />
      <el-table-column prop="maxQps" label="最大QPS" width="90" align="center" />
      <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
      <el-table-column prop="usageCount" label="调用次数" width="90" align="center" />
      <el-table-column prop="failureRate" label="失败率" width="80" align="center">
        <template #default="{ row }">{{ row.failureRate }}%</template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="View" @click="handleView(row)">详情</el-button>
          <el-button type="primary" link size="small" icon="Edit" @click="handleEdit(row)" v-permission="'model:update'">编辑</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            size="small"
            @click="handleToggleStatus(row)"
            v-permission="'model:update'"
          >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button type="primary" link size="small" icon="Cpu" @click="handleTest(row)" v-permission="'model:test'">测试</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)" v-permission="'model:delete'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="form" ref="formRef" label-width="120px" :rules="formRules">
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="请输入模型名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="form.modelType" style="width: 200px">
            <el-option label="通用" value="general" />
            <el-option label="代码优化" value="code_opt" />
            <el-option label="仿生适配" value="bio_adapt" />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址" prop="apiEndpoint">
          <el-input v-model="form.apiEndpoint" placeholder="https://api.example.com/v1" maxlength="255" />
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="API密钥" maxlength="255" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="权重" prop="weight">
              <el-input-number v-model="form.weight" :min="1" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大QPS" prop="maxQps">
              <el-input-number v-model="form.maxQps" :min="1" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序值" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="场景标签" prop="scenarioTags">
          <el-input v-model="form.scenarioTags" placeholder='JSON格式，如 ["teaching","competition"]' maxlength="500" />
        </el-form-item>
        <el-form-item label="维护窗口" prop="maintainWindow">
          <el-input v-model="form.maintainWindow" placeholder="如 03:00-04:00" maxlength="50" />
        </el-form-item>
        <el-form-item label="默认参数" prop="defaultParams">
          <el-input v-model="form.defaultParams" type="textarea" :rows="2" placeholder='JSON格式，如 {"temperature":0.7}' maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="模型详情" v-model="detailVisible" width="640px">
      <template v-if="currentDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="模型名称">{{ currentDetail.modelName }}</el-descriptions-item>
          <el-descriptions-item label="模型类型">
            <el-tag v-if="currentDetail.modelType === 'general'" type="primary" size="small">通用</el-tag>
            <el-tag v-else-if="currentDetail.modelType === 'code_opt'" type="success" size="small">代码优化</el-tag>
            <el-tag v-else-if="currentDetail.modelType === 'bio_adapt'" type="warning" size="small">仿生适配</el-tag>
            <span v-else>{{ currentDetail.modelType }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentDetail.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="API地址" :span="2">{{ currentDetail.apiEndpoint }}</el-descriptions-item>
          <el-descriptions-item label="权重">{{ currentDetail.weight }}</el-descriptions-item>
          <el-descriptions-item label="最大QPS">{{ currentDetail.maxQps }}</el-descriptions-item>
          <el-descriptions-item label="排序值">{{ currentDetail.sortOrder }}</el-descriptions-item>
          <el-descriptions-item label="调用次数">{{ currentDetail.usageCount }}</el-descriptions-item>
          <el-descriptions-item label="失败率">{{ currentDetail.failureRate }}%</el-descriptions-item>
          <el-descriptions-item label="场景标签" :span="2">{{ currentDetail.scenarioTags || '-' }}</el-descriptions-item>
          <el-descriptions-item label="维护窗口" :span="2">{{ currentDetail.maintainWindow || '-' }}</el-descriptions-item>
          <el-descriptions-item label="默认参数" :span="2">{{ currentDetail.defaultParams || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 测试弹窗 -->
    <el-dialog title="模型在线测试" v-model="testVisible" width="480px">
      <el-form :model="testForm" label-width="80px">
        <el-form-item label="模型">
          <el-input :model-value="testModelName" disabled />
        </el-form-item>
        <el-form-item label="测试输入" prop="testInput">
          <el-input v-model="testForm.testInput" type="textarea" :rows="5" placeholder="请输入测试内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTestSubmit" :loading="testLoading">发送测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listModels, getModel, createModel, updateModel, deleteModel, toggleModelStatus, testModel } from '@/api/model'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const modelList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref(null)
const detailVisible = ref(false)
const currentDetail = ref(null)
const testVisible = ref(false)
const testLoading = ref(false)
const testModelName = ref('')
const testModelId = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  modelName: undefined,
  modelType: undefined,
  status: undefined
})

const form = reactive({
  modelName: '',
  modelType: 'general',
  apiEndpoint: '',
  apiKey: '',
  weight: 1,
  maxQps: 10,
  sortOrder: 0,
  status: 1,
  scenarioTags: '',
  maintainWindow: '',
  defaultParams: ''
})

const testForm = reactive({ testInput: '' })

const formRules = {
  modelName: [{ required: true, message: '模型名称不能为空', trigger: 'blur' }],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }],
  apiEndpoint: [{ required: true, message: 'API地址不能为空', trigger: 'blur' }],
  apiKey: [{ required: true, message: 'API密钥不能为空', trigger: 'blur' }]
}

function getList(page) {
  loading.value = true
  if (page) {
    queryParams.pageNum = page.pageNum
    queryParams.pageSize = page.pageSize
  }
  listModels(queryParams).then(res => {
    modelList.value = res.list
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
  queryParams.modelName = undefined
  queryParams.modelType = undefined
  queryParams.status = undefined
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

function resetForm() {
  form.modelName = ''
  form.modelType = 'general'
  form.apiEndpoint = ''
  form.apiKey = ''
  form.weight = 1
  form.maxQps = 10
  form.sortOrder = 0
  form.status = 1
  form.scenarioTags = ''
  form.maintainWindow = ''
  form.defaultParams = ''
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增模型'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑模型'
  form.modelName = row.modelName
  form.modelType = row.modelType
  form.apiEndpoint = row.apiEndpoint
  form.apiKey = ''
  form.weight = row.weight
  form.maxQps = row.maxQps
  form.sortOrder = row.sortOrder
  form.status = row.status
  form.scenarioTags = row.scenarioTags || ''
  form.maintainWindow = row.maintainWindow || ''
  form.defaultParams = row.defaultParams || ''
  dialogVisible.value = true
}

function handleSubmit() {
  submitLoading.value = true
  const data = {
    modelName: form.modelName,
    modelType: form.modelType,
    apiEndpoint: form.apiEndpoint,
    apiKey: form.apiKey,
    weight: form.weight,
    maxQps: form.maxQps,
    sortOrder: form.sortOrder,
    status: form.status,
    scenarioTags: form.scenarioTags || undefined,
    maintainWindow: form.maintainWindow || undefined,
    defaultParams: form.defaultParams || undefined
  }
  if (!isEdit.value && !data.apiKey) {
    ElMessage.warning('新增时API密钥不能为空')
    submitLoading.value = false
    return
  }
  const action = isEdit.value ? updateModel(editId.value, data) : createModel(data)
  action.then(() => {
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getList()
  }).catch(() => {}).finally(() => {
    submitLoading.value = false
  })
}

function handleView(row) {
  getModel(row.id).then(res => {
    currentDetail.value = res
    detailVisible.value = true
  })
}

function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确认${action}模型「${row.modelName}」？`, '状态确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    toggleModelStatus(row.id, newStatus).then(() => {
      ElMessage.success(`${action}成功`)
      getList()
    })
  }).catch(() => {})
}

function handleTest(row) {
  testModelId.value = row.id
  testModelName.value = row.modelName
  testForm.testInput = ''
  testVisible.value = true
}

function handleTestSubmit() {
  if (!testForm.testInput) {
    ElMessage.warning('请输入测试内容')
    return
  }
  testLoading.value = true
  testModel(testModelId.value, { testInput: testForm.testInput, modelId: testModelId.value }).then(() => {
    ElMessage.success('测试请求已发送')
    testVisible.value = false
  }).catch(() => {}).finally(() => {
    testLoading.value = false
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除模型「${row.modelName}」？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger'
  }).then(() => {
    deleteModel(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => getList())
</script>