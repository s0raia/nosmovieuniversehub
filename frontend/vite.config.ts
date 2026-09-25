import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The /api proxy means the browser only ever talks to one origin, so there is
// no CORS configuration and session cookies work without extra handling.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
