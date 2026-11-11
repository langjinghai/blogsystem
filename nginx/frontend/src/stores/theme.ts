/**
 * ============================================
 *  AIOT Nexus — 主题 Store
 *  双主题（浅色 / 深色）+ 持久化
 * ============================================
 */
import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export type ThemeMode = 'light' | 'dark'

function getSavedTheme(): ThemeMode {
  const saved = localStorage.getItem('app_theme')
  if (saved === 'light' || saved === 'dark') return saved
  // 跟随系统偏好
  if (window.matchMedia?.('(prefers-color-scheme: dark)').matches) return 'dark'
  return 'light'
}

function applyTheme(t: ThemeMode) {
  document.documentElement.setAttribute('data-theme', t)
}

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<ThemeMode>(getSavedTheme())

  // 初始化时立即应用
  applyTheme(theme.value)

  function setTheme(t: ThemeMode) {
    theme.value = t
    localStorage.setItem('app_theme', t)
    applyTheme(t)
  }

  function toggleTheme() {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  // 监听系统主题变化（仅在用户未手动设置时）
  const mq = window.matchMedia('(prefers-color-scheme: dark)')
  mq.addEventListener('change', (e) => {
    if (!localStorage.getItem('app_theme')) {
      setTheme(e.matches ? 'dark' : 'light')
    }
  })

  return { theme, setTheme, toggleTheme }
})
