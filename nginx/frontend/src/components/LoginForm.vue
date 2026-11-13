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

    <!-- 密码 -->
    <el-form-item prop="password">
      <el-input
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        show-password
        class="app-input"
      >
        <template #prefix>
          <span class="app-input-icon">🔑</span>
        </template>
      </el-input>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const props = withDefaults(defineProps<{ channel?: 'phone' | 'email' }>(), {
  channel: 'phone',
})

const formRef = ref<FormInstance>()
const form = reactive({ phone: '', email: '', password: '' })

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
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 32, message: '密码为4~32位', trigger: 'blur' },
  ],
}

function handleEnter() { submit() }

async function submit(): Promise<{ phone: string; email: string; password: string } | null> {
  try { await formRef.value!.validate() } catch { return null }
  return { phone: form.phone, email: form.email.trim(), password: form.password }
}

defineExpose({ submit, clearValidate: () => formRef.value?.clearValidate() })

watch(() => props.channel, () => formRef.value?.clearValidate())
</script>
