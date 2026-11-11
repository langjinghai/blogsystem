<template>
  <div class="app-comment-section">
    <!-- 评论输入 -->
    <div v-if="showInput" class="app-comment-section__input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
        class="app-input"
        @keyup.enter.ctrl="submitComment"
      />
      <div style="display:flex;justify-content:flex-end;margin-top:10px">
        <el-button class="app-btn--primary" :loading="submitting" :disabled="!inputText.trim()" @click="submitComment">
          发送评论
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="app-comment-section__list">
      <div v-if="comments.length === 0" class="app-empty-state">
        <div class="app-empty-state__icon">💬</div>
        <div class="app-empty-state__text">暂无评论，来写第一条吧</div>
      </div>

      <div v-for="comment in comments" :key="comment.id" class="app-comment-item">
        <img
          v-if="comment.authorAvatar"
          :src="comment.authorAvatar"
          class="app-comment-item__avatar"
          alt="avatar"
        />
        <div v-else class="app-comment-item__avatar-placeholder">
          {{ comment.authorName?.charAt(0) || '?' }}
        </div>

        <div class="app-comment-item__body">
          <div class="app-comment-item__header">
            <span class="app-comment-item__author">{{ comment.authorName }}</span>
            <span class="app-comment-item__time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <div class="app-comment-item__content">{{ comment.content }}</div>
        </div>

        <button
          v-if="canDelete(comment.authorId)"
          class="app-comment-item__delete"
          @click="handleDelete(comment.id)"
        >
          删除
        </button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="app-pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="$emit('page-change', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Comment } from '@/api/comment'
import { addComment, deleteComment } from '@/api/comment'

const props = defineProps<{
  articleId: number
  comments: Comment[]
  total: number
  pageSize: number
  currentPage: number
  showInput?: boolean
  currentUserId?: number
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
  (e: 'page-change', page: number): void
  (e: 'reset-page'): void
}>()

const inputText = ref('')
const submitting = ref(false)

function canDelete(authorId: number): boolean {
  return props.currentUserId != null && props.currentUserId === authorId
}

async function submitComment() {
  const text = inputText.value.trim()
  if (!text) return
  submitting.value = true
  try {
    const res = await addComment({ articleId: props.articleId, content: text })
    if (res.success) {
      ElMessage.success('评论成功')
      inputText.value = ''
      emit('reset-page')
      emit('refresh')
    } else {
      ElMessage.error(res.errorMag || '评论失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || '网络异常')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    const res = await deleteComment(id)
    if (res.success) {
      ElMessage.success('已删除')
      emit('refresh')
    } else {
      ElMessage.error(res.errorMag || '删除失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  }
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
  return d.toLocaleDateString('zh-CN')
}
</script>
