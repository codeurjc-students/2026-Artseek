import { queryOptions } from '@tanstack/react-query'

import { fetchBackendStatus } from '../api/api'

export function backendStatusQueryOptions() {
  return queryOptions({
    queryKey: ['backend-status'],
    queryFn: ({ signal }) => fetchBackendStatus(signal),
    retry: 1,
    staleTime: 0,
  })
}

