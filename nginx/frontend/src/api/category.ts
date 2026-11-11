import http from '@/utils/request'
import type { ApiResult } from '@/utils/request'

export interface Category {
  id: number
  name: string
  description?: string
  icon?: string
  articleCount?: number
  sortOrder?: number
}

export async function getCategoryList(): Promise<ApiResult<Category[]>> {
  const { data } = await http.get<ApiResult<Category[]>>('/category/list')
  return data
}
