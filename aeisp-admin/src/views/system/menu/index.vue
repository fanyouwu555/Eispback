<template>
  <div class="app-container">
    <el-row class="mb-2">
      <el-button type="primary" plain icon="Plus" @click="handleAdd">新增菜单</el-button>
      <el-button plain icon="Refresh" @click="getTree">刷新</el-button>
    </el-row>

    <el-table v-loading="loading" :data="menuList" row-key="id" default-expand-all stripe
              :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
      <el-table-column label="排序" width="50" align="center">
        <template #default>
          <el-icon class="drag-handle" style="cursor: grab;"><Rank /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="菜单名称" prop="permissionName" min-width="180" />
      <el-table-column label="图标" width="60" align="center">
        <template #default="scope">
          <el-icon v-if="scope.row.icon"><component :is="scope.row.icon" /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sortOrder" width="60" align="center" />
      <el-table-column label="权限标识" prop="permissionCode" min-width="140" />
      <el-table-column label="路由路径" prop="routePath" min-width="120" />
      <el-table-column label="组件路径" prop="component" min-width="140" show-overflow-tooltip />
      <el-table-column label="类型" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.menuType === 0" size="small">目录</el-tag>
          <el-tag v-else-if="scope.row.menuType === 1" type="success" size="small">菜单</el-tag>
          <el-tag v-else-if="scope.row.menuType === 2" type="warning" size="small">按钮</el-tag>
          <el-tag v-else-if="scope.row.menuType === 3" type="info" size="small">外链</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="handleAddChild(scope.row)">添加子项</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogOpen" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="menuList" :props="{ label: 'permissionName', children: 'children' }"
                          placeholder="选择上级菜单" clearable filterable check-strictly node-key="id" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
            <el-radio :value="3">外链</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="permissionName">
          <el-input v-model="form.permissionName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permissionCode">
          <el-input v-model="form.permissionCode" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="routePath" v-if="form.menuType === 1 || form.menuType === 3">
          <el-input v-model="form.routePath" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="form.menuType === 1">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="是否可见" prop="isVisible">
          <el-radio-group v-model="form.isVisible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否缓存" prop="isCache">
          <el-radio-group v-model="form.isCache">
            <el-radio :value="1">缓存</el-radio>
            <el-radio :value="0">不缓存</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import { getMenuTree, createMenu, updateMenu, deleteMenu, getMenu } from '@/api/system/menu'
import Sortable from 'sortablejs'

const loading = ref(false)
const menuList = ref([])
const dialogOpen = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const initForm = { id: undefined, parentId: undefined, menuType: 0, permissionName: '', permissionCode: '', sortOrder: 0, icon: '', routePath: '', component: '', isVisible: 1, isCache: 1 }
const form = reactive({ ...initForm })

const rules = {
  permissionName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '权限标识不能为空', trigger: 'blur' }]
}

async function getTree() {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuList.value = res || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  Object.assign(form, initForm)
  dialogTitle.value = '新增菜单'
  dialogOpen.value = true
}

function handleAddChild(row) {
  Object.assign(form, initForm)
  form.parentId = row.id
  dialogTitle.value = '新增子菜单'
  dialogOpen.value = true
}

async function handleUpdate(row) {
  try {
    const res = await getMenu(row.id)
    Object.assign(form, res)
    dialogTitle.value = '编辑菜单'
    dialogOpen.value = true
  } catch (e) {
    ElMessage.error('获取菜单信息失败')
  }
}

async function submitForm() {
  await formRef.value.validate()
  if (form.id) {
    await updateMenu(form.id, form)
    ElMessage.success('修改成功')
  } else {
    await createMenu(form)
    ElMessage.success('新增成功')
  }
  dialogOpen.value = false
  getTree()
}

function resetForm() {
  formRef.value?.resetFields()
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除菜单 "${row.permissionName}" 吗？如果有子菜单，删除将失败。`, '提示', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    getTree()
  }).catch(() => {})
}

let sortable = null

function initDrag() {
  const el = document.querySelector('.el-table__body-wrapper tbody')
  if (!el || sortable) return
  sortable = Sortable.create(el, {
    handle: '.drag-handle',
    animation: 150,
    onEnd: async ({ newIndex, oldIndex }) => {
      if (newIndex === oldIndex) return
      const flatRows = getFlatRows(menuList.value)
      if (oldIndex < flatRows.length && newIndex < flatRows.length) {
        const movedItem = flatRows[oldIndex]
        const targetItem = flatRows[newIndex]
        const newSortOrder = targetItem.sortOrder
        try {
          await updateMenu(movedItem.id, { ...movedItem, sortOrder: newSortOrder })
          ElMessage.success('排序更新成功')
          getTree()
        } catch (e) {
          ElMessage.error('排序更新失败')
        }
      }
    }
  })
}

function getFlatRows(tree) {
  const result = []
  function walk(nodes) {
    for (const node of nodes) {
      result.push(node)
      if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }
  walk(tree)
  return result
}

onMounted(() => {
  getTree()
  nextTick(() => {
    initDrag()
  })
})
</script>

<style scoped>
.mb-2 { margin-bottom: 12px; }
</style>