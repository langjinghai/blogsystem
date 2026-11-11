/**
 * ============================================
 *  AIOT Nexus — Axios 实例 + 拦截器 + 通用类型
 *  全局共享，供 api/*.ts 使用
 * ============================================
 */
import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'

// ==================== 通用类型 ====================

/** 后端统一响应结构（与 Spring Boot Result 类对齐） */
export interface ApiResult<T = any> {
  success: boolean
  errorMag: string | null
  data: T
  total: number | null
}

// ==================== Axios 实例 ====================

const http: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 15000,
  // 不设置默认 Content-Type——axios 对 JSON 数据会自动设 application/json，
  // 对 FormData 则让浏览器自动生成 multipart/form-data 带 boundary。
})

// ==================== 请求拦截器 ====================

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ==================== 响应拦截器 ====================

http.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    return response
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        localStorage.removeItem('auth_token')
        // 只有不在登录页时才跳转，避免无限循环
        const path = window.location.pathname
        if (path !== '/login' && path !== '/register' && path !== '/forget') {
          window.location.replace('/login')
        }
      }
    }
    return Promise.reject(error)
  },
)

export default http
