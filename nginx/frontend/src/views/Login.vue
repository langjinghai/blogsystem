<template>
  <AuthLayout
    title="AIOT Nexus"
    subtitle="登录 · 连接神经矩阵"
    :ai-messages="aiMessages"
  >
    <!-- Tabs 切换 -->
    <el-tabs v-model="mode" class="app-tabs" @before-leave="handleTabSwitch">
      <el-tab-pane label="验证码登录" name="code">
        <SmsLoginForm ref="smsFormRef" />
      </el-tab-pane>
      <el-tab-pane label="密码登录" name="password">
        <LoginForm ref="pwdFormRef" />
      </el-tab-pane>
    </el-tabs>

    <el-button
      class="app-btn--primary"
      :loading="loading"
      :disabled="loading"
      @click="handleLogin"
    >
      连接矩阵
    </el-button>

    <div class="app-card__links">
      <router-link to="/register" class="app-link">注册新账号</router-link>
      <span class="app-link-divider">|</span>
      <router-link to="/forget" class="app-link">忘记密码</router-link>
    </div>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login, type LoginParams } from '@/api/user'
import AuthLayout from '@/layouts/AuthLayout.vue'
import SmsLoginForm from '@/components/SmsLoginForm.vue'
import LoginForm from '@/components/LoginForm.vue'

const router = useRouter()
const userStore = useUserStore()

const mode = ref<'code' | 'password'>('code')
const smsFormRef = ref<InstanceType<typeof SmsLoginForm>>()
const pwdFormRef = ref<InstanceType<typeof LoginForm>>()
const loading = ref(false)

const aiMessages = [
  '指挥官，欢迎连接 AIOT Nexus 系统。',
  '请选择登录方式。',
  '当前系统状态：在线 ▮',
]

async function handleLogin() {
  const active = mode.value === 'code' ? smsFormRef : pwdFormRef
  const data = await active.value?.submit()
  if (!data) return

  const params: LoginParams = 'code' in data!
    ? { phone: data!.phone, code: data!.code }
    : { phone: data!.phone, password: (data as any).password }

  loading.value = true
  try {
    const res = await login(params)
    if (res.success) {
      const token = res.data as string | null
      if (token) {
        userStore.setToken(token)
        await userStore.loadUserInfo()
      } else {
        ElMessage.warning('登录成功但未获取到token')
        return
      }
      ElMessage.success('连接成功！欢迎回来，指挥官。')
      await router.replace('/home')
    } else {
      ElMessage.error(res.errorMag || '登录失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || err?.message || '网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleTabSwitch() {
  nextTick(() => {
    smsFormRef.value?.clearValidate()
    pwdFormRef.value?.clearValidate()
  })
  return true
}
</script>
