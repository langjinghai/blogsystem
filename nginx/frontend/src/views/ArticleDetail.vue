<template>
  <div class="app-page-container">
    <div class="app-article-detail" v-loading="loading">
      <!-- 标题 & 元信息 -->
      <div class="app-article-detail__header">
        <h1 class="app-article-detail__title">{{ article?.title }}</h1>
        <div class="app-article-detail__meta">
          <span class="meta-author">
            <img v-if="article?.authorAvatar" :src="article.authorAvatar" alt="avatar" />
            <span v-else class="avatar-placeholder">{{ article?.authorName?.charAt(0) || '?' }}</span>
            <router-link :to="`/user/${article?.authorId}`" class="author-name-link">
              {{ article?.authorName }}
            </router-link>
            <FollowButton
              v-if="article && !isOwner"
              :followee-id="article.authorId"
              :initial-followed="isFollowingAuthor"
              @update:followed="onFollowChange"
            />
          </span>
          <span>🕐 {{ formatTime(article?.createTime) }}</span>
          <span>👁 {{ article?.viewCount || 0 }} 阅读</span>
          <span>❤ {{ article?.likeCount || 0 }} 点赞</span>
          <span>💬 {{ article?.commentCount || 0 }} 评论</span>
        </div>
      </div>

      <!-- 封面 -->
      <img
        v-if="article?.coverImage"
        :src="article.coverImage"
        class="app-article-detail__cover"
        alt="cover"
      />

      <!-- 正文 -->
      <div class="app-article-detail__content" v-html="renderedContent"></div>

      <!-- 操作按钮 -->
      <div class="app-article-detail__actions">
        <el-button
          :class="liked ? 'app-btn--primary' : 'app-btn--outline'"
          :loading="likeLoading"
          @click="toggleLikeAction"
        >
          {{ liked ? '❤ 已点赞' : '🤍 点赞' }} {{ article?.likeCount || 0 }}
        </el-button>
        <el-button class="app-btn--outline" @click="scrollToComments">
          💬 评论 {{ article?.commentCount || 0 }}
        </el-button>
        <el-button
          v-if="isOwner"
          class="app-btn--outline"
          @click="$router.push(`/article/edit/${article?.id}`)"
        >
          ✏️ 编辑
        </el-button>
        <el-button
          v-if="isOwner"
          class="app-btn--danger"
          @click="handleDeleteArticle"
        >
          🗑 删除
        </el-button>
      </div>

      <!-- 评论区 -->
      <CommentList
        :article-id="articleId"
        :comments="comments"
        :total="commentTotal"
        :page-size="commentPageSize"
        :current-page="commentPage"
        :current-user-id="currentUserId"
        show-input
        @refresh="loadComments"
        @reset-page="commentPage = 1"
        @page-change="onCommentPageChange"
      />
    </div>

    <div v-if="!loading && !article" class="app-empty-state">
      <div class="app-empty-state__icon">📄</div>
      <div class="app-empty-state__text">文章不存在或已被删除</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { useUserStore } from '@/stores/user'
import { getArticleById, toggleLike, deleteArticle, addView, type Article } from '@/api/article'
import { getCommentsByArticle, type Comment } from '@/api/comment'
import { isFollowing } from '@/api/follow'
import CommentList from '@/components/CommentList.vue'
import FollowButton from '@/components/FollowButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const articleId = computed(() => Number(route.params.id))
const article = ref<Article | null>(null)
const loading = ref(true)
const liked = ref(false)
const likeLoading = ref(false)
const isFollowingAuthor = ref(false)

const comments = ref<Comment[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = 20

const currentUserId = computed(() => userStore.userInfo?.id)

const isOwner = computed(() => {
  if (!article.value || !userStore.userInfo) return false
  return article.value.authorId === userStore.userInfo.id
})

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  try {
    return marked(article.value.content) as string
  } catch {
    return '<p style="color:var(--color-error)">内容渲染失败</p>'
  }
})

onMounted(async () => {
  await loadArticle()
  await loadComments()
  // 文章加载成功后增加浏览量（不阻塞页面渲染）
  if (article.value) {
    addView(articleId.value).catch(() => {})
  }
})

async function loadArticle() {
  loading.value = true
  try {
    const res = await getArticleById(articleId.value)
    if (res.success && res.data) {
      article.value = res.data as Article
      liked.value = article.value.isLiked || false
      // 加载关注状态（非作者本人时）
      if (article.value.authorId !== userStore.userInfo?.id) {
        loadFollowStatus()
      }
    } else {
      article.value = null
    }
  } catch {
    article.value = null
    ElMessage.error('文章加载失败')
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const res = await getCommentsByArticle({
      articleId: articleId.value,
      page: commentPage.value,
      size: commentPageSize,
    })
    if (res.success && res.data) {
      comments.value = res.data.records || []
      commentTotal.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('评论加载失败')
  }
}

async function onCommentPageChange(page: number) {
  commentPage.value = page
  await loadComments()
  scrollToComments()
}

async function toggleLikeAction() {
  likeLoading.value = true
  try {
    const res = await toggleLike(articleId.value)
    if (res.success) {
      await loadArticle()
    } else {
      ElMessage.error(res.errorMag || '操作失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    likeLoading.value = false
  }
}

async function handleDeleteArticle() {
  try {
    await ElMessageBox.confirm('确定要永久删除这篇文章吗？此操作不可撤销。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
    })
  } catch {
    return
  }
  try {
    const res = await deleteArticle(articleId.value)
    if (res.success) {
      ElMessage.success('文章已删除')
      router.push('/my/article')
    } else {
      ElMessage.error(res.errorMag || '删除失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  }
}

async function loadFollowStatus() {
  if (!article.value) return
  try {
    const res = await isFollowing(article.value.authorId)
    if (res.success) {
      isFollowingAuthor.value = res.data as boolean
    }
  } catch { /* ignore */ }
}

function onFollowChange(followed: boolean) {
  isFollowingAuthor.value = followed
}

function scrollToComments() {
  const el = document.querySelector('.app-comment-section')
  el?.scrollIntoView({ behavior: 'smooth' })
}

function formatTime(t?: string): string {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.author-name-link {
  color: var(--color-primary);
  text-decoration: none;
  cursor: pointer;
  &:hover {
    text-decoration: underline;
  }
}
.meta-author {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
