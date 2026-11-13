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
        placeholder="请输入注册手机号"
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
        placeholder="请输入注册邮箱"
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

    <!-- 新密码 -->
    <el-form-item prop="newPassword">
      <el-input
        v-model="form.newPassword"
        type="password"
        placeholder="设置新密码（4~32位）"
        show-password
        class="app-input"
      >
        <template #prefix>
          <span class="app-input-icon">🔐</span>
        </template>
      </el-input>
      <div class="password-strength" v-if="form.newPassword">
        <div class="strength-bar">
          <div class="strength-fill" :class="strengthClass" :style="{ width: strengthPercent + '%' }"></div>
        </div>
        <span class="strength-text" :class="strengthClass">{{ strengthLabel }}</span>
      </div>
    </el-form-item>

    <!-- 确认新密码 -->
    <el-form-item prop="confirmPassword">
      <el-input
        v-model="form.confirmPassword"
        type="password"
        placeholder="再次输入新密码"
        show-password
        class="app-input"
      >
        <template #prefix>
          <span class="app-input-icon">🔏</span>
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
const form = reactive({ phone: '', email: '', code: '', newPassword: '', confirmPassword: '' })

const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

const validatePwd = (_: any, value: string, callback: any) => {
  if (!value) return callback(new Error('请设置新密码'))
  if (value.length < 4 || value.length > 32) return callback(new Error('密码长度为4~32位'))
  if (!/^\w{4,32}$/.test(value)) return callback(new Error('密码仅支持字母、数字、下划线'))
  callback()
}

const validateConfirm = (_: any, value: string, callback: any) => {
  if (!value) return callback(new Error('请再次输入新密码'))
  if (value !== form.newPassword) return callback(new Error('两次密码不一致'))
  callback()
}

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
  newPassword: [{ required: true, validator: validatePwd, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
}

const accountValid = computed(() => {
  return props.channel === 'email'
    ? EMAIL_REGEX.test(form.email.trim())
    : form.phone.length === 11
})

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
      ? await sendEmailCode(form.email.trim(), 'forget')
      : await sendCode(form.phone, 'forget')
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
    ElMessage.error(err?.response?.data?.errorMag || err?.message || '网络异常')
  } finally {
    sending.value = false
  }
}

const strength = computed(() => {
  const p = form.newPassword; let s = 0
  if (p.length >= 8) s++; if (p.length >= 12) s++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s++
  if (/\d/.test(p)) s++; if (/[^a-zA-Z\d]/.test(p)) s++
  if (s <= 1) return { pct: 25, label: '强度：弱', cls: 'weak' }
  if (s <= 2) return { pct: 50, label: '强度：中', cls: 'medium' }
  if (s <= 3) return { pct: 75, label: '强度：强', cls: 'strong' }
  return { pct: 100, label: '强度：极强', cls: 'very-strong' }
})
const strengthPercent = computed(() => strength.value.pct)
const strengthLabel = computed(() => strength.value.label)
const strengthClass = computed(() => strength.value.cls)

function handleEnter() { submit() }

async function submit() {
  try { await formRef.value!.validate() } catch { return null }
  return {
    phone: form.phone,
    email: form.email.trim(),
    code: form.code,
    newPassword: form.newPassword,
    confirmPassword: form.confirmPassword,
  }
}

defineExpose({ submit, clearValidate: () => formRef.value?.clearValidate() })

watch(() => props.channel, () => formRef.value?.clearValidate())
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style lang="scss" scoped>
.password-strength { display: flex; align-items: center; gap: 10px; margin-top: 8px; padding: 0 4px; }
.strength-bar { flex: 1; height: 4px; background: var(--bg-input); border-radius: 2px; overflow: hidden; }
.strength-fill {
  height: 100%; border-radius: 2px; transition: width .4s ease, background .4s ease;
  &.weak { background: #ef4444; }
  &.medium { background: #f59e0b; }
  &.strong { background: #10b981; }
  &.very-strong { background: var(--color-primary); box-shadow: 0 0 8px var(--color-primary-glow); }
}
.strength-text {
  font-size: 11px; white-space: nowrap; transition: color .3s ease;
  &.weak { color: #ef4444; } &.medium { color: #f59e0b; }
  &.strong { color: #10b981; } &.very-strong { color: var(--color-primary); }
}
</style>
