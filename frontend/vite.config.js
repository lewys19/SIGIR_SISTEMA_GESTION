import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5180,
    // En desarrollo, /api y /ws van al backend Quarkus (sin problemas de CORS)
    proxy: {
      '/api': 'http://localhost:8090',
      '/ws': { target: 'ws://localhost:8090', ws: true },
    },
  },
  test: { environment: 'node' },
})
