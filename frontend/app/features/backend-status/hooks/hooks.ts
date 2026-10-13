import { useQuery } from '@tanstack/react-query'

import { fetchBackendStatus } from '../api/api'

export function useBackendStatusQuery() {
  return useQuery({
    queryKey: ['backend-status'],
    queryFn: ({ signal }) => fetchBackendStatus(signal),
    retry: 1,
  })
}
