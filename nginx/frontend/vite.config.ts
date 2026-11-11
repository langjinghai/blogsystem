import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/user': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/article': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/comment': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/category': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/like': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/upload': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router'],
        },
      },
    },
  },
})
