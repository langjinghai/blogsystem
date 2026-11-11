<template>
  <div class="app-ai-panel">
    <div class="app-ai-panel__placeholder">
      <span class="app-ai-panel__icon">🤖</span>
      <span class="app-ai-panel__label">AI Assistant</span>
    </div>
    <div class="app-ai-panel__messages">
      <p v-for="(msg, i) in messages" :key="i" class="app-ai-panel__message">{{ msg }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
/** AI 助手面板组件 — 根据页面显示不同提示语 */
defineProps<{
  messages: string[]
}>()
</script>
