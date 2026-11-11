<template>
  <div>
    <div class="app-section-header">
      <h3 class="app-section-header__title">我的文章</h3>
    </div>

    <div class="app-card" v-loading="loading">
      <div v-if="articles.length === 0 && !loading" class="app-empty-state">
        <div class="app-empty-state__icon">📝</div>
        <div class="app-empty-state__text">你还没有发布任何文章</div>
        <el-button class="app-btn--primary" style="margin-top:16px" @click="$router.push('/article/publish')">
          去发布第一篇
        </el-button>
      </div>

      <div v-for="article in articles" :key="article.id" class="app-article-card-compact" style="display:flex;align-items:center;gap:16px">
        <div style="flex:1;cursor:pointer" @click="$router.push(`/post/${article.id}`)">
          <div class="app-article-card-compact__title">{{ article.title }}</div>
          <div class="app-article-card-compact__meta">
            <span>👁 {{ article.viewCount }}</span>
            <span>❤ {{ article.likeCount }}</span>
            <span>💬 {{ article.commentCount }}</span>
            <span>{{ formatTime(article.createTime) }}</span>
          </div>
        </div>
        <div style="display:flex;gap:8px;flex-shrink:0">
          <el-button class="app-btn--outline" size="small" @click="$router.push(`/article/edit/${article.id}`)">
            编辑
          </el-button>
          <el-button class="app-btn--danger" size="small" @click="handleDelete(article.id)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="app-pagination">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="page"
        @current-change="loadArticles"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyArticles, deleteArticle, type Article } from '@/api/article'

const userStore = useUserStore()

const articles = ref<Article[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)

onMounted(async () => {
  await userStore.loadUserInfo()
  loadArticles()
})

async function loadArticles() {
  loading.value = true
  try {
    const res = await getMyArticles({ page: page.value, size: pageSize })
    if (res.success && res.data) {
      articles.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    const res = await deleteArticle(id)
    if (res.success) {
      ElMessage.success('已删除')
      loadArticles()
    } else {
      ElMessage.error(res.errorMag || '删除失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  }
}

function formatTime(t: string): string {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}
</script>
