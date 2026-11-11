<template>
  <div class="app-page-container app-publish-page">
    <div class="app-section-header">
      <h3 class="app-section-header__title">发布文章</h3>
    </div>

    <div class="app-card app-publish-page__form">
      <!-- 标题 -->
      <el-form-item label="文章标题" class="app-form-item">
        <el-input
          v-model="form.title"
          placeholder="请输入文章标题（1~100字符）"
          maxlength="100"
          show-word-limit
          class="app-input"
        />
      </el-form-item>

      <!-- 封面 & 摘要 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="文章封面" class="app-form-item">
            <el-upload
              class="app-upload"
              drag
              :auto-upload="false"
              :limit="1"
              :on-change="handleCoverChange"
              :file-list="coverFiles"
              accept="image/*"
            >
              <div style="padding:20px;text-align:center">
                <div style="font-size:32px;margin-bottom:8px">🖼️</div>
                <div style="font-size:13px;color:var(--text-muted)">拖拽或点击上传封面图</div>
              </div>
            </el-upload>
            <div v-if="coverPreview" style="margin-top:10px">
              <img :src="coverPreview" style="width:100%;max-height:160px;object-fit:cover;border-radius:8px" alt="preview" />
            </div>
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
              placeholder="请输入文章摘要（会在列表页展示）"
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
          <textarea
            v-model="form.content"
            placeholder="# 开始书写你的文章...
支持 Markdown 语法"
          ></textarea>
        </div>
        <div class="app-publish-page__editor-pane">
          <span class="pane-label">👁 实时预览</span>
          <div class="app-publish-page__preview" v-html="previewHtml"></div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div style="display:flex;gap:16px;justify-content:flex-end">
        <el-button class="app-btn--outline" @click="$router.back()">取消</el-button>
        <el-button class="app-btn--primary" :loading="submitting" :disabled="!canSubmit" @click="handlePublish">
          发布文章
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { getCategoryList, type Category } from '@/api/category'
import { createArticle, uploadImage, type ArticleForm } from '@/api/article'
import type { UploadFile } from 'element-plus'

const router = useRouter()

const categories = ref<Category[]>([])
const coverFiles = ref<UploadFile[]>([])
const coverPreview = ref('')
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

const previewHtml = computed(() => {
  if (!form.content) return '<span style="color:var(--text-muted)">预览区域...</span>'
  return marked(form.content) as string
})

onMounted(async () => {
  try {
    const res = await getCategoryList()
    if (res.success) {
      categories.value = (res.data as Category[]) || []
    }
  } catch {
    ElMessage.error('网络异常，分类加载失败')
  }
})

onUnmounted(() => {
  if (coverPreview.value) URL.revokeObjectURL(coverPreview.value)
})

async function handleCoverChange(file: UploadFile) {
  coverFiles.value = [file]
  const raw = file.raw
  if (raw) {
    const oldUrl = coverPreview.value
    coverPreview.value = URL.createObjectURL(raw)
    if (oldUrl) URL.revokeObjectURL(oldUrl)
    try {
      const res = await uploadImage(raw)
      if (res.success && res.data) {
        form.coverImage = res.data as string
      } else {
        ElMessage.warning(res.errorMag || '封面上传失败')
      }
    } catch {
      ElMessage.warning('封面上传失败，您仍可发布文章')
    }
  }
}

async function handlePublish() {
  if (!canSubmit.value) {
    ElMessage.warning('请填写标题、内容和分类')
    return
  }
  submitting.value = true
  try {
    const res = await createArticle({
      title: form.title.trim(),
      summary: form.summary.trim(),
      content: form.content.trim(),
      coverImage: form.coverImage,
      categoryId: form.categoryId,
    })
    if (res.success) {
      ElMessage.success('文章发布成功！')
      const id = res.data as number
      router.push(`/post/${id}`)
    } else {
      ElMessage.error(res.errorMag || '发布失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || '网络异常')
  } finally {
    submitting.value = false
  }
}
</script>
