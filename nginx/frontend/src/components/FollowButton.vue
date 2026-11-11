<template>
  <el-button
    class="follow-btn"
    :class="isFollowed ? 'follow-btn--active' : 'follow-btn--default'"
    :loading="loading"
    size="small"
    @click.stop="handleToggle"
    @mouseenter="hovering = true"
    @mouseleave="hovering = false"
  >
    <template v-if="isFollowed">
      {{ hovering ? '取关' : '已关注' }}
    </template>
    <template v-else>
      + 关注
    </template>
  </el-button>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { follow, unfollow } from '@/api/follow'

const props = defineProps<{
  followeeId: number
  initialFollowed: boolean
}>()

const emit = defineEmits<{
  'update:followed': [value: boolean]
}>()

const isFollowed = ref(props.initialFollowed)
const loading = ref(false)
const hovering = ref(false)

// 当父组件异步获取到关注状态后，同步更新内部状态
watch(() => props.initialFollowed, (val) => {
  isFollowed.value = val
})

async function handleToggle() {
  loading.value = true
  try {
    if (isFollowed.value) {
      const res = await unfollow(props.followeeId)
      if (res.success) {
        isFollowed.value = false
        emit('update:followed', false)
        ElMessage.success('已取关')
      } else {
        ElMessage.error(res.errorMag || '取关失败')
      }
    } else {
      const res = await follow(props.followeeId)
      if (res.success) {
        isFollowed.value = true
        emit('update:followed', true)
        ElMessage.success('关注成功')
      } else {
        ElMessage.error(res.errorMag || '关注失败')
      }
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || '网络异常')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.follow-btn {
  font-weight: 600;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  min-width: 70px;

  &.follow-btn--default {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: #fff;
    &:hover {
      background: var(--color-primary-light);
      border-color: var(--color-primary-light);
    }
  }

  &.follow-btn--active {
    background: transparent;
    border-color: var(--border-glow);
    color: var(--text-secondary);
    &:hover {
      background: var(--color-error);
      border-color: var(--color-error);
      color: #fff;
    }
  }
}
</style>
