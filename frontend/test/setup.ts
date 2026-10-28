import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach, beforeEach, vi } from 'vitest'

beforeEach(() => {
  // No test can accidentally reach a live backend, even without its own mock.
  vi.stubGlobal('fetch', vi.fn<typeof fetch>().mockImplementation(async (input) => {
    throw new Error(`Unexpected request: ${String(input)}`)
  }))
})

afterEach(() => {
  cleanup()
})
