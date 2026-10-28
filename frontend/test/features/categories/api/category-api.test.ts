import { describe, expect, it } from 'vitest'
import { categories } from '../../../fixtures'
import { mockFetch } from '../../../test-utils'
import { fetchCategoriesByType } from '../../../../app/features/categories/api/category-api'

describe('categories by type API', () => {
  const request = (signal?: AbortSignal) => fetchCategoriesByType('digital art/3D', signal)
  const endpoint = '/api/v1/categories/digital%20art%2F3D'
  it('loads validated data from the endpoint and forwards the abort signal', async () => {
    const signal = new AbortController().signal
    mockFetch().mockResolvedValue(Response.json(categories))

    await expect(request(signal)).resolves.toEqual(categories)
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith(endpoint, { signal, headers: expect.any(Headers) })
  })

  it('accepts an empty collection', async () => {
    mockFetch().mockResolvedValue(Response.json([]))
    await expect(request()).resolves.toEqual([])
  })

  it('rejects unsuccessful HTTP responses', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    await expect(request()).rejects.toThrow('HTTP 503')
  })

  it.each([
    [null], [{}], [[null]], [[{ id: '1', name: 'Oil', type: 'painting' }]],
    [[{ id: 1, name: 2, type: 'painting' }]], [[{ id: 1, name: 'Oil' }]],
  ])('rejects invalid response data: %j', async (value) => {
    mockFetch().mockResolvedValue(Response.json(value))
    await expect(request()).rejects.toThrow('Categories response has an invalid shape')
  })

  it('rejects malformed JSON', async () => {
    mockFetch().mockResolvedValue(new Response('not JSON'))
    await expect(request()).rejects.toBeInstanceOf(SyntaxError)
  })

  it('rejects a bodyless response instead of treating it as an empty collection', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 204 }))
    await expect(request()).rejects.toThrow('Categories response has an invalid shape')
  })

  it('propagates network failures', async () => {
    const failure = new TypeError('Network unavailable')
    mockFetch().mockRejectedValue(failure)
    await expect(request()).rejects.toBe(failure)
  })

  it('propagates request cancellation', async () => {
    const signal = AbortSignal.abort()
    const failure = new DOMException('Request aborted', 'AbortError')
    mockFetch().mockRejectedValue(failure)
    await expect(request(signal)).rejects.toBe(failure)
    expect(mockFetch()).toHaveBeenCalledWith(endpoint, { signal, headers: expect.any(Headers) })
  })
})
