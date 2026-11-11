import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo as fetchUserInfo, type UserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // ---------- state ----------
  const token = ref<string>(localStorage.getItem('auth_token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const infoLoaded = ref(false)

  // ---------- getters ----------
  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickName || '未命名用户')
  const avatar = computed(() => userInfo.value?.icon || '')

  // ---------- actions ----------
  function setToken(t: string) {
    token.value = t
    if (t) {
      localStorage.setItem('auth_token', t)
    } else {
      localStorage.removeItem('auth_token')
    }
  }

  async function loadUserInfo(): Promise<UserInfo | null> {
    if (!token.value) return null
    if (infoLoaded.value && userInfo.value) return userInfo.value
    try {
      const res = await fetchUserInfo()
      if (res.success && res.data) {
        userInfo.value = res.data as UserInfo
        infoLoaded.value = true
        return userInfo.value
      }
      return null
    } catch {
      console.error('用户信息加载失败')
      return null
    }
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    infoLoaded.value = true
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    infoLoaded.value = false
    localStorage.removeItem('auth_token')
  }

  return { token, userInfo, infoLoaded, isLoggedIn, nickname, avatar, setToken, loadUserInfo, setUserInfo, logout }
})
