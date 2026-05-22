<template>
  <div class="app-container">
    <el-alert title="模板三级分类管理" type="info" :closable="false" show-icon class="mb-4">
      <template #default>一级分类 → 二级分类 → 三级分类，支持动态编辑</template>
    </el-alert>

    <el-row :gutter="16">
      <!-- 分类树 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <span>分类树</span>
            <el-button type="success" size="small" icon="Plus" class="float-right" @click="handleAddRoot">新增一级分类</el-button>
          </template>
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="treeProps"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <span>{{ data.name }}</span>
                <span class="tree-actions">
                  <el-button v-if="(data.level || 0) < 2" type="primary" link size="small" icon="Plus" @click.stop="handleAddChild(data)">子级</el-button>
                  <el-button type="primary" link size="small" icon="Edit" @click.stop="handleEdit(data)">编辑</el-button>
                  <el-button v-if="(data.level || 0) === 0" type="danger" link size="small" icon="Delete" @click.stop="handleDelete(data)">删除</el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 编辑表单 -->
      <el-col :span="14">
        <el-card>
          <template #header>{{ formTitle }}</template>
          <el-form :model="form" ref="formRef" label-width="100px" :rules="rules" v-if="formVisible">
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" />
            </el-form-item>
            <el-form-item v-if="!isEdit && form.level !== 0" label="上级分类" prop="parentId">
              <el-cascader
                v-model="form.parentId"
                :options="treeData"
                :props="{ value: 'id', label: 'name', children: 'children', emitPath: false, checkStrictly: true }"
                placeholder="顶级分类请留空"
                clearable
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="层级">
              <el-tag>{{ levelLabel }}</el-tag>
            </el-form-item>
            <el-form-item label="排序值" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width: 200px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
          <el-empty v-else description="请在左侧选择或新增分类" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryTree, createCategory, updateCategory, deleteCategory } from '@/api/template/category'

const treeRef = ref(null)
const treeData = ref([])
const formVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const treeProps = { children: 'children', label: 'name' }

const form = reactive({
  name: '',
  parentId: undefined,
  level: 0,
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
}

const levelLabel = computed(() => {
  const levels = ['一级分类', '二级分类', '三级分类']
  return levels[form.level] || '未知'
})

const formTitle = computed(() => isEdit.value ? '编辑分类' : '新增分类')

function loadTree() {
  getCategoryTree().then(res => {
    treeData.value = res || []
  })
}

function findLevel(id, nodes) {
  for (const n of nodes) {
    if (n.id === id) return n.level !== undefined ? n.level + 1 : 1
    if (n.children && n.children.length) {
      const l = findLevel(id, n.children)
      if (l >= 0) return l
    }
  }
  return 0
}

function handleAddRoot() {
  isEdit.value = false
  editId.value = null
  form.name = ''
  form.parentId = undefined
  form.level = 0
  form.sortOrder = 0
  formVisible.value = true
}

function handleAddChild(parent) {
  if ((parent.level || 0) >= 2) {
    ElMessage.warning('最多支持三级分类')
    return
  }
  isEdit.value = false
  editId.value = null
  form.name = ''
  form.parentId = parent.id
  form.level = (parent.level || 0) + 1
  form.sortOrder = 0
  formVisible.value = true
}

function handleEdit(data) {
  isEdit.value = true
  editId.value = data.id
  form.name = data.name
  form.parentId = data.parentId || undefined
  form.level = data.level || 0
  form.sortOrder = data.sortOrder || 0
  formVisible.value = true
}

function handleNodeClick(data) {
  handleEdit(data)
}

function handleSave() {
  saving.value = true
  const payload = isEdit.value
    ? { name: form.name, sortOrder: form.sortOrder }
    : {
        name: form.name,
        parentId: form.parentId || 0,
        level: form.level,
        sortOrder: form.sortOrder
      }
  const action = isEdit.value
    ? updateCategory(editId.value, payload)
    : createCategory(payload)
  action.then(() => {
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    formVisible.value = false
    loadTree()
  }).finally(() => {
    saving.value = false
  })
}

function handleDelete(data) {
  ElMessageBox.confirm(`确认删除分类「${data.name}」？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger'
  }).then(() => {
    deleteCategory(data.id).then(() => {
      ElMessage.success('删除成功')
      loadTree()
    })
  }).catch(() => {})
}

function resetForm() {
  form.name = ''
  form.parentId = undefined
  form.sortOrder = 0
}

onMounted(() => loadTree())
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.float-right { float: right; }
.tree-node { display: flex; align-items: center; justify-content: space-between; width: 100%; padding-right: 8px; }
.tree-actions { white-space: nowrap; display: none; }
.tree-node:hover .tree-actions { display: inline; }
</style>