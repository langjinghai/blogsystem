<template>
  <div class="app-page-container app-publish-page">
    <div class="app-section-header">
      <h3 class="app-section-header__title">编辑文章</h3>
    </div>

    <div v-loading="loading" class="app-card app-publish-page__form">
      <!-- 标题 -->
      <el-form-item label="文章标题" class="app-form-item">
        <el-input
          v-model="form.title"
          placeholder="请输入文章标题"
          maxlength="100"
          show-word-limit
          class="app-input"
        />
      </el-form-item>

      <!-- 封面 & 摘要 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="文章封面" class="app-form-item">
            <div v-if="form.coverImage" style="margin-bottom:10px">
              <img :src="form.coverImage" style="width:100%;max-height:160px;object-fit:cover;border-radius:8px" alt="cover" />
            </div>
            <el-upload
              class="app-upload"
              drag
              :auto-upload="false"
              :limit="1"
              :on-change="handleCoverChange"
              accept="image/*"
            >
              <div style="padding:20px;text-align:center">
                <div style="font-size:32px;margin-bottom:8px">🖼️</div>
                <div style="font-size:13px;color:var(--text-muted)">重新上传封面（可选）</div>
              </div>
            </el-upload>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="文章分类" class="app-form-item">
            <el-select
              v-model="form.categoryId"
              placeholder="请选择分类"
              class="app-input"
              style="width:100%"
            >
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="文章摘要" class="app-form-item">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="4"
              placeholder="请输入文章摘要"
              maxlength="200"
              show-word-limit
              class="app-input"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- Markdown 编辑器 -->
      <div class="app-publish-page__editor">
        <div class="app-publish-page__editor-pane">
          <span class="pane-label">📝 Markdown 编辑</span>
          <textarea v-model="form.content" placeholder="支持 Markdown 语法"></textarea>
        </div>
        <div class="app-publish-page__editor-pane">
          <span class="pane-label">👁 实时预览</span>
          <div class="app-publish-page__preview" v-html="previewHtml"></div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div style="display:flex;gap:16px;justify-content:flex-end">
        <el-button class="app-btn--outline" @click="$router.back()">取消</el-button>
        <el-button
          class="app-btn--primary"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="handleSave"
        >
          保存修改
        </el-button>
      </div>
    </div>

    <div v-if="!loading && !articleId" class="app-empty-state">
      <div class="app-empty-state__icon">❌</div>
      <div class="app-empty-state__text">文章不存在</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { renderMarkdownPreview } from '@/utils/markdown'
import { getCategoryList, type Category } from '@/api/category'
import { getArticleById, updateArticle, uploadImage, type ArticleForm } from '@/api/article'
import type { UploadFile } from 'element-plus'

const route = useRoute()
const router = useRouter()

const articleId = computed(() => Number(route.params.id))
const categories = ref<Category[]>([])
const loading = ref(true)
const submitting = ref(false)

const form = reactive<ArticleForm>({
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: undefined as unknown as number,
})

const canSubmit = computed(() => {
  return form.title.trim().length > 0 && form.content.trim().length > 0 && form.categoryId > 0
})

const previewHtml = computed(() => renderMarkdownPreview(form.content))

onMounted(async () => {
  try {
    const [catRes, articleRes] = await Promise.all([
      getCategoryList(),
      getArticleById(articleId.value),
    ])
    if (catRes.success) categories.value = (catRes.data as Category[]) || []
    if (articleRes.success && articleRes.data) {
      const a = articleRes.data as any
      form.title = a.title || ''
      form.summary = a.summary || ''
      form.content = a.content || ''
      form.coverImage = a.coverImage || ''
      form.categoryId = a.categoryId
    }
  } catch {
    ElMessage.error('网络异常，文章加载失败')
  }
  loading.value = false
})

async function handleCoverChange(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  try {
    const res = await uploadImage(raw)
    if (res.success && res.data) {
      form.coverImage = res.data as string
      ElMessage.success('新封面上传成功')
    } else {
      ElMessage.warning(res.errorMag || '封面上传失败')
    }
  } catch {
    ElMessage.warning('封面上传失败')
  }
}

async function handleSave() {
  if (!canSubmit.value) {
    ElMessage.warning('请填写标题、内容和分类')
    return
  }
  submitting.value = true
  try {
    const res = await updateArticle({
      id: articleId.value,
      title: form.title.trim(),
      summary: form.summary.trim(),
      content: form.content.trim(),
      coverImage: form.coverImage,
      categoryId: form.categoryId,
    })
    if (res.success) {
      ElMessage.success('文章更新成功！')
      router.push(`/post/${articleId.value}`)
    } else {
      ElMessage.error(res.errorMag || '更新失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || '网络异常')
  } finally {
    submitting.value = false
  }
}
</script>
