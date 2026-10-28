import { describe, expect, it } from 'vitest'
import { apiRequest, HttpError } from '../../app/utils/api-request'
import { mockFetch } from '../test-utils'

describe('shared API requests', () => {
  it('parses JSON responses', async () => {
    mockFetch().mockResolvedValue(Response.json({ id: 1 }))

    const result = await apiRequest<{ id: number }>('/api/resources')
    expect(result.id).toBe(1)
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/resources', { headers: expect.any(Headers) })
    expect(new Headers(mockFetch().mock.calls[0][1]?.headers).has('Content-Type')).toBe(false)
  })

  it.each([200, 204, 205])('returns undefined for an empty HTTP %i response', async (status) => {
    mockFetch().mockResolvedValue(new Response(null, { status }))
    await expect(apiRequest<void>('/api/resources')).resolves.toBeUndefined()
  })

  it.each<HeadersInit>([
    { 'Content-Type': 'application/json', Authorization: 'Bearer test-token' },
    [['Content-Type', 'application/json'], ['X-Request-ID', 'test-request']],
    new Headers({ 'Content-Type': 'application/json', 'X-Request-ID': 'test-request' }),
  ])('preserves supplied headers and all native request options: %j', async (headers) => {
    const options: RequestInit = {
      method: 'PATCH',
      headers,
      body: JSON.stringify({ name: 'Updated' }),
      signal: new AbortController().signal,
      credentials: 'include',
      cache: 'no-store',
      mode: 'same-origin',
      redirect: 'error',
      referrerPolicy: 'no-referrer',
      keepalive: true,
    }
    mockFetch().mockResolvedValue(Response.json({ id: 1 }))

    await apiRequest('/api/resources/1', options)

    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/resources/1', {
      ...options, headers: expect.any(Headers),
    })
    expect([...new Headers(mockFetch().mock.calls[0][1]?.headers)]).toEqual([...new Headers(headers)])
  })

  it('defaults string bodies to JSON without mutating supplied headers or options', async () => {
    const headers = new Headers({ 'X-Request-ID': 'test-request' })
    const init = { method: 'POST', headers, body: JSON.stringify({ name: 'Artwork' }) }
    mockFetch().mockResolvedValue(Response.json({ id: 1 }))

    await apiRequest('/api/resources', init)

    const sentHeaders = new Headers(mockFetch().mock.calls[0][1]?.headers)
    expect(sentHeaders.get('Content-Type')).toBe('application/json')
    expect(sentHeaders.get('X-Request-ID')).toBe('test-request')
    expect(headers.has('Content-Type')).toBe(false)
    expect(init.headers).toBe(headers)
    expect(init.body).toBe('{"name":"Artwork"}')
  })

  it('preserves an explicit content type case-insensitively', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 204 }))
    await apiRequest('/api/resources', {
      method: 'POST', body: 'plain text', headers: { 'content-type': 'text/plain' },
    })

    expect(new Headers(mockFetch().mock.calls[0][1]?.headers).get('Content-Type')).toBe('text/plain')
  })

  it('passes multipart bodies through without adding a content type', async () => {
    const body = new FormData()
    body.append('name', 'Artwork')
    mockFetch().mockResolvedValue(new Response(null, { status: 204 }))

    await apiRequest('/api/resources', { method: 'POST', body })

    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/resources', {
      method: 'POST', body, headers: expect.any(Headers),
    })
    expect(mockFetch().mock.calls[0][1]?.body).toBe(body)
    expect(new Headers(mockFetch().mock.calls[0][1]?.headers).has('Content-Type')).toBe(false)
  })

  it.each([400, 401, 404, 500, 502, 503])('rejects non-JSON HTTP %i errors consistently', async (status) => {
    mockFetch().mockResolvedValue(new Response('not JSON', { status }))
    const result = apiRequest('/api/resources')

    await expect(result).rejects.toBeInstanceOf(HttpError)
    await expect(result).rejects.toMatchObject({ status })
    await expect(result).rejects.toThrow(`HTTP ${status}`)
  })

  it.each(['POST', 'PUT', 'PATCH', 'DELETE'])('sends %s requests with the supplied body', async (method) => {
    const body = JSON.stringify({ name: 'Artwork' })
    mockFetch().mockResolvedValue(new Response(null, { status: 204 }))

    await apiRequest<void>('/api/resources/1', { method, body })

    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/resources/1', {
      method, body, headers: expect.any(Headers),
    })
    expect(new Headers(mockFetch().mock.calls[0][1]?.headers).get('Content-Type')).toBe('application/json')
  })

  it('includes the HTTP status text without relying on an error-body format', async () => {
    mockFetch().mockResolvedValue(new Response('<html>Bad gateway</html>', {
      status: 502, statusText: 'Bad Gateway',
    }))
    await expect(apiRequest('/api/resources')).rejects.toMatchObject({
      status: 502, statusText: 'Bad Gateway', message: 'Request failed with HTTP 502 Bad Gateway',
    })
  })

  it('propagates malformed JSON', async () => {
    mockFetch().mockResolvedValue(new Response('not JSON'))
    await expect(apiRequest('/api/resources')).rejects.toBeInstanceOf(SyntaxError)
  })

  it('preserves network failures', async () => {
    const failure = new TypeError('Network unavailable')
    mockFetch().mockRejectedValue(failure)
    await expect(apiRequest('/api/resources')).rejects.toBe(failure)
  })

  it('forwards cancellation of an in-flight request and preserves its rejection', async () => {
    const controller = new AbortController()
    mockFetch().mockImplementation((_path, options) => new Promise((_resolve, reject) => {
      options?.signal?.addEventListener('abort', () => reject(options.signal?.reason), { once: true })
    }))
    const pending = apiRequest('/api/resources', { signal: controller.signal })
    const rejection = expect(pending).rejects.toHaveProperty('name', 'AbortError')
    controller.abort()

    await rejection
    await expect(pending).rejects.toBe(controller.signal.reason)
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/resources', {
      signal: controller.signal, headers: expect.any(Headers),
    })
  })
})
