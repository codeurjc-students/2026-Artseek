import { queryOptions } from '@tanstack/react-query'

import { fetchCategoriesByType } from '../api/category-api'

export function categoriesByTypeQueryOptions(type: string) {
  return queryOptions({
    queryKey: ['categories', 'by-type', type],
    queryFn: ({ signal }) => fetchCategoriesByType(type, signal),
    retry: 1,
    staleTime: 60_000,
  })
}

