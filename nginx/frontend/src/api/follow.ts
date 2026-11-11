import http from '@/utils/request'
import type { ApiResult } from '@/utils/request'

export type { ApiResult }

/** 粉丝/关注列表中的用户简略信息 */
export interface FollowUser {
  id: number
  phone: string
  nickName: string
  icon: string
  articleCount?: number
  likeCount?: number
  followerCount?: number
  followingCount?: number
  isFollowed?: boolean
}

/** POST /follow/{followeeId} — 关注用户 */
export async function follow(followeeId: number): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>(`/follow/${followeeId}`)
  return data
}

/** DELETE /follow/{followeeId} — 取关用户 */
export async function unfollow(followeeId: number): Promise<ApiResult> {
  const { data } = await http.delete<ApiResult>(`/follow/${followeeId}`)
  return data
}

/** GET /follow/status/{followeeId} — 检查是否已关注 */
export async function isFollowing(followeeId: number): Promise<ApiResult<boolean>> {
  const { data } = await http.get<ApiResult<boolean>>(`/follow/status/${followeeId}`)
  return data
}

/** GET /follow/{userId}/followers?page=&size= — 粉丝列表 */
export async function getFollowers(
  userId: number,
  page = 1,
  size = 10,
): Promise<ApiResult<FollowUser[]>> {
  const { data } = await http.get<ApiResult<FollowUser[]>>(`/follow/${userId}/followers`, {
    params: { page, size },
  })
  return data
}

/** GET /follow/{userId}/following?page=&size= — 关注列表 */
export async function getFollowing(
  userId: number,
  page = 1,
  size = 10,
): Promise<ApiResult<FollowUser[]>> {
  const { data } = await http.get<ApiResult<FollowUser[]>>(`/follow/${userId}/following`, {
    params: { page, size },
  })
  return data
}
