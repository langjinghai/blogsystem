<template>
  <button class="app-theme-toggle" @click="themeStore.toggleTheme()" :title="label">
    <span v-if="themeStore.theme === 'light'">🌙</span>
    <span v-else>☀️</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const label = computed(() => (themeStore.theme === 'light' ? '切换到深色模式' : '切换到浅色模式'))
</script>
