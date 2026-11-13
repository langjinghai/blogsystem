<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    @keyup.enter="handleEnter"
  >
    <!-- 手机号 / 邮箱 -->
    <el-form-item :prop="props.channel">
      <el-input
        v-if="props.channel === 'phone'"
        v-model="form.phone"
        placeholder="请输入手机号"
        maxlength="11"
        class="app-input"
        @input="form.phone = form.phone.replace(/\D/g, '')"
      >
        <template #prefix>
          <span class="app-input-icon">📡</span>
        </template>
      </el-input>
      <el-input
        v-else
        v-model="form.email"
        placeholder="请输入QQ邮箱（或 Foxmail）"
        maxlength="128"
        class="app-input"
      >
        <template #prefix>
          <span class="app-input-icon">📧</span>
        </template>
      </el-input>
    </el-form-item>

    <!-- 验证码 -->
    <el-form-item prop="code">
      <el-input
        v-model="form.code"
        placeholder="请输入验证码"
        maxlength="6"
        class="app-input"
        @input="form.code = form.code.replace(/[^a-zA-Z\d]/g, '')"
      >
        <template #prefix>
          <span class="app-input-icon">💌</span>
        </template>
        <template #suffix>
          <el-button
            class="app-btn--code"
            :disabled="countdown > 0 || !accountValid"
            :loading="sending"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? `${countdown}秒` : '获取验证码' }}
          </el-button>
        </template>
      </el-input>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onUnmounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { sendCode, sendEmailCode } from '@/api/user'

const props = withDefaults(defineProps<{ channel?: 'phone' | 'email' }>(), {
  channel: 'phone',
})

const formRef = ref<FormInstance>()
const form = reactive({ phone: '', email: '', code: '' })

const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1(3[0-9]|4[579]|5[0-35-9]|6[6]|7[0135678]|8[0-9]|9[89])\d{8}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: EMAIL_REGEX, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^[a-zA-Z\d]{6}$/, message: '验证码为6位数字或字母', trigger: 'blur' },
  ],
}

const accountValid = computed(() => {
  return props.channel === 'email'
    ? EMAIL_REGEX.test(form.email.trim())
    : form.phone.length === 11
})

// ==================== 验证码倒计时 ====================
const countdown = ref(0)
const sending = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) { clearInterval(timer!); timer = null }
  }, 1000)
}

async function handleSendCode() {
  const valid = await formRef.value?.validateField(props.channel).catch(() => false)
  if (!valid) return
  sending.value = true
  try {
    const res = props.channel === 'email'
      ? await sendEmailCode(form.email.trim(), 'login')
      : await sendCode(form.phone, 'login')
    if (res.success) {
      ElMessage.success(
        props.channel === 'email'
          ? '验证码已发送到邮箱，请查收'
          : '验证码已发送，请查看后端控制台',
      )
      startCountdown()
    } else {
      ElMessage.error(res.errorMag || '发送验证码失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || err?.message || '网络异常，请稍后重试')
  } finally {
    sending.value = false
  }
}

// ==================== 提交 ====================

function handleEnter() {
  submit()
}

async function submit(): Promise<{ phone: string; email: string; code: string } | null> {
  try { await formRef.value!.validate() } catch { return null }
  return { phone: form.phone, email: form.email.trim(), code: form.code }
}

defineExpose({ submit, clearValidate: () => formRef.value?.clearValidate() })

watch(() => props.channel, () => formRef.value?.clearValidate())
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
