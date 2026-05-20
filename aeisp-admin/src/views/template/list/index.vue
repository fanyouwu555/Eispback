<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="模板名称" prop="templateName">
        <el-input v-model="queryParams.templateName" placeholder="请输入模板名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="场景" prop="scenario">
        <el-select v-model="queryParams.scenario" placeholder="场景类型" clearable style="width: 130px">
          <el-option label="教学" value="teaching" />
          <el-option label="竞赛" value="competition" />
          <el-option label="实训" value="practice" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="模板状态" clearable style="width: 120px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="Plus" @click="handleAdd" v-permission="'template:create'">新增模板</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="templateList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="templateName" label="模板名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="scenario" label="场景" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.scenario === 'teaching'" type="primary" size="small">教学</el-tag>
          <el-tag v-else-if="row.scenario === 'competition'" type="warning" size="small">竞赛</el-tag>
          <el-tag v-else-if="row.scenario === 'practice'" type="success" size="small">实训</el-tag>
          <span v-else>{{ row.scenario }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">上架</el-tag>
          <el-tag v-else type="danger" size="small">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentVersionNo" label="当前版本" width="100" align="center" />
      <el-table-column prop="sortWeight" label="权重" width="70" align="center" />
      <el-table-column prop="usageCount" label="使用次数" width="90" align="center" />
      <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="View" @click="handleView(row)">详情</el-button>
          <el-button type="primary" link size="small" icon="Edit" @click="handleEdit(row)" v-permission="'template:update'">编辑</el-button>
          <el-button type="success" link size="small" icon="Upload" @click="handleUploadVersion(row)" v-permission="'template:version:manage'">版本</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            size="small"
            :icon="row.status === 1 ? 'Hide' : 'View'"
            @click="handleToggleStatus(row)"
            v-permission="'template:update'"
          >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)" v-permission="'template:delete'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 新增模板弹窗 -->
    <el-dialog title="新增模板" v-model="createVisible" width="560px">
      <el-form :model="createForm" ref="createFormRef" label-width="110px" :rules="templateRules">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="createForm.templateName" placeholder="请输入模板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="场景" prop="scenario">
          <el-select v-model="createForm.scenario" placeholder="请选择场景" style="width: 200px">
            <el-option label="教学" value="teaching" />
            <el-option label="竞赛" value="competition" />
            <el-option label="实训" value="practice" />
          </el-select>
        </el-form-item>
        <el-form-item label="权重" prop="sortWeight">
          <el-input-number v-model="createForm.sortWeight" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="预览图URL" prop="previewImage">
          <el-input v-model="createForm.previewImage" placeholder="预览图URL（可选）" maxlength="500" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="模板描述（可选）" maxlength="500" />
        </el-form-item>
        <el-divider>初始版本</el-divider>
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="createForm.versionNo" placeholder="如 1.0.0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input v-model="createForm.changelog" type="textarea" :rows="2" placeholder="更新说明（可选）" maxlength="500" />
        </el-form-item>
        <el-form-item label="模板文件(ZIP)" prop="zipFile">
          <el-upload ref="zipUploadRef" :auto-upload="false" :limit="1" accept=".zip" @change="handleZipChange">
            <el-button type="primary" icon="Upload">选择ZIP文件</el-button>
            <template #tip><span class="el-upload__tip">仅支持 .zip 格式</span></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑模板弹窗 -->
    <el-dialog title="编辑模板" v-model="editVisible" width="520px">
      <el-form :model="editForm" ref="editFormRef" label-width="110px" :rules="templateRules">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="editForm.templateName" maxlength="100" />
        </el-form-item>
        <el-form-item label="场景" prop="scenario">
          <el-select v-model="editForm.scenario" style="width: 200px">
            <el-option label="教学" value="teaching" />
            <el-option label="竞赛" value="competition" />
            <el-option label="实训" value="practice" />
          </el-select>
        </el-form-item>
        <el-form-item label="权重" prop="sortWeight">
          <el-input-number v-model="editForm.sortWeight" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="预览图URL" prop="previewImage">
          <el-input v-model="editForm.previewImage" maxlength="500" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="模板详情" v-model="detailVisible" width="720px">
      <template v-if="currentDetail">
        <el-descriptions :column="2" border class="mb-4">
          <el-descriptions-item label="ID" :span="1">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="模板名称" :span="1">{{ currentDetail.templateName }}</el-descriptions-item>
          <el-descriptions-item label="场景" :span="1">
            <el-tag v-if="currentDetail.scenario === 'teaching'" type="primary" size="small">教学</el-tag>
            <el-tag v-else-if="currentDetail.scenario === 'competition'" type="warning" size="small">竞赛</el-tag>
            <el-tag v-else-if="currentDetail.scenario === 'practice'" type="success" size="small">实训</el-tag>
            <span v-else>{{ currentDetail.scenario }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag v-if="currentDetail.status === 1" type="success" size="small">上架</el-tag>
            <el-tag v-else type="danger" size="small">下架</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="权重" :span="1">{{ currentDetail.sortWeight }}</el-descriptions-item>
          <el-descriptions-item label="使用次数" :span="1">{{ currentDetail.usageCount }}</el-descriptions-item>
          <el-descriptions-item label="当前版本" :span="2">
            {{ currentDetail.currentVersion?.versionNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.description" label="描述" :span="2">{{ currentDetail.description }}</el-descriptions-item>
        </el-descriptions>

        <!-- 版本历史 -->
        <el-divider>版本历史</el-divider>
        <el-table :data="versionList" stripe border size="small">
          <el-table-column prop="versionNo" label="版本号" width="100" />
          <el-table-column prop="changelog" label="更新日志" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时间" width="170" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.id !== currentDetail.currentVersion?.id"
                type="warning"
                link
                size="small"
                icon="Refresh"
                @click="handleRollback(row)"
                v-permission="'template:version:manage'"
              >回滚</el-button>
              <el-tag v-else size="small" type="success">当前</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 文件树 -->
        <el-divider>文件结构</el-divider>
        <el-tree :data="fileTree" :props="fileTreeProps" default-expand-all highlight-current>
          <template #default="{ node, data }">
            <span>
              <el-icon v-if="data.type === 'dir'"><Folder /></el-icon>
              <el-icon v-else><Document /></el-icon>
              {{ data.name }}
            </span>
          </template>
        </el-tree>
      </template>
    </el-dialog>

    <!-- 上传版本弹窗 -->
    <el-dialog title="上传新版本" v-model="versionVisible" width="480px">
      <el-form :model="versionForm" ref="versionFormRef" label-width="100px" :rules="versionRules">
        <el-form-item label="模板名称">
          <el-input :model-value="versionTemplateName" disabled />
        </el-form-item>
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="versionForm.versionNo" placeholder="如 1.1.0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input v-model="versionForm.changelog" type="textarea" :rows="2" placeholder="更新说明（可选）" maxlength="500" />
        </el-form-item>
        <el-form-item label="ZIP文件" prop="zipFile">
          <el-upload ref="versionUploadRef" :auto-upload="false" :limit="1" accept=".zip" @change="handleVersionZipChange">
            <el-button type="primary" icon="Upload">选择ZIP文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="versionVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVersionSubmit" :loading="versionLoading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Document } from '@element-plus/icons-vue'
import {
  listTemplates, getTemplate, createTemplate, updateTemplate,
  deleteTemplate, toggleTemplateStatus,
  uploadTemplateVersion, rollbackTemplateVersion
} from '@/api/template'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const templateList = ref([])
const total = ref(0)
const createVisible = ref(false)
const createLoading = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const detailVisible = ref(false)
const versionVisible = ref(false)
const versionLoading = ref(false)
const currentDetail = ref(null)
const versionList = ref([])
const fileTree = ref([])
const versionTemplateName = ref('')
const versionTemplateId = ref(null)
const zipFile = ref(null)
const versionZipFile = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  templateName: undefined,
  scenario: undefined,
  status: undefined
})

const createForm = reactive({
  templateName: '',
  scenario: 'teaching',
  sortWeight: 0,
  previewImage: '',
  description: '',
  versionNo: '1.0.0',
  changelog: ''
})

const editForm = reactive({
  templateName: '',
  scenario: 'teaching',
  sortWeight: 0,
  previewImage: '',
  description: ''
})
const editId = ref(null)

const versionForm = reactive({
  versionNo: '',
  changelog: ''
})

const templateRules = {
  templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
  scenario: [{ required: true, message: '请选择场景', trigger: 'change' }],
  sortWeight: [{ required: true, message: '请输入权重', trigger: 'blur' }],
  versionNo: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const versionRules = {
  versionNo: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const fileTreeProps = { children: 'children', label: 'name' }

function getList(page) {
  loading.value = true
  if (page) {
    queryParams.pageNum = page.pageNum
    queryParams.pageSize = page.pageSize
  }
  listTemplates(queryParams).then(res => {
    templateList.value = res.list
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
  queryParams.templateName = undefined
  queryParams.scenario = undefined
  queryParams.status = undefined
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

function handleZipChange(uploadFile) {
  zipFile.value = uploadFile.raw
}

function handleVersionZipChange(uploadFile) {
  versionZipFile.value = uploadFile.raw
}

function handleAdd() {
  createForm.templateName = ''
  createForm.scenario = 'teaching'
  createForm.sortWeight = 0
  createForm.previewImage = ''
  createForm.description = ''
  createForm.versionNo = '1.0.0'
  createForm.changelog = ''
  zipFile.value = null
  createVisible.value = true
}

function handleCreateSubmit() {
  if (!zipFile.value) {
    ElMessage.warning('请选择ZIP文件')
    return
  }
  createLoading.value = true
  const fd = new FormData()
  fd.append('templateName', createForm.templateName)
  fd.append('scenario', createForm.scenario)
  fd.append('sortWeight', createForm.sortWeight)
  fd.append('previewImage', createForm.previewImage || '')
  fd.append('description', createForm.description || '')
  fd.append('versionNo', createForm.versionNo)
  fd.append('changelog', createForm.changelog || '')
  fd.append('zipFile', zipFile.value)
  createTemplate(fd).then(() => {
    ElMessage.success('创建成功')
    createVisible.value = false
    getList()
  }).finally(() => {
    createLoading.value = false
  })
}

function handleEdit(row) {
  editId.value = row.id
  editForm.templateName = row.templateName
  editForm.scenario = row.scenario
  editForm.sortWeight = row.sortWeight
  editForm.previewImage = row.previewImage || ''
  editForm.description = row.description || ''
  editVisible.value = true
}

function handleEditSubmit() {
  editLoading.value = true
  updateTemplate(editId.value, {
    templateName: editForm.templateName,
    scenario: editForm.scenario,
    sortWeight: editForm.sortWeight,
    previewImage: editForm.previewImage,
    description: editForm.description
  }).then(() => {
    ElMessage.success('保存成功')
    editVisible.value = false
    getList()
  }).finally(() => {
    editLoading.value = false
  })
}

function handleView(row) {
  getTemplate(row.id).then(res => {
    currentDetail.value = res
    versionList.value = [res.currentVersion, ...(res.historyVersions || [])].filter(Boolean)
    fileTree.value = res.fileTree || []
    detailVisible.value = true
  })
}

function handleUploadVersion(row) {
  versionTemplateId.value = row.id
  versionTemplateName.value = row.templateName
  versionForm.versionNo = ''
  versionForm.changelog = ''
  versionZipFile.value = null
  versionVisible.value = true
}

function handleVersionSubmit() {
  if (!versionZipFile.value) {
    ElMessage.warning('请选择ZIP文件')
    return
  }
  versionLoading.value = true
  const fd = new FormData()
  fd.append('versionNo', versionForm.versionNo)
  fd.append('changelog', versionForm.changelog || '')
  fd.append('zipFile', versionZipFile.value)
  uploadTemplateVersion(versionTemplateId.value, fd).then(() => {
    ElMessage.success('版本上传成功')
    versionVisible.value = false
    getList()
  }).finally(() => {
    versionLoading.value = false
  })
}

function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确认${action}模板「${row.templateName}」？`, '状态确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    toggleTemplateStatus(row.id, newStatus).then(() => {
      ElMessage.success(`${action}成功`)
      getList()
    })
  }).catch(() => {})
}

function handleRollback(version) {
  ElMessageBox.confirm(
    `确认回滚到版本 ${version.versionNo}？回滚仅改变当前版本指向，不会删除其他版本。`,
    '回滚确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    rollbackTemplateVersion(currentDetail.value.id, version.id).then(() => {
      ElMessage.success('回滚成功')
      detailVisible.value = false
      getList()
    })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除模板「${row.templateName}」及其所有版本？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger'
  }).then(() => {
    deleteTemplate(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => getList())
</script>