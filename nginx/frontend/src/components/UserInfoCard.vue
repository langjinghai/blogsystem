<template>
  <div class="app-user-info-card">
    <img v-if="avatar" :src="avatar" class="app-user-info-card__avatar" alt="avatar" />
    <div v-else class="app-user-info-card__avatar-placeholder">
      {{ (nickname || 'U').charAt(0).toUpperCase() }}
    </div>

    <h3 class="app-user-info-card__name">{{ nickname }}</h3>

    <div class="app-user-info-card__stats">
      <div>
        <div class="stat-value">{{ articleCount }}</div>
        <div class="stat-label">文章</div>
      </div>
      <div>
        <div class="stat-value">{{ likeCount }}</div>
        <div class="stat-label">获赞</div>
      </div>
      <div>
        <div class="stat-value">{{ followerCount }}</div>
        <div class="stat-label">粉丝</div>
      </div>
      <div>
        <div class="stat-value">{{ followingCount }}</div>
        <div class="stat-label">关注</div>
      </div>
    </div>

    <div class="app-user-info-card__actions">
      <el-button class="app-btn--primary" size="small" @click="$router.push('/profile')">
        个人中心
      </el-button>
      <el-button class="app-btn--outline" size="small" @click="$router.push('/my/article')">
        我的文章
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  nickname?: string
  avatar?: string
  articleCount?: number
  likeCount?: number
  followerCount?: number
  followingCount?: number
}>()
</script>
