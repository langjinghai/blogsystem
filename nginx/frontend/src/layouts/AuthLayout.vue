<template>
  <div class="app-page">
    <!-- ====== 背景层 ====== -->
    <div class="app-bg">
      <div class="app-bg__grid"></div>
      <div class="app-bg__glow app-bg__glow--primary"></div>
      <div class="app-bg__glow app-bg__glow--accent"></div>
    </div>

    <!-- ====== 卡片区 ====== -->
    <div class="app-card-wrapper">
      <div class="app-card--auth">
        <h1 class="app-card__title">{{ title }}</h1>
        <p class="app-card__subtitle">{{ subtitle }}</p>
        <slot />
      </div>
    </div>

    <!-- ====== AI 助手 ====== -->
    <AiAssistant :messages="aiMessages" class="app-ai-panel" />
  </div>
</template>

<script setup lang="ts">
import AiAssistant from '@/components/AiAssistant.vue'

defineProps<{
  title: string
  subtitle: string
  aiMessages: string[]
}>()
</script>
