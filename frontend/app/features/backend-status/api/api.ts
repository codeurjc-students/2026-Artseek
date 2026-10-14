export interface BackendStatus {
  available: true
}

export async function fetchBackendStatus(signal?: AbortSignal): Promise<BackendStatus> {
  const response = await fetch('/api/health', { signal })

  if (!response.ok) {
    throw new Error(`Backend health check failed with HTTP ${response.status}`)
  }

  return { available: true }
}
