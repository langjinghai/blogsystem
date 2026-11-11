import http from '@/utils/request'
import type { ApiResult } from '@/utils/request'

export interface Article {
  id: number
  title: string
  summary: string
  content: string
  coverImage: string
  categoryId: number
  categoryName?: string
  authorId: number
  authorName: string
  authorAvatar?: string
  viewCount: number
  likeCount: number
  commentCount: number
  isLiked?: boolean
  createTime: string
  updateTime: string
}

export interface ArticlePageQuery {
  page: number
  size: number
  keyword?: string
  categoryId?: number
}

export interface ArticlePageResult {
  records: Article[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ArticleForm {
  title: string
  summary: string
  content: string
  coverImage: string
  categoryId: number
}

// ---------- articles ----------

export async function getArticlePage(query: ArticlePageQuery): Promise<ApiResult<ArticlePageResult>> {
  const { data } = await http.get<ApiResult<ArticlePageResult>>('/article/page', { params: query })
  return data
}

export async function getArticleById(id: number): Promise<ApiResult<Article>> {
  const { data } = await http.get<ApiResult<Article>>(`/article/${id}`)
  return data
}

export async function getHotArticles(limit = 5): Promise<ApiResult<Article[]>> {
  const { data } = await http.get<ApiResult<Article[]>>('/article/hot', { params: { limit } })
  return data
}

export async function getLatestArticles(limit = 5): Promise<ApiResult<Article[]>> {
  const { data } = await http.get<ApiResult<Article[]>>('/article/latest', { params: { limit } })
  return data
}

export async function getMyArticles(query: ArticlePageQuery): Promise<ApiResult<ArticlePageResult>> {
  const { data } = await http.get<ApiResult<ArticlePageResult>>('/article/my', { params: query })
  return data
}

/** GET /article/user/{userId}?page=&size= — 指定用户的文章 */
export async function getUserArticles(
  userId: number,
  page = 1,
  size = 10,
): Promise<ApiResult<ArticlePageResult>> {
  const { data } = await http.get<ApiResult<ArticlePageResult>>(`/article/user/${userId}`, {
    params: { page, size },
  })
  return data
}

export async function createArticle(form: ArticleForm): Promise<ApiResult<number>> {
  const { data } = await http.post<ApiResult<number>>('/article', form)
  return data
}

export async function updateArticle(form: ArticleForm & { id: number }): Promise<ApiResult> {
  const { data } = await http.put<ApiResult>('/article', form)
  return data
}

export async function deleteArticle(id: number): Promise<ApiResult> {
  const { data } = await http.delete<ApiResult>(`/article/${id}`)
  return data
}

// ---------- like ----------

/** PUT /article/{id}/view — 增加浏览量 */
export async function addView(articleId: number): Promise<ApiResult> {
  const { data } = await http.put<ApiResult>(`/article/${articleId}/view`)
  return data
}

/** PUT /article/{id}/like — 切换点赞，无返回数据，前端刷新详情 */
export async function toggleLike(articleId: number): Promise<ApiResult> {
  const { data } = await http.put<ApiResult>(`/article/${articleId}/like`)
  return data
}

// ---------- upload ----------

export async function uploadImage(file: File): Promise<ApiResult<string>> {
  const fd = new FormData()
  fd.append('file', file)
  // 不手动设置 Content-Type，让浏览器自动附加 boundary
  const { data } = await http.post<ApiResult<string>>('/upload/image', fd)
  return data
}
