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
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const formRef = ref<FormInstance>()
const form = reactive({ phone: '', password: '' })

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1(3[0-9]|4[579]|5[0-35-9]|6[6]|7[0135678]|8[0-9]|9[89])\d{8}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 32, message: '密码为4~32位', trigger: 'blur' },
  ],
}

function handleEnter() { submit() }

async function submit(): Promise<{ phone: string; password: string } | null> {
  try { await formRef.value!.validate() } catch { return null }
  return { phone: form.phone, password: form.password }
}

defineExpose({ submit, clearValidate: () => formRef.value?.clearValidate() })
</script>
