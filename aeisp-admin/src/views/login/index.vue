<template>
  <div class="login-container">
    <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="login-title">{{ platformName }}</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="密码"
          prefix-icon="Lock"
          show-password
          @keyup.enter="handleLogin"
        />
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" class="login-button" @click="handleLogin">
          登 录
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const platformName = ref('AEISP 后台管理')

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const res = await request.get('/platform/info')
    if (res && res.name) {
      platformName.value = res.name + ' 后台管理'
    }
  } catch {
    // 忽略平台信息加载失败
  }
})

async function handleLogin() {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    router.push(redirect || '/')
  } catch (error) {
    if (error?.response?.status === 503 || error?.message?.includes('维护')) {
      ElMessage.error('系统维护中，请稍后再试')
    } else {
      ElMessage.error(error?.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #2d3a4b;
}
.login-form {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
}
.login-button {
  width: 100%;
}
</style>
