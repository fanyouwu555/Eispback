<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="套餐名称" prop="packageName">
        <el-input v-model="queryParams.packageName" placeholder="请输入套餐名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="套餐状态" clearable style="width: 120px">
          <el-option
            v-for="item in packageStatusOptions"
            :key="item.itemValue"
            :label="item.itemLabel"
            :value="Number(item.itemValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="Plus" @click="handleAdd" v-permission="'finance:package:manage'">新增套餐</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="packageList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="packageName" label="套餐名称" min-width="140" />
      <el-table-column prop="price" label="价格" width="100" align="center">
        <template #default="{ row }">{{ (row.price / 100).toFixed(2) }} 元</template>
      </el-table-column>
      <el-table-column prop="durationHours" label="时长" width="90" align="center">
        <template #default="{ row }">{{ row.durationHours }} 小时</template>
      </el-table-column>
      <el-table-column prop="validDays" label="有效期" width="90" align="center">
        <template #default="{ row }">{{ row.validDays }} 天</template>
      </el-table-column>
      <el-table-column prop="promotion" label="优惠活动" min-width="140" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="packageStatusColor(row.status) || 'primary'" size="small">
            {{ packageStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="Edit" @click="handleEdit(row)" v-permission="'finance:package:manage'">编辑</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)" v-permission="'finance:package:manage'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="520px">
      <el-form :model="form" ref="formRef" label-width="100px" :rules="rules">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="请输入套餐名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :step="100" :precision="0" style="width: 200px" />
          <span class="ml-2">分（{{ (form.price / 100).toFixed(2) }}元）</span>
        </el-form-item>
        <el-form-item label="时长(小时)" prop="durationHours">
          <el-input-number v-model="form.durationHours" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效期(天)" prop="validDays">
          <el-input-number v-model="form.validDays" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="item in packageStatusOptions" :key="item.itemValue" :value="Number(item.itemValue)">
              {{ item.itemLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠活动" prop="promotion">
          <el-input v-model="form.promotion" type="textarea" :rows="2" placeholder="请输入优惠活动描述" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPackages, getPackage, createPackage, updatePackage, deletePackage } from '@/api/recharge'
import { useDict } from '@/composables/useDict'
import Pagination from '@/components/Pagination.vue'

const { options: packageStatusOptions, label: packageStatusLabel, color: packageStatusColor } = useDict('package_status')

const loading = ref(false)
const packageList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  packageName: undefined,
  status: undefined
})

const form = reactive({
  packageName: '',
  price: 0,
  durationHours: 0,
  validDays: 0,
  sortOrder: 0,
  status: 1,
  promotion: ''
})

const rules = {
  packageName: [{ required: true, message: '套餐名称不能为空', trigger: 'blur' }],
  price: [{ required: true, message: '价格不能为空', trigger: 'blur' }],
  durationHours: [{ required: true, message: '时长不能为空', trigger: 'blur' }]
}

function getList(page) {
  loading.value = true
  if (page) {
    queryParams.pageNum = page.pageNum
    queryParams.pageSize = page.pageSize
  }
  listPackages(queryParams).then(res => {
    packageList.value = res.list
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
  queryParams.packageName = undefined
  queryParams.status = undefined
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增套餐'
  form.packageName = ''
  form.price = 0
  form.durationHours = 0
  form.validDays = 0
  form.sortOrder = 0
  form.status = 1
  form.promotion = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑套餐'
  form.packageName = row.packageName
  form.price = row.price
  form.durationHours = row.durationHours
  form.validDays = row.validDays
  form.sortOrder = row.sortOrder
  form.status = row.status
  form.promotion = row.promotion || ''
  dialogVisible.value = true
}

function handleSubmit() {
  const data = {
    packageName: form.packageName,
    price: form.price,
    durationHours: form.durationHours,
    validDays: form.validDays,
    sortOrder: form.sortOrder,
    status: form.status,
    promotion: form.promotion
  }
  submitLoading.value = true
  const action = isEdit.value
    ? updatePackage(editId.value, data)
    : createPackage(data)
  action.then(() => {
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getList()
  }).finally(() => {
    submitLoading.value = false
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除套餐「${row.packageName}」？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  }).then(() => {
    deletePackage(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => getList())
</script>