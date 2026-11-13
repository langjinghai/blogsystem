<template>
  <AuthLayout
    title="AIOT Nexus"
    subtitle="注册 · 激活神经链接"
    :ai-messages="aiMessages"
  >
    <el-radio-group
      v-model="registerType"
      size="small"
      style="display:flex;justify-content:center;margin-bottom:14px"
    >
      <el-radio-button value="phone">手机号注册</el-radio-button>
      <el-radio-button value="email">邮箱注册</el-radio-button>
    </el-radio-group>

    <RegisterForm ref="formRef" :channel="registerType" />

    <el-button
      class="app-btn--primary"
      :loading="loading"
      :disabled="loading"
      @click="handleRegister"
    >
      激活链接
    </el-button>

    <div class="app-card__links">
      <router-link to="/login" class="app-link">← 返回登录</router-link>
    </div>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, emailRegister } from '@/api/user'
import AuthLayout from '@/layouts/AuthLayout.vue'
import RegisterForm from '@/components/RegisterForm.vue'

const router = useRouter()
const formRef = ref<InstanceType<typeof RegisterForm>>()
const loading = ref(false)
const registerType = ref<'phone' | 'email'>('phone')

const aiMessages = [
  '新兵报到，指挥官。',
  '完成神经矩阵注册以激活作战权限。',
  '当前系统状态：等待注册 ▮',
]

async function handleRegister() {
  const data = await formRef.value?.submit()
  if (!data) return

  loading.value = true
  try {
    const res = registerType.value === 'email'
      ? await emailRegister({
          email: data.email,
          code: data.code,
          password: data.password,
          confirmPassword: data.confirmPassword,
        })
      : await register({
          phone: data.phone,
          code: data.code,
          password: data.password,
          confirmPassword: data.confirmPassword,
        })
    if (res.success) {
      ElMessage.success('注册成功！神经链接已激活，欢迎加入。')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      ElMessage.error(res.errorMag || '注册失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || err?.message || '网络异常')
  } finally {
    loading.value = false
  }
}
</script>
