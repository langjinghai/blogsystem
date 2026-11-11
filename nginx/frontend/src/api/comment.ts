import http from '@/utils/request'
import type { ApiResult } from '@/utils/request'

export interface Comment {
  id: number
  articleId: number
  content: string
  authorId: number
  authorName: string
  authorAvatar?: string
  parentId?: number
  createTime: string
}

export interface CommentPageQuery {
  articleId: number
  page: number
  size: number
}

export interface CommentPageResult {
  records: Comment[]
  total: number
  size: number
  current: number
  pages: number
}

export async function getCommentsByArticle(
  query: CommentPageQuery,
): Promise<ApiResult<CommentPageResult>> {
  const { data } = await http.get<ApiResult<CommentPageResult>>(
    `/comment/article/${query.articleId}`,
    { params: { page: query.page, size: query.size } },
  )
  return data
}

export async function addComment(body: {
  articleId: number
  content: string
  parentId?: number
}): Promise<ApiResult<number>> {
  const { data } = await http.post<ApiResult<number>>('/comment', body)
  return data
}

export async function deleteComment(id: number): Promise<ApiResult> {
  const { data } = await http.delete<ApiResult>(`/comment/${id}`)
  return data
}
