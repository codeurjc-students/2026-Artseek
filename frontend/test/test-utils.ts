import { vi } from 'vitest'

export function mockFetch() {
  return vi.mocked(globalThis.fetch)
}

export function deferred<T>() {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((resolvePromise) => { resolve = resolvePromise })
  return { promise, resolve }
}
