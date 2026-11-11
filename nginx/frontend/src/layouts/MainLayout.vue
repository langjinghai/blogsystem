<template>
  <div class="app-layout">
    <!-- ====== 左侧边栏（桌面端） ====== -->
    <aside class="app-sidebar">
      <router-link to="/home" class="app-sidebar__brand">
        <div class="app-sidebar__logo">⚡</div>
        <span class="app-sidebar__title">AIOT Nexus</span>
      </router-link>

      <SearchBar
        class="app-sidebar__search"
        placeholder="搜索文章..."
        @search="handleSearch"
      />

      <nav class="app-sidebar__nav">
        <CategoryMenu :categories="categories" />
      </nav>

      <div class="app-sidebar__user">
        <UserInfoCard
          :nickname="userStore.nickname"
          :avatar="userStore.avatar"
          :article-count="userStore.userInfo?.articleCount ?? 0"
          :like-count="userStore.userInfo?.likeCount ?? 0"
          :follower-count="userStore.userInfo?.followerCount ?? 0"
          :following-count="userStore.userInfo?.followingCount ?? 0"
        />
      </div>
    </aside>

    <!-- ====== 移动端抽屉导航 ====== -->
    <Teleport to="body">
      <div class="app-drawer-overlay" :class="{ open: drawerOpen }" @click="drawerOpen = false" />
      <aside class="app-drawer" :class="{ open: drawerOpen }">
        <button class="app-drawer__close" @click="drawerOpen = false">✕</button>
        <div style="margin-top:40px;display:flex;flex-direction:column;gap:4px;">
          <router-link
            v-for="link in mobileLinks"
            :key="link.to"
            :to="link.to"
            class="app-category-menu__item"
            @click="drawerOpen = false"
          >
            <span class="cat-name">
              <span class="cat-icon">{{ link.icon }}</span>
              {{ link.label }}
            </span>
          </router-link>
          <div
            class="app-category-menu__item"
            style="border-top:1px solid var(--border-subtle);margin-top:8px;padding-top:16px;color:var(--color-error)"
            @click="handleLogout"
          >
            <span class="cat-name"><span class="cat-icon">🚪</span>退出登录</span>
          </div>
        </div>
      </aside>
    </Teleport>

    <!-- ====== 主内容区 ====== -->
    <div class="app-layout__main">
      <header class="app-header">
        <button class="app-header__hamburger" @click="drawerOpen = !drawerOpen">
          <span></span><span></span><span></span>
        </button>
        <div class="app-header__spacer"></div>

        <ThemeToggle />

        <el-dropdown trigger="click">
          <div class="app-header__user">
            <img
              v-if="userStore.avatar"
              :src="userStore.avatar"
              class="app-header__user-avatar"
              alt="avatar"
            />
            <span v-else class="app-header__user-avatar-placeholder">
              {{ userStore.nickname.charAt(0).toUpperCase() }}
            </span>
            <span class="app-header__user-name">{{ userStore.nickname }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/profile')">
                <span style="margin-right:6px">👤</span> 个人中心
              </el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my/article')">
                <span style="margin-right:6px">📝</span> 我的文章
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <span style="margin-right:6px">🚪</span> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="app-content">
        <router-view />
      </main>

      <footer class="app-footer">
        <div class="app-footer__brand">
          <span>⚡</span>
          <span>AIOT Nexus</span>
        </div>
        <p class="app-footer__copy">© 2026 神经矩阵指挥系统 · 保留所有权利</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/user'
import { getCategoryList } from '@/api/category'
import type { Category } from '@/api/category'
import ThemeToggle from '@/components/ThemeToggle.vue'
import SearchBar from '@/components/SearchBar.vue'
import CategoryMenu from '@/components/CategoryMenu.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'

const userStore = useUserStore()
const router = useRouter()

const drawerOpen = ref(false)
const categories = ref<Category[]>([])
const searchKeyword = ref('')

const mobileLinks = [
  { to: '/home', label: '首页', icon: '🏠' },
  { to: '/article/list', label: '文章列表', icon: '📋' },
  { to: '/article/publish', label: '发布文章', icon: '✍️' },
  { to: '/my/article', label: '我的文章', icon: '📝' },
  { to: '/profile', label: '个人中心', icon: '👤' },
]

onMounted(async () => {
  // 加载用户信息
  if (userStore.isLoggedIn && !userStore.infoLoaded) {
    try {
      await userStore.loadUserInfo()
    } catch {
      // 用户信息接口暂不可用
    }
  }
  // 加载分类
  try {
    const res = await getCategoryList()
    if (res.success && res.data) {
      categories.value = res.data
    }
  } catch {
    // 分类接口暂不可用
  }
})

function handleSearch(keyword: string) {
  searchKeyword.value = keyword
  router.push({ path: '/article/list', query: { keyword: keyword || undefined } })
}

async function handleLogout() {
  drawerOpen.value = false
  try { await logoutApi() } catch { /* 忽略后端未实现 */ }
  userStore.logout()
  ElMessage.success('已退出连接，指挥官。')
  router.push('/login')
}
</script>
