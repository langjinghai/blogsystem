<template>
  <el-input
    v-model="keyword"
    :placeholder="placeholder"
    clearable
    class="app-search"
    @keyup.enter="handleSearch"
    @input="handleInput"
    @clear="handleClear"
  >
    <template #prefix>
      <span class="search-icon">🔍</span>
    </template>
  </el-input>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
}>(), {
  modelValue: '',
  placeholder: '搜索文章...',
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
  (e: 'search', v: string): void
}>()

const keyword = ref(props.modelValue)

function handleInput() {
  emit('update:modelValue', keyword.value)
}

function handleSearch() {
  emit('update:modelValue', keyword.value)
  emit('search', keyword.value)
}

function handleClear() {
  keyword.value = ''
  emit('update:modelValue', '')
  emit('search', '')
}
</script>
