<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="roleList" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="角色名称" prop="roleName" />
      <el-table-column label="角色编码" prop="roleCode" />
      <el-table-column label="描述" prop="description" />
      <el-table-column label="类型" align="center" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isSystem === 1" type="warning">系统内置</el-tag>
          <el-tag v-else type="info">自定义</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createdAt" width="180" />
      <el-table-column label="操作" align="center" width="250">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="primary" icon="User" @click="handleAssignPermission(row)">分配权限</el-button>
          <el-button v-if="row.isSystem !== 1" link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="open" :title="title" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permOpen" title="分配权限" width="600px">
      <el-tree
        ref="treeRef"
        :data="permissionList"
        show-checkbox
        node-key="id"
        :props="{ label: 'permissionName', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitPermission">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRoles, addRole, updateRole, deleteRole, listPermissions } from '@/api/system'

const loading = ref(false)
const roleList = ref([])
const open = ref(false)
const permOpen = ref(false)
const title = ref('')
const formRef = ref(null)
const treeRef = ref(null)
const permissionList = ref([])
const currentRole = ref(null)

const form = reactive({ id: undefined, roleName: '', roleCode: '', description: '' })
const rules = {
  roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
  roleCode: [{ required: true, message: '角色编码不能为空', trigger: 'blur' }]
}

async function getList() {
  loading.value = true
  try {
    const res = await listRoles()
    roleList.value = res.list || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  Object.assign(form, { id: undefined, roleName: '', roleCode: '', description: '' })
  open.value = true
  title.value = '新增角色'
}

function handleUpdate(row) {
  Object.assign(form, row)
  open.value = true
  title.value = '编辑角色'
}

async function handleAssignPermission(row) {
  currentRole.value = row
  const res = await listPermissions()
  permissionList.value = res || []
  permOpen.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除角色 "${row.roleName}" 吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

async function submitForm() {
  try {
    await formRef.value.validate()
    if (form.id) {
      await updateRole(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await addRole(form)
      ElMessage.success('新增成功')
    }
    open.value = false
    getList()
  } catch {}
}

async function submitPermission() {
  const checkedKeys = treeRef.value.getCheckedKeys()
  await updateRole(currentRole.value.id, { permissionIds: checkedKeys })
  permOpen.value = false
  ElMessage.success('权限分配成功')
}

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
.mb8 { margin-bottom: 8px; }
</style>
