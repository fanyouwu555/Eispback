<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="资源名称" prop="resourceName">
        <el-input v-model="queryParams.resourceName" placeholder="请输入资源名称" clearable @keyup.enter="handleQuery" />
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
      <el-table-column prop="resourceName" label="资源名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="Edit" @click="handleEdit(row)" v-permission="'library:update'">编辑</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)" v-permission="'library:delete'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 新增资源弹窗 -->
    <el-dialog title="新增库资源" v-model="createVisible" width="480px">
      <el-form :model="createForm" ref="createFormRef" label-width="100px" :rules="libraryRules">
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="createForm.resourceName" placeholder="请输入资源名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="资源描述（可选）" maxlength="500" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listLibraries, createLibrary, updateLibrary, deleteLibrary
} from '@/api/library'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const libraryList = ref([])
const total = ref(0)
const createVisible = ref(false)
const createLoading = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const editId = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  resourceName: undefined
})

const createForm = reactive({
  resourceName: '',
  description: ''
})

const editForm = reactive({
  resourceName: '',
  description: ''
})

const libraryRules = {
  resourceName: [{ required: true, message: '资源名称不能为空', trigger: 'blur' }]
}

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
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

function handleAdd() {
  createForm.resourceName = ''
  createForm.description = ''
  createVisible.value = true
}

function handleCreateSubmit() {
  createLoading.value = true
  createLibrary({
    resourceName: createForm.resourceName,
    description: createForm.description
  }).then(() => {
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

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除资源「${row.resourceName}」？`, '删除确认', {
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
