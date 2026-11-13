/**
 * ============================================
 *  AIOT Nexus - 用户认证 API 层
 *  严格对接 Spring Boot 后端接口
 * ============================================
 */
import http from '@/utils/request'
import type { ApiResult } from '@/utils/request'

// ==================== 类型定义 ====================

export type { ApiResult }

export type CodeScene = 'login' | 'register' | 'forget'

export interface LoginParams {
  phone?: string
  email?: string
  password?: string
  code?: string
}

export interface RegisterParams {
  phone?: string
  email?: string
  code: string
  password: string
  confirmPassword: string
}

export interface ForgetParams {
  phone?: string
  email?: string
  code: string
  newPassword: string
  confirmPassword: string
}

export interface EmailLoginParams {
  email: string
  code?: string
  password?: string
}

export interface EmailRegisterParams {
  email: string
  code: string
  password: string
  confirmPassword: string
}

export interface EmailForgetParams {
  email: string
  code: string
  newPassword: string
  confirmPassword: string
}

export interface UpdatePasswordParams {
  password: string
  newPassword: string
  confirmPassword: string
}

export interface UserInfo {
  id: number
  phone?: string
  email?: string
  nickName: string
  icon: string
  articleCount?: number
  likeCount?: number
  followerCount?: number
  followingCount?: number
  isFollowed?: boolean
  createTime?: string
}

export interface UpdateProfileParams {
  nickName?: string
  icon?: string
}

// ==================== API 方法 ====================

/** POST /user/code?phone=13800138000&scene=login|register|forget */
export async function sendCode(phone: string, scene: CodeScene = 'login'): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/code', null, { params: { phone, scene } })
  return data
}

/** POST /user/email/code?email=xxx@qq.com&scene=login|register|forget */
export async function sendEmailCode(email: string, scene: CodeScene = 'login'): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/email/code', null, { params: { email, scene } })
  return data
}

/** POST /user/login — 支持 { phone, code } 或 { phone, password } */
export async function login(params: LoginParams): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/login', params)
  return data
}

/** POST /user/email/login — 支持 { email, code } 或 { email, password } */
export async function emailLogin(params: EmailLoginParams): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/email/login', params)
  return data
}

/** POST /user/register */
export async function register(params: RegisterParams): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/register', params)
  return data
}

/** POST /user/email/register */
export async function emailRegister(params: EmailRegisterParams): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/email/register', params)
  return data
}

/** POST /user/forget */
export async function forgetPassword(params: ForgetParams): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/forget', params)
  return data
}

/** POST /user/email/forget */
export async function emailForgetPassword(params: EmailForgetParams): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/email/forget', params)
  return data
}

/** POST /user/updatePassword */
export async function updatePassword(params: UpdatePasswordParams): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/updatePassword', params)
  return data
}

/** POST /user/logout */
export async function logout(): Promise<ApiResult> {
  const { data } = await http.post<ApiResult>('/user/logout')
  return data
}

/** GET /user/info */
export async function getUserInfo(): Promise<ApiResult<UserInfo>> {
  const { data } = await http.get<ApiResult<UserInfo>>('/user/info')
  return data
}

/** GET /user/{id} — 获取他人主页信息 */
export async function getUserProfileById(id: number): Promise<ApiResult<UserInfo>> {
  const { data } = await http.get<ApiResult<UserInfo>>(`/user/${id}`)
  return data
}

/** PUT /user/profile */
export async function updateProfile(params: UpdateProfileParams): Promise<ApiResult> {
  const { data } = await http.put<ApiResult>('/user/profile', params)
  return data
}

