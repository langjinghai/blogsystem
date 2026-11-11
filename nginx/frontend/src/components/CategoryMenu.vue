<template>
  <div class="app-category-menu">
    <router-link
      v-for="cat in categories"
      :key="cat.id"
      :to="`/article/list?categoryId=${cat.id}`"
      class="app-category-menu__item"
      :class="{ active: activeId === cat.id }"
    >
      <span class="cat-name">
        <span class="cat-icon">{{ cat.icon || '📂' }}</span>
        {{ cat.name }}
      </span>
      <span v-if="cat.articleCount != null" class="cat-count">{{ cat.articleCount }}</span>
    </router-link>

    <div v-if="categories.length === 0" class="app-empty-state">
      <div class="app-empty-state__icon">📭</div>
      <div class="app-empty-state__text">暂无分类</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Category } from '@/api/category'

defineProps<{
  categories: Category[]
  activeId?: number
}>()
</script>
