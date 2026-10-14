import { useQuery } from '@tanstack/react-query'

import { fetchCategoriesByType } from '../api/category-api'

export function useCategoriesByTypeQuery(type: string) {
  return useQuery({
    queryKey: ['categories', 'by-type', type],
    queryFn: ({ signal }) => fetchCategoriesByType(type, signal),
    retry: 1,
  })
}
