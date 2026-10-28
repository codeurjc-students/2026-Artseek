import { apiRequest } from '../../../utils/api-request'
import type { BackendStatus } from '../model/BackendStatus'

export async function fetchBackendStatus(signal?: AbortSignal): Promise<BackendStatus> {
  await apiRequest<void>('/api/health', { signal })
  return { available: true }
}
