import DOMPurify from 'dompurify'
import { marked } from 'marked'

// 文章里的外链统一加 noopener，避免用户点击后新窗口能操作原页面
DOMPurify.addHook('afterSanitizeAttributes', (node) => {
  if (node.tagName === 'A') {
    node.setAttribute('target', '_blank')
    node.setAttribute('rel', 'noopener noreferrer')
  }
})

/**
 * Markdown → 安全的 HTML。
 * 文章正文通过 v-html 渲染，必须先经 DOMPurify 消毒，防止存储型 XSS。
 */
export function renderMarkdown(content: string): string {
  if (!content) return ''
  try {
    const html = marked.parse(content) as string
    return DOMPurify.sanitize(html, { USE_PROFILES: { html: true } })
  } catch {
    return ''
  }
}

/** 编辑器预览：空内容显示占位提示。 */
export function renderMarkdownPreview(content: string): string {
  if (!content) {
    return '<span style="color:var(--text-muted)">预览区域...</span>'
  }
  return renderMarkdown(content)
}
