import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [((uni as unknown as { default?: typeof uni }).default || uni)()],
  server: { proxy: { '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true, rewrite: (path) => path.replace(/^\/api/, '') } } },
})
