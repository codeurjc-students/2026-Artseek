import { describe, expect, it } from 'vitest'
import { usedCategoryTypesQueryOptions } from '../../../../app/features/categories/queries/category-type-queries'
import { queryClient } from '../../../../app/lib/query-client'
import { categoryTypes } from '../../../fixtures'
import { mockFetch } from '../../../test-utils'

describe('used category types queries', () => {
  it('loads types and reuses fresh data', async () => {
    mockFetch().mockResolvedValue(Response.json(categoryTypes))
    const options = usedCategoryTypesQueryOptions()

    await expect(queryClient.query(options)).resolves.toEqual(categoryTypes)
    await expect(queryClient.query(options)).resolves.toEqual(categoryTypes)
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/v1/categories/types/used', {
      signal: expect.any(AbortSignal),
    })
  })

  it('loads again after the freshness window expires', async () => {
    mockFetch().mockImplementation(async () => Response.json(categoryTypes))
    const options = usedCategoryTypesQueryOptions()
    expect(options.staleTime).toBe(60_000)
    await queryClient.query(options)
    queryClient.setQueryData(options.queryKey, categoryTypes, {
      updatedAt: Date.now() - 60_001,
    })

    await queryClient.query(options)
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })

  it('loads invalidated data again', async () => {
    mockFetch().mockImplementation(async () => Response.json(categoryTypes))
    const options = usedCategoryTypesQueryOptions()
    await queryClient.query(options)
    await queryClient.invalidateQueries({ queryKey: options.queryKey })

    expect(mockFetch()).toHaveBeenCalledTimes(1)
    await queryClient.query(options)
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })

  it('accepts an empty collection', async () => {
    mockFetch().mockResolvedValue(Response.json([]))
    await expect(queryClient.query(usedCategoryTypesQueryOptions())).resolves.toEqual([])
  })

  it('rejects network failures after one retry', async () => {
    mockFetch().mockRejectedValue(new TypeError('Network unavailable'))
    await expect(queryClient.query(usedCategoryTypesQueryOptions())).rejects.toThrow('Network unavailable')
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })
})
