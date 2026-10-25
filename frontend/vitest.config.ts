import { defineConfig } from 'vitest/config'

// Keep unit tests independent of React Router's application build plugin.
export default defineConfig({
  resolve: { tsconfigPaths: true },
  test: {
    environment: 'jsdom',
    include: ['test/**/*.test.{ts,tsx}'],
    setupFiles: ['./test/setup.ts'],
    restoreMocks: true,
    unstubGlobals: true,
  },
})
