export class HttpError extends Error {
  constructor(readonly status: number, readonly statusText: string) {
    super(`Request failed with HTTP ${status}${statusText ? ` ${statusText}` : ''}`)
    this.name = 'HttpError'
  }
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const headers = new Headers(init.headers)
  if (typeof init.body === 'string' && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(path, { ...init, headers })

  if (!response.ok) {
    throw new HttpError(response.status, response.statusText)
  }

  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

