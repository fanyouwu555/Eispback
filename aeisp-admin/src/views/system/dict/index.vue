<template>
  <div class="app-container">
    <!-- 左侧：字典类型 -->
    <div class="left-panel">
      <div class="panel-header">
        <h3>字典类型</h3>
      </div>

      <!-- 搜索表单 -->
      <el-form :model="queryParams" :inline="true" ref="queryRef">
        <el-form-item label="名称" prop="dictName">
          <el-input v-model="queryParams.dictName" placeholder="字典名称" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="标识" prop="dictCode">
          <el-input v-model="queryParams.dictCode" placeholder="字典标识" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row class="mb-2">
        <el-button type="primary" plain icon="Plus" @click="handleAddType">新增类型</el-button>
      </el-row>

      <el-table v-loading="typeLoading" :data="typeList" highlight-current-row @row-click="handleRowClick" stripe>
        <el-table-column label="字典名称" prop="dictName" min-width="120" />
        <el-table-column label="字典标识" prop="dictCode" min-width="120" />
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="系统内置" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isSystem === 1 ? 'warning' : ''" size="small">
              {{ scope.row.isSystem === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="description" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click.stop="handleUpdateType(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click.stop="handleDeleteType(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-if="typeTotal > 0" :total="typeTotal" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getTypeList" />
    </div>

    <!-- 右侧：字典数据 -->
    <div class="right-panel" v-if="currentType">
      <div class="panel-header">
        <h3>字典数据 - {{ currentType.dictName }} ({{ currentType.dictCode }})</h3>
      </div>

      <el-row class="mb-2">
        <el-button type="primary" plain icon="Plus" @click="handleAddData">新增数据</el-button>
      </el-row>

      <el-table v-loading="dataLoading" :data="dataList" stripe>
        <el-table-column label="数据标签" prop="itemLabel" min-width="120" />
        <el-table-column label="数据值" prop="itemValue" min-width="120" />
        <el-table-column label="排序" prop="sortOrder" width="60" align="center" />
        <el-table-column label="标签颜色" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.color" :type="scope.row.color" size="small">{{ scope.row.color }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认" width="60" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault === 1" type="success" size="small">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleUpdateData(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteData(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="right-panel placeholder" v-else>
      <el-empty description="请选择一个字典类型查看数据" />
    </div>

    <!-- 字典类型弹窗 -->
    <el-dialog v-model="typeDialogOpen" :title="typeDialogTitle" width="500px" @close="resetTypeForm">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典标识" prop="dictCode">
          <el-input v-model="typeForm.dictCode" placeholder="请输字典标识" :disabled="typeForm.id != null" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input v-model="typeForm.description" placeholder="请输入备注" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitTypeForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog v-model="dataDialogOpen" :title="dataDialogTitle" width="500px" @close="resetDataForm">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="数据标签" prop="itemLabel">
          <el-input v-model="dataForm.itemLabel" placeholder="请输入数据标签" />
        </el-form-item>
        <el-form-item label="数据值" prop="itemValue">
          <el-input v-model="dataForm.itemValue" placeholder="请输入数据值" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="dataForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-select v-model="dataForm.color" placeholder="请选择颜色" clearable>
            <el-option label="默认" value="" />
            <el-option label="success" value="success" />
            <el-option label="warning" value="warning" />
            <el-option label="info" value="info" />
            <el-option label="danger" value="danger" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否默认" prop="isDefault">
          <el-radio-group v-model="dataForm.isDefault">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitDataForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDictTypes, createDictType, updateDictType, deleteDictType, listDictData, createDictData, updateDictData, deleteDictData } from '@/api/system/dict'
import Pagination from '@/components/Pagination.vue'

// 字典类型
const typeLoading = ref(false)
const typeList = ref([])
const typeTotal = ref(0)
const typeDialogOpen = ref(false)
const typeDialogTitle = ref('')
const typeFormRef = ref(null)
const queryRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  dictName: undefined,
  dictCode: undefined,
  status: undefined
})

const initTypeForm = { id: undefined, dictName: '', dictCode: '', status: 1, description: '' }
const typeForm = reactive({ ...initTypeForm })

const typeRules = {
  dictName: [{ required: true, message: '字典名称不能为空', trigger: 'blur' }],
  dictCode: [{ required: true, message: '字典标识不能为空', trigger: 'blur' }]
}

// 字典数据
const currentType = ref(null)
const dataLoading = ref(false)
const dataList = ref([])
const dataDialogOpen = ref(false)
const dataDialogTitle = ref('')
const dataFormRef = ref(null)

const initDataForm = { id: undefined, dictCode: '', itemLabel: '', itemValue: '', sortOrder: 0, status: 1, color: '', isDefault: 0 }
const dataForm = reactive({ ...initDataForm })

const dataRules = {
  itemLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
  itemValue: [{ required: true, message: '数据值不能为空', trigger: 'blur' }]
}

// 类型方法
async function getTypeList(pagination = null) {
  typeLoading.value = true
  try {
    if (pagination) {
      queryParams.pageNum = pagination.page
      queryParams.pageSize = pagination.limit
    }
    const res = await listDictTypes(queryParams)
    typeList.value = res.list || []
    typeTotal.value = res.total || 0
  } finally {
    typeLoading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getTypeList()
}

function resetQuery() {
  queryParams.dictName = undefined
  queryParams.dictCode = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  getTypeList()
}

function handleAddType() {
  Object.assign(typeForm, initTypeForm)
  typeForm.status = 1
  typeDialogTitle.value = '新增字典类型'
  typeDialogOpen.value = true
}

function handleUpdateType(row) {
  Object.assign(typeForm, row)
  typeDialogTitle.value = '编辑字典类型'
  typeDialogOpen.value = true
}

async function submitTypeForm() {
  await typeFormRef.value.validate()
  if (typeForm.id) {
    await updateDictType(typeForm.id, typeForm)
    ElMessage.success('修改成功')
  } else {
    await createDictType(typeForm)
    ElMessage.success('新增成功')
  }
  typeDialogOpen.value = false
  getTypeList()
}

function resetTypeForm() {
  typeFormRef.value?.resetFields()
}

function handleDeleteType(row) {
  ElMessageBox.confirm(`确认删除字典类型 "${row.dictName}" 吗？${row.isSystem === 1 ? '（系统内置类型不可删除）' : ''}`, '提示', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteDictType(row.id)
    ElMessage.success('删除成功')
    if (currentType.value?.id === row.id) {
      currentType.value = null
      dataList.value = []
    }
    getTypeList()
  }).catch(() => {})
}

// 数据方法
async function handleRowClick(row) {
  currentType.value = row
  await loadDictData(row.dictCode)
}

async function loadDictData(dictCode) {
  dataLoading.value = true
  try {
    const res = await listDictData(dictCode)
    dataList.value = res || []
  } finally {
    dataLoading.value = false
  }
}

function handleAddData() {
  Object.assign(dataForm, initDataForm)
  dataForm.dictCode = currentType.value.dictCode
  dataForm.status = 1
  dataDialogTitle.value = '新增字典数据'
  dataDialogOpen.value = true
}

function handleUpdateData(row) {
  Object.assign(dataForm, row)
  dataDialogTitle.value = '编辑字典数据'
  dataDialogOpen.value = true
}

async function submitDataForm() {
  await dataFormRef.value.validate()
  if (dataForm.id) {
    await updateDictData(dataForm.id, dataForm)
    ElMessage.success('修改成功')
  } else {
    await createDictData(dataForm)
    ElMessage.success('新增成功')
  }
  dataDialogOpen.value = false
  loadDictData(currentType.value.dictCode)
}

function resetDataForm() {
  dataFormRef.value?.resetFields()
}

function handleDeleteData(row) {
  ElMessageBox.confirm(`确认删除字典数据 "${row.itemLabel}" 吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteDictData(row.id)
    ElMessage.success('删除成功')
    loadDictData(currentType.value.dictCode)
  }).catch(() => {})
}

onMounted(() => {
  getTypeList()
})
</script>

<style scoped>
.app-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 100px);
}

.left-panel {
  width: 60%;
  min-width: 500px;
  overflow-y: auto;
}

.right-panel {
  flex: 1;
  min-width: 400px;
  overflow-y: auto;
  border-left: 1px solid #e4e7ed;
  padding-left: 16px;
}

.right-panel.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel-header h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.mb-2 {
  margin-bottom: 12px;
}
</style>