import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import Health, { clientLoader } from '../../app/routes/HealthRoute'
import { mockFetch } from '../test-utils'
import { healthLoaderArgs, healthProps } from './route-test-utils'

describe('Health route', () => {
  it('displays availability for a successful empty health response', async () => {
    mockFetch().mockResolvedValue(new Response(null, { status: 204 }))
    const args = healthLoaderArgs()
    const loaderData = await clientLoader(args)
    render(<Health {...healthProps(loaderData)} />)

    expect(loaderData).toEqual({ backendStatus: { available: true } })
    expect(screen.getByRole('status')).toHaveTextContent('Backend is up and running')
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/health', {
      signal: args.request.signal, headers: expect.any(Headers),
    })
  })

  it.each(['HTTP', 'network'])('renders unavailable on %s failure', async (failure) => {
    if (failure === 'HTTP') mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    else mockFetch().mockRejectedValue(new TypeError('Network unavailable'))
    const loaderData = await clientLoader(healthLoaderArgs())
    render(<Health {...healthProps(loaderData)} />)

    expect(loaderData).toEqual({ backendStatus: null })
    expect(screen.getByRole('status')).toHaveTextContent('Backend unavailable')
  })

  it('detects failure after success and recovery on the next visit', async () => {
    mockFetch().mockResolvedValueOnce(new Response(null, { status: 204 }))
    const first = await clientLoader(healthLoaderArgs())
    const { rerender } = render(<Health {...healthProps(first)} />)
    expect(screen.getByRole('status')).toHaveTextContent('Backend is up and running')

    mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    rerender(<Health {...healthProps(await clientLoader(healthLoaderArgs()))} />)
    expect(screen.getByRole('status')).toHaveTextContent('Backend unavailable')

    mockFetch().mockResolvedValueOnce(new Response(null, { status: 204 }))
    rerender(<Health {...healthProps(await clientLoader(healthLoaderArgs()))} />)
    expect(screen.getByRole('status')).toHaveTextContent('Backend is up and running')
    expect(mockFetch()).toHaveBeenCalledTimes(3)
  })

  it('propagates cancellation instead of reporting an unavailable backend', async () => {
    const controller = new AbortController()
    mockFetch().mockImplementation(async (_path, options) => {
      options?.signal?.throwIfAborted()
      return new Response(null, { status: 200 })
    })
    const args = healthLoaderArgs(controller.signal)
    controller.abort()

    await expect(clientLoader(args)).rejects.toHaveProperty('name', 'AbortError')
  })
})
