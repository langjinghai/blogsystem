/**
 * ============================================
 *  AIOT Nexus - 路由配置
 * ============================================
 */
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login',
  },

  // ---------- 认证页面（无导航栏） ----------
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录 - AIOT Nexus' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册 - AIOT Nexus' },
  },
  {
    path: '/forget',
    name: 'ForgetPassword',
    component: () => import('@/views/ForgetPassword.vue'),
    meta: { title: '重置密码 - AIOT Nexus' },
  },

  // ---------- 主应用页面（带导航栏） ----------
  {
    path: '/home',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页 - AIOT Nexus' },
      },
    ],
  },
  {
    path: '/article',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'ArticleList',
        component: () => import('@/views/ArticleList.vue'),
        meta: { title: '文章列表 - AIOT Nexus' },
      },
      {
        path: 'publish',
        name: 'PublishArticle',
        component: () => import('@/views/PublishArticle.vue'),
        meta: { title: '发布文章 - AIOT Nexus' },
      },
      {
        path: 'edit/:id',
        name: 'EditArticle',
        component: () => import('@/views/EditArticle.vue'),
        meta: { title: '编辑文章 - AIOT Nexus' },
      },
    ],
  },
  {
    path: '/post/:id',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'ArticleDetail',
        component: () => import('@/views/ArticleDetail.vue'),
        meta: { title: '文章详情 - AIOT Nexus' },
      },
    ],
  },
  {
    path: '/my/article',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'MyArticle',
        component: () => import('@/views/MyArticle.vue'),
        meta: { title: '我的文章 - AIOT Nexus' },
      },
    ],
  },
  {
    path: '/profile',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心 - AIOT Nexus' },
      },
    ],
  },
  {
    path: '/user/:id',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue'),
        meta: { title: '用户主页 - AIOT Nexus' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const title = (to.meta.title as string) || 'AIOT Nexus'
  document.title = title

  // 认证守卫
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('auth_token')
    if (!token) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }

  // 已登录用户访问认证页则重定向到首页
  if (
    (to.path === '/login' || to.path === '/register' || to.path === '/forget') &&
    localStorage.getItem('auth_token')
  ) {
    return next('/home')
  }

  next()
})

export default router
