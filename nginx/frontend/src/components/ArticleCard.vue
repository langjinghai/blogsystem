<template>
  <div class="app-article-card" @click="goDetail">
    <div class="app-article-card__cover">
      <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
      <div v-else class="app-article-card__cover-placeholder">📄</div>
    </div>
    <div class="app-article-card__body">
      <h3 class="app-article-card__title">{{ article.title }}</h3>
      <p class="app-article-card__summary">{{ article.summary || '暂无摘要' }}</p>
      <div class="app-article-card__meta">
        <span class="app-article-card__meta-item">
          <span class="meta-icon">👁</span> {{ formatCount(article.viewCount) }}
        </span>
        <span class="app-article-card__meta-item" :class="{ 'app-article-card__meta-item--liked': article.isLiked }">
          <span class="meta-icon">{{ article.isLiked ? '❤' : '🤍' }}</span> {{ formatCount(article.likeCount) }}
        </span>
        <span class="app-article-card__meta-item">
          <span class="meta-icon">💬</span> {{ formatCount(article.commentCount) }}
        </span>
      </div>
      <div class="app-article-card__author">
        <img
          v-if="article.authorAvatar"
          :src="article.authorAvatar"
          class="app-article-card__author-avatar"
          alt="author"
        />
        <span v-else class="app-article-card__author-avatar-placeholder">U</span>
        <span class="app-article-card__author-name">{{ article.authorName }}</span>
        <span class="app-article-card__author-time">{{ formatTime(article.createTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Article } from '@/api/article'

const props = defineProps<{ article: Article }>()
const router = useRouter()

function goDetail() {
  router.push(`/post/${props.article.id}`)
}

function formatCount(n: number): string {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function formatTime(t: string): string {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return d.toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.app-article-card__meta-item--liked {
  .meta-icon {
    animation: likedPulse 0.4s ease;
  }
}

@keyframes likedPulse {
  0%   { transform: scale(1); }
  50%  { transform: scale(1.3); }
  100% { transform: scale(1); }
}
</style>
