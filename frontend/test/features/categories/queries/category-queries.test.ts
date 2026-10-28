import { describe, expect, it } from 'vitest'
import { categoriesByTypeQueryOptions } from '../../../../app/features/categories/queries/category-queries'
import { queryClient } from '../../../../app/lib/query-client'
import { categories } from '../../../fixtures'
import { deferred, mockFetch } from '../../../test-utils'

describe('categories by type queries', () => {
  it('loads categories and reuses fresh data for the same type', async () => {
    mockFetch().mockResolvedValue(Response.json(categories))
    const options = categoriesByTypeQueryOptions('painting')

    await expect(queryClient.query(options)).resolves.toEqual(categories)
    await expect(queryClient.query(options)).resolves.toEqual(categories)
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/v1/categories/painting', {
      signal: expect.any(AbortSignal),
    })
  })

  it('uses separate cache entries for different category types', async () => {
    const sculpture = [{ id: 3, name: 'Bronze', type: 'sculpture' }]
    mockFetch()
      .mockResolvedValueOnce(Response.json(categories))
      .mockResolvedValueOnce(Response.json(sculpture))

    await queryClient.query(categoriesByTypeQueryOptions('painting'))
    await expect(queryClient.query(categoriesByTypeQueryOptions('sculpture'))).resolves.toEqual(sculpture)
    await expect(queryClient.query(categoriesByTypeQueryOptions('painting'))).resolves.toEqual(categories)
    expect(mockFetch()).toHaveBeenCalledTimes(2)
    expect(mockFetch()).toHaveBeenNthCalledWith(2, '/api/v1/categories/sculpture', { signal: expect.any(AbortSignal) })
  })

  it('treats an empty collection as successful data', async () => {
    mockFetch().mockResolvedValue(Response.json([]))
    await expect(queryClient.query(categoriesByTypeQueryOptions('painting'))).resolves.toEqual([])
  })

  it('retries a transient failure and recovers', async () => {
    mockFetch().mockRejectedValueOnce(new TypeError('Temporary failure'))
      .mockResolvedValueOnce(Response.json(categories))
    await expect(queryClient.query(categoriesByTypeQueryOptions('painting'))).resolves.toEqual(categories)
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })

  it('rejects after one failed retry', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    await expect(queryClient.query(categoriesByTypeQueryOptions('painting'))).rejects.toThrow('HTTP 503')
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })

  it('forwards cache cancellation to the HTTP request', async () => {
    mockFetch().mockReturnValue(deferred<Response>().promise)
    const options = categoriesByTypeQueryOptions('painting')
    const pending = queryClient.query(options)
    const rejection = expect(pending).rejects.toThrow()
    const signal = mockFetch().mock.calls[0][1]?.signal

    expect(signal?.aborted).toBe(false)
    await queryClient.cancelQueries({ queryKey: options.queryKey })
    await rejection
    expect(signal?.aborted).toBe(true)
  })
})
