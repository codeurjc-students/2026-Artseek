import { queryOptions } from '@tanstack/react-query'

import { fetchUsedCategoryTypes } from '../api/category-type-api'

export function usedCategoryTypesQueryOptions() {
  return queryOptions({
    queryKey: ['categories', 'types', 'used'],
    queryFn: ({ signal }) => fetchUsedCategoryTypes(signal),
    retry: 1,
    staleTime: 60_000,
  })
}

