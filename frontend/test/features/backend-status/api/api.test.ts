import { describe, expect, it } from 'vitest'
import { mockFetch } from '../../../test-utils'
import { fetchBackendStatus } from '../../../../app/features/backend-status/api/api'

describe('backend status API', () => {
  it.each([200, 204])('accepts an empty HTTP %i response without parsing JSON', async (status) => {
    const signal = new AbortController().signal
    mockFetch().mockResolvedValue(new Response(null, { status }))

    await expect(fetchBackendStatus(signal)).resolves.toEqual({ available: true })
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/health', { signal })
  })

  it('rejects unsuccessful HTTP responses', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    await expect(fetchBackendStatus()).rejects.toThrow('Backend health check failed with HTTP 503')
  })

  it('propagates network failures', async () => {
    const failure = new TypeError('Network unavailable')
    mockFetch().mockRejectedValue(failure)
    await expect(fetchBackendStatus()).rejects.toBe(failure)
  })

  it('propagates request cancellation', async () => {
    const failure = new DOMException('Request aborted', 'AbortError')
    mockFetch().mockRejectedValue(failure)
    await expect(fetchBackendStatus(AbortSignal.abort())).rejects.toBe(failure)
  })
})
