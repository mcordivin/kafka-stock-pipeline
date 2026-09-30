import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'

// export default defineConfig(({ mode }) => {
//   const env = loadEnv(mode, '.', '');
//   const target = env.API_SERVER_URL || 'http://localhost:8081';
//   return {
//     plugins: [react(), tailwindcss()],
//     server: {
//       port: 5173,
//       proxy: {
//         '/symbols': target,
//         '/candles': target,
//         '/ws': { target, ws: true },
//       },
//     },
//   };
// });

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/ws': {
        target: 'http://localhost:8082',  // not ws://
        ws: true,
        changeOrigin: true
      },
      '/symbols': 'http://localhost:8082',
      '/candles': 'http://localhost:8082',
      '/alerts': 'http://localhost:8082',
    },
  },
})
