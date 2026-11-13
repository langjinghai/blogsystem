<template>
  <AuthLayout
    title="重置密码"
    subtitle="重置神经矩阵认证凭据"
    :ai-messages="aiMessages"
  >
    <el-radio-group
      v-model="resetType"
      size="small"
      style="display:flex;justify-content:center;margin-bottom:14px"
    >
      <el-radio-button value="phone">手机号找回</el-radio-button>
      <el-radio-button value="email">邮箱找回</el-radio-button>
    </el-radio-group>

    <ForgetPasswordForm ref="formRef" :channel="resetType" />

    <el-button
      class="app-btn--primary"
      :loading="loading"
      :disabled="loading"
      @click="handleReset"
    >
      重置凭据
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
import { forgetPassword, emailForgetPassword } from '@/api/user'
import AuthLayout from '@/layouts/AuthLayout.vue'
import ForgetPasswordForm from '@/components/ForgetPasswordForm.vue'

const router = useRouter()
const formRef = ref<InstanceType<typeof ForgetPasswordForm>>()
const loading = ref(false)
const resetType = ref<'phone' | 'email'>('phone')

const aiMessages = [
  '检测到凭据丢失，指挥官。',
  '通过手机验证重置您的神经矩阵认证。',
  '当前系统状态：凭据恢复模式 ▮',
]

async function handleReset() {
  const data = await formRef.value?.submit()
  if (!data) return

  loading.value = true
  try {
    const res = resetType.value === 'email'
      ? await emailForgetPassword({
          email: data.email,
          code: data.code,
          newPassword: data.newPassword,
          confirmPassword: data.confirmPassword,
        })
      : await forgetPassword({
          phone: data.phone,
          code: data.code,
          newPassword: data.newPassword,
          confirmPassword: data.confirmPassword,
        })
    if (res.success) {
      ElMessage.success('密码重置成功！请使用新凭据重新登录。')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      ElMessage.error(res.errorMag || '重置密码失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || err?.message || '网络异常')
  } finally {
    loading.value = false
  }
}
</script>
