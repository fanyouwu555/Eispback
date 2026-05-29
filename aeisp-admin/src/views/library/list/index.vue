<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="资源名称" prop="resourceName">
        <el-input v-model="queryParams.resourceName" placeholder="请输入资源名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="资源状态" clearable style="width: 120px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="Plus" @click="handleAdd" v-permission="'library:create'">新增资源</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="libraryList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="resourceCode" label="编码" width="180" align="center" />
      <el-table-column prop="resourceName" label="资源名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusColor(row.status) || 'info'" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentVersionNo" label="当前版本" width="100" align="center" />
      <el-table-column prop="fileSizeLabel" label="文件大小" width="100" align="center" />
      <el-table-column prop="downloadCount" label="下载" width="70" align="center" />
      <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
      <el-table-column label="操作" width="320" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="View" @click="handleView(row)">详情</el-button>
          <el-button type="primary" link size="small" icon="Edit" @click="handleEdit(row)" v-permission="'library:update'">编辑</el-button>
          <el-button type="success" link size="small" icon="Upload" @click="handleUploadVersion(row)" v-permission="'library:version:manage'">版本</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            size="small"
            :icon="row.status === 1 ? 'Hide' : 'View'"
            @click="handleToggleStatus(row)"
            v-permission="'library:update'"
          >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)" v-permission="'library:delete'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 新增资源弹窗 -->
    <el-dialog title="新增库资源" v-model="createVisible" width="520px">
      <el-form :model="createForm" ref="createFormRef" label-width="100px" :rules="libraryRules">
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="createForm.resourceName" placeholder="请输入资源名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="资源描述（可选）" maxlength="500" />
        </el-form-item>
        <el-divider>初始版本</el-divider>
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="createForm.versionNo" placeholder="如 1.0.0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input v-model="createForm.changelog" type="textarea" :rows="2" placeholder="更新说明（可选）" maxlength="500" />
        </el-form-item>
        <el-form-item label="ZIP文件" prop="zipFile">
          <el-upload ref="zipUploadRef" :auto-upload="false" :limit="1" accept=".zip,application/zip,application/x-zip-compressed" @change="handleZipChange">
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

    <!-- 编辑资源弹窗 -->
    <el-dialog title="编辑库资源" v-model="editVisible" width="480px">
      <el-form :model="editForm" ref="editFormRef" label-width="100px" :rules="libraryRules">
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="editForm.resourceName" maxlength="100" />
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
    <el-dialog title="库资源详情" v-model="detailVisible" width="720px">
      <template v-if="currentDetail">
        <el-descriptions :column="2" border class="mb-4">
          <el-descriptions-item label="ID" :span="1">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="资源编码" :span="1">{{ currentDetail.resourceCode }}</el-descriptions-item>
          <el-descriptions-item label="资源名称" :span="1">{{ currentDetail.resourceName }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag :type="statusColor(currentDetail.status) || 'info'" size="small">
              {{ statusLabel(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前版本" :span="1">{{ currentDetail.currentVersionNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下载次数" :span="1">{{ currentDetail.downloadCount }}</el-descriptions-item>
          <el-descriptions-item label="文件大小" :span="1">{{ currentDetail.fileSizeLabel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="1">{{ currentDetail.createdAt || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.status === 3" label="违规原因" :span="2">
            <el-tag type="danger">{{ currentDetail.violationReason }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.description" label="描述" :span="2">{{ currentDetail.description }}</el-descriptions-item>
        </el-descriptions>

        <!-- 版本历史 -->
        <el-divider>版本历史</el-divider>
        <el-table :data="versionList" stripe border size="small">
          <el-table-column prop="versionNo" label="版本号" width="100" />
          <el-table-column prop="changelog" label="更新日志" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时间" width="170" />
          <el-table-column label="ZIP 下载" width="100" align="center">
            <template #default="{ row }">
              <el-link v-if="row.storageUrl" :href="row.storageUrl" target="_blank" type="primary" :underline="false" icon="Download">下载</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.id !== currentDetail.currentVersion?.id"
                type="warning"
                link
                size="small"
                icon="Refresh"
                @click="handleRollback(row)"
                v-permission="'library:version:manage'"
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
        <el-form-item label="资源名称">
          <el-input :model-value="versionResourceName" disabled />
        </el-form-item>
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="versionForm.versionNo" placeholder="如 1.1.0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input v-model="versionForm.changelog" type="textarea" :rows="2" placeholder="更新说明（可选）" maxlength="500" />
        </el-form-item>
        <el-form-item label="ZIP文件" prop="zipFile">
          <el-upload ref="versionUploadRef" :auto-upload="false" :limit="1" accept=".zip,application/zip,application/x-zip-compressed" @change="handleVersionZipChange">
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Document } from '@element-plus/icons-vue'
import {
  listLibraries, getLibraryDetail, createLibrary, updateLibrary,
  deleteLibrary, toggleLibraryStatus,
  uploadLibraryVersion, rollbackLibraryVersion,
  getLibraryFiles
} from '@/api/library'
import Pagination from '@/components/Pagination.vue'

const statusOptions = [
  { label: '正常', value: 0, color: 'info' },
  { label: '上架', value: 1, color: 'success' },
  { label: '下架', value: 2, color: 'danger' },
  { label: '违规', value: 3, color: 'warning' }
]

function statusLabel(val) {
  const item = statusOptions.find(s => s.value === val)
  return item ? item.label : val
}

function statusColor(val) {
  const item = statusOptions.find(s => s.value === val)
  return item ? item.color : 'info'
}

const loading = ref(false)
const libraryList = ref([])
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
const versionResourceName = ref('')
const versionResourceId = ref(null)
const zipFile = ref(null)
const versionZipFile = ref(null)
const zipUploadRef = ref(null)
const versionUploadRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  resourceName: undefined,
  status: undefined
})

const createForm = reactive({
  resourceName: '',
  description: '',
  versionNo: '1.0.0',
  changelog: ''
})

const editForm = reactive({
  resourceName: '',
  description: ''
})
const editId = ref(null)

const versionForm = reactive({
  versionNo: '',
  changelog: ''
})

const libraryRules = {
  resourceName: [{ required: true, message: '资源名称不能为空', trigger: 'blur' }],
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
  listLibraries(queryParams).then(res => {
    libraryList.value = res.list
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
  queryParams.resourceName = undefined
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
  createForm.resourceName = ''
  createForm.description = ''
  createForm.versionNo = '1.0.0'
  createForm.changelog = ''
  zipFile.value = null
  createVisible.value = true
  nextTick(() => {
    zipUploadRef.value?.clearFiles()
  })
}

function handleCreateSubmit() {
  if (!zipFile.value) {
    ElMessage.warning('请选择ZIP文件')
    return
  }
  createLoading.value = true
  const fd = new FormData()
  fd.append('resourceName', createForm.resourceName)
  fd.append('description', createForm.description || '')
  fd.append('versionNo', createForm.versionNo)
  fd.append('changelog', createForm.changelog || '')
  fd.append('zipFile', zipFile.value)
  createLibrary(fd).then(() => {
    ElMessage.success('创建成功')
    createVisible.value = false
    getList()
  }).finally(() => {
    createLoading.value = false
  })
}

function handleEdit(row) {
  editId.value = row.id
  editForm.resourceName = row.resourceName
  editForm.description = row.description || ''
  editVisible.value = true
}

function handleEditSubmit() {
  editLoading.value = true
  updateLibrary(editId.value, {
    resourceName: editForm.resourceName,
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
  getLibraryDetail(row.id).then(res => {
    currentDetail.value = res
    versionList.value = [res.currentVersion, ...(res.historyVersions || [])].filter(Boolean)
    fileTree.value = res.fileTree || []
    detailVisible.value = true
  })
}

function handleUploadVersion(row) {
  versionResourceId.value = row.id
  versionResourceName.value = row.resourceName
  versionForm.versionNo = ''
  versionForm.changelog = ''
  versionZipFile.value = null
  versionVisible.value = true
  nextTick(() => {
    versionUploadRef.value?.clearFiles()
  })
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
  uploadLibraryVersion(versionResourceId.value, fd).then(() => {
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
  ElMessageBox.confirm(`确认${action}资源「${row.resourceName}」？`, '状态确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    toggleLibraryStatus(row.id, newStatus).then(() => {
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
    rollbackLibraryVersion(currentDetail.value.id, version.id).then(() => {
      ElMessage.success('回滚成功')
      detailVisible.value = false
      getList()
    })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除资源「${row.resourceName}」及其所有版本？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger'
  }).then(() => {
    deleteLibrary(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>
