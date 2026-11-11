/**
 * ============================================
 *  AIOT Nexus — 前端入口
 *  Vue3 + TypeScript + Element Plus + Axios
 * ============================================
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'

// 主题 token（CSS 变量）
import './styles/tokens.css'
import './styles/base.scss'
import './styles/auth.scss'
import './styles/layout.scss'
import './styles/components.scss'
import './styles/article.scss'
import './styles/markdown.scss'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')

console.log(
  '%c🤖 AIOT Nexus %cFrontend %cActivated %c▮',
  'font-size:16px;color:#2563eb;',
  'color:#64748b;',
  'color:#6366f1;',
  'color:#10b981;',
)
console.log('%c双主题博客系统已就绪，指挥官。', 'color:#64748b;font-size:12px;')
