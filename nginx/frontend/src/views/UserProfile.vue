<template>
  <div class="app-page-container app-profile-page">
    <div class="app-section-header">
      <h3 class="app-section-header__title">用户主页</h3>
    </div>

    <div class="app-card" v-loading="loading">
      <!-- 用户信息 -->
      <div class="app-profile-page__header">
        <div class="app-profile-page__avatar-wrap">
          <img
            v-if="profile.icon"
            :src="profile.icon"
            class="app-profile-page__avatar"
            alt="avatar"
          />
          <div v-else class="app-profile-page__avatar-placeholder">
            {{ (profile.nickName || 'U').charAt(0).toUpperCase() }}
          </div>
        </div>

        <div class="app-profile-page__info">
          <h2>{{ profile.nickName }}</h2>
          <p v-if="profile.phone">手机号：{{ maskedPhone }}</p>
          <div class="app-profile-page__stats">
            <div>
              <div class="stat-value">{{ profile.articleCount || 0 }}</div>
              <div class="stat-label">文章</div>
            </div>
            <div>
              <div class="stat-value">{{ profile.followerCount || 0 }}</div>
              <div class="stat-label">粉丝</div>
            </div>
            <div>
              <div class="stat-value">{{ profile.followingCount || 0 }}</div>
              <div class="stat-label">关注</div>
            </div>
          </div>
        </div>

        <div v-if="!isSelf" style="margin-left:auto">
          <FollowButton
            :followee-id="userId"
            :initial-followed="profile.isFollowed || false"
            @update:followed="onFollowChange"
          />
        </div>
      </div>

      <!-- 该用户的文章列表 -->
      <div style="margin-top:32px; padding-top:24px; border-top:1px solid var(--border-color)">
        <h4 style="margin-bottom:16px; font-size:16px; font-weight:600">
          {{ isSelf ? '我的文章' : 'TA 的文章' }}
        </h4>

        <div v-if="articles.length > 0" class="article-list">
          <ArticleCard
            v-for="a in articles"
            :key="a.id"
            :article="a"
          />
        </div>
        <div v-else-if="!loadingArticles" class="app-empty-state">
          <div class="app-empty-state__text">暂无文章</div>
        </div>

        <div v-if="totalArticles > pageSize" class="app-pagination-wrap">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="totalArticles"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="onPageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserProfileById, type UserInfo } from '@/api/user'
import { getUserArticles, type Article } from '@/api/article'
import FollowButton from '@/components/FollowButton.vue'
import ArticleCard from '@/components/ArticleCard.vue'

const route = useRoute()
const userStore = useUserStore()

const userId = computed(() => Number(route.params.id))
const isSelf = computed(() => userId.value === userStore.userInfo?.id)

const loading = ref(true)
const loadingArticles = ref(false)
const profile = ref<UserInfo>({} as UserInfo)

// 文章列表
const articles = ref<Article[]>([])
const totalArticles = ref(0)
const currentPage = ref(1)
const pageSize = 10

const maskedPhone = computed(() => {
  const phone = profile.value.phone || ''
  if (phone.length >= 11) {
    return phone.slice(0, 3) + '****' + phone.slice(7)
  }
  return phone
})

onMounted(() => {
  loadProfile()
})

watch(userId, () => {
  loadProfile()
})

async function loadProfile() {
  loading.value = true
  try {
    const res = await getUserProfileById(userId.value)
    if (res.success && res.data) {
      profile.value = res.data as UserInfo
    } else {
      ElMessage.error(res.errorMag || '用户不存在')
    }
  } catch {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
  await loadArticles()
}

async function loadArticles() {
  loadingArticles.value = true
  try {
    const res = await getUserArticles(userId.value, currentPage.value, pageSize)
    if (res.success && res.data) {
      articles.value = res.data.records || []
      totalArticles.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载文章列表失败')
  } finally {
    loadingArticles.value = false
  }
}

function onPageChange(page: number) {
  currentPage.value = page
  loadArticles()
}

function onFollowChange(followed: boolean) {
  if (profile.value) {
    profile.value.isFollowed = followed
    // 更新粉丝数（前端本地更新，不需要重新请求）
    if (profile.value.followerCount != null) {
      profile.value.followerCount += followed ? 1 : -1
    }
  }
}
</script>
