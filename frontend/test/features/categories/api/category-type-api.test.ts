import { describe, expect, it } from 'vitest'
import { fetchUsedCategoryTypes } from '../../../../app/features/categories/api/category-type-api'
import { categoryTypes } from '../../../fixtures'
import { mockFetch } from '../../../test-utils'

describe('used category types API', () => {
  it('loads validated types and forwards the abort signal', async () => {
    const signal = new AbortController().signal
    mockFetch().mockResolvedValue(Response.json(categoryTypes))

    await expect(fetchUsedCategoryTypes(signal)).resolves.toEqual(categoryTypes)
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/v1/categories/types/used', {
      signal, headers: expect.any(Headers),
    })
  })

  it('accepts an empty collection', async () => {
    mockFetch().mockResolvedValue(Response.json([]))
    await expect(fetchUsedCategoryTypes()).resolves.toEqual([])
  })

  it('rejects unsuccessful HTTP responses', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    await expect(fetchUsedCategoryTypes()).rejects.toThrow('HTTP 503')
  })

  it.each([[null], [{}], [[null]], [[{ value: 1 }]], [[{}]]])('rejects invalid response data: %j', async (value) => {
    mockFetch().mockResolvedValue(Response.json(value))
    await expect(fetchUsedCategoryTypes()).rejects.toThrow('Used category types response has an invalid shape')
  })

  it('rejects malformed JSON', async () => {
    mockFetch().mockResolvedValue(new Response('not JSON'))
    await expect(fetchUsedCategoryTypes()).rejects.toBeInstanceOf(SyntaxError)
  })

  it('rejects a bodyless response instead of treating it as an empty collection', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 204 }))
    await expect(fetchUsedCategoryTypes()).rejects.toThrow('Used category types response has an invalid shape')
  })

  it('propagates network failures', async () => {
    const failure = new TypeError('Network unavailable')
    mockFetch().mockRejectedValue(failure)
    await expect(fetchUsedCategoryTypes()).rejects.toBe(failure)
  })

  it('propagates request cancellation', async () => {
    const signal = AbortSignal.abort()
    const failure = new DOMException('Request aborted', 'AbortError')
    mockFetch().mockRejectedValue(failure)
    await expect(fetchUsedCategoryTypes(signal)).rejects.toBe(failure)
    expect(mockFetch()).toHaveBeenCalledWith('/api/v1/categories/types/used', {
      signal, headers: expect.any(Headers),
    })
  })
})
