import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach, beforeEach, vi } from 'vitest'
import { queryClient } from '../app/lib/query-client'

beforeEach(() => {
  queryClient.clear()
  // Keep production retry policies while avoiding backoff and GC timers.
  queryClient.setDefaultOptions({ queries: { retryDelay: 0, gcTime: Infinity } })
  // No test can accidentally reach a live backend, even without its own mock.
  vi.stubGlobal('fetch', vi.fn<typeof fetch>().mockImplementation(async (input) => {
    throw new Error(`Unexpected request: ${String(input)}`)
  }))
})

afterEach(() => {
  cleanup()
  queryClient.clear()
})
