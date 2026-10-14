import { useQuery } from '@tanstack/react-query'

import { fetchUsedCategoryTypes } from '../api/category-type-api'

export function useUsedCategoryTypesQuery() {
  return useQuery({
    queryKey: ['categories', 'types', 'used'],
    queryFn: ({ signal }) => fetchUsedCategoryTypes(signal),
    retry: 1,
  })
}
