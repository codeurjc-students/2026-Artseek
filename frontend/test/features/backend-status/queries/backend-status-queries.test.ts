import { describe, expect, it } from 'vitest'
import { backendStatusQueryOptions } from '../../../../app/features/backend-status/queries/backend-status-queries'
import { queryClient } from '../../../../app/lib/query-client'
import { mockFetch } from '../../../test-utils'

describe('backend status queries', () => {
  it('checks every time, including after a cached success', async () => {
    mockFetch().mockImplementation(async () => new Response(null, { status: 204 }))

    await expect(queryClient.query(backendStatusQueryOptions())).resolves.toEqual({ available: true })
    await expect(queryClient.query(backendStatusQueryOptions())).resolves.toEqual({ available: true })
    expect(mockFetch()).toHaveBeenCalledTimes(2)
    expect(mockFetch()).toHaveBeenCalledWith('/api/health', { signal: expect.any(AbortSignal) })
  })

  it('retries a transient failure and recovers', async () => {
    mockFetch().mockRejectedValueOnce(new TypeError('Temporary failure'))
      .mockResolvedValueOnce(new Response(null, { status: 200 }))

    await expect(queryClient.query(backendStatusQueryOptions())).resolves.toEqual({ available: true })
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })

  it('rejects a failed health check after one retry', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    await expect(queryClient.query(backendStatusQueryOptions())).rejects.toThrow('HTTP 503')
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })
})
