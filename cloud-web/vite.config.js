import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/user': {
        target: 'http://localhost:8089',
        changeOrigin: true,
      },
      '/course': {
        target: 'http://localhost:8089',
        changeOrigin: true,
      },
      '/evaluation': {
        target: 'http://localhost:8089',
        changeOrigin: true,
      },
    },
  },
})
