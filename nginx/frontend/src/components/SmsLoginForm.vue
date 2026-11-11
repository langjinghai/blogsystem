<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    @keyup.enter="handleEnter"
  >
    <!-- 手机号 -->
    <el-form-item prop="phone">
      <el-input
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
            :disabled="countdown > 0 || form.phone.length < 11"
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
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { sendCode } from '@/api/user'

const formRef = ref<FormInstance>()
const form = reactive({ phone: '', code: '' })

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1(3[0-9]|4[579]|5[0-35-9]|6[6]|7[0135678]|8[0-9]|9[89])\d{8}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^[a-zA-Z\d]{6}$/, message: '验证码为6位数字或字母', trigger: 'blur' },
  ],
}

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
  const valid = await formRef.value?.validateField('phone').catch(() => false)
  if (!valid) return
  sending.value = true
  try {
    const res = await sendCode(form.phone)
    if (res.success) { ElMessage.success('验证码已发送，请查看后端控制台'); startCountdown() }
    else { ElMessage.error(res.errorMag || '发送验证码失败') }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || err?.message || '网络异常，请稍后重试')
  } finally { sending.value = false }
}

// ==================== 提交 API ====================

/** Enter 键触发 */
function handleEnter() {
  submit()
}

/**
 * 校验表单并返回数据，校验失败返回 null。
 * 父组件通过 ref 调用：`const data = await formRef.value?.submit()`
 */
async function submit(): Promise<{ phone: string; code: string } | null> {
  try { await formRef.value!.validate() } catch { return null }
  return { phone: form.phone, code: form.code }
}

defineExpose({ submit, clearValidate: () => formRef.value?.clearValidate() })

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
