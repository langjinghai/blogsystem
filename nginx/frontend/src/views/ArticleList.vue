<template>
  <div>
    <!-- ====== 搜索和筛选栏 ====== -->
    <div class="app-card" style="margin-bottom:20px;padding:16px 20px;display:flex;gap:16px;align-items:center;flex-wrap:wrap">
      <SearchBar
        :model-value="query.keyword || ''"
        placeholder="搜索文章标题..."
        style="flex:1;min-width:200px"
        @search="handleSearch"
      />
      <el-select
        v-model="query.categoryId"
        clearable
        placeholder="按分类"
        class="app-input app-select-popper"
        style="width:160px"
        @change="loadArticles"
      >
        <el-option label="全部分类" :value="undefined" />
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
    </div>

    <!-- 文章网格 -->
    <el-row :gutter="20">
      <el-col v-for="article in articles" :key="article.id" :xs="24" :sm="12" :lg="8" style="margin-bottom:20px">
        <ArticleCard :article="article" />
      </el-col>
    </el-row>

    <div v-if="articles.length === 0 && !loading" class="app-empty-state">
      <div class="app-empty-state__icon">🔍</div>
      <div class="app-empty-state__text">没有找到匹配的文章</div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="app-pagination">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.size"
        v-model:current-page="query.page"
        @current-change="loadArticles"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticlePage, type Article, type ArticlePageQuery } from '@/api/article'
import { getCategoryList, type Category } from '@/api/category'
import ArticleCard from '@/components/ArticleCard.vue'
import SearchBar from '@/components/SearchBar.vue'

const route = useRoute()
const router = useRouter()

const articles = ref<Article[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const loading = ref(false)

const query = reactive<ArticlePageQuery>({
  page: 1,
  size: 9,
  keyword: (route.query.keyword as string) || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
})

onMounted(async () => {
  try {
    const catRes = await getCategoryList()
    if (catRes.success) {
      categories.value = (catRes.data as Category[]) || []
    }
  } catch {
    // silently fail
  }
  loadArticles()
})

watch(
  () => route.query,
  (newQuery) => {
    const keyword = (newQuery.keyword as string) || ''
    const categoryId = newQuery.categoryId ? Number(newQuery.categoryId) : undefined
    if (query.keyword !== keyword || query.categoryId !== categoryId) {
      query.keyword = keyword
      query.categoryId = categoryId
      query.page = 1
      loadArticles()
    }
  },
)

async function loadArticles() {
  loading.value = true
  try {
    const q: Record<string, string> = {}
    if (query.keyword) q.keyword = query.keyword
    if (query.categoryId) q.categoryId = String(query.categoryId)
    router.replace({ query: q })

    const res = await getArticlePage({ ...query })
    if (res.success && res.data) {
      articles.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    // silently fail
  } finally {
    loading.value = false
  }
}

function handleSearch(keyword: string) {
  query.keyword = keyword
  query.page = 1
  loadArticles()
}
</script>
