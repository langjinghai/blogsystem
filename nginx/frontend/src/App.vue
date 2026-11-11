<template>
  <router-view v-slot="{ Component }">
    <transition name="page-fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'

// 初始化主题（确保 DOM 属性在渲染前设置）
useThemeStore()

// 移除 FOUC 防护 class
onMounted(() => {
  document.documentElement.classList.remove('no-transition')
})
</script>

<style lang="scss">
// ==================== 页面切换过渡 ====================
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
