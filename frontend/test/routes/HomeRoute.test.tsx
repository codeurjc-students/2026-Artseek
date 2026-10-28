import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import Home, { clientLoader } from '../../app/routes/HomeRoute'
import { categoryTypes } from '../fixtures'
import { mockFetch } from '../test-utils'
import { homeLoaderArgs, homeProps } from './route-test-utils'

describe('Home route', () => {
  it('loads category types and displays encoded links', async () => {
    mockFetch().mockResolvedValue(Response.json(categoryTypes))
    const args = homeLoaderArgs()
    const loaderData = await clientLoader(args)
    render(<Home {...homeProps(loaderData)} />, { wrapper: MemoryRouter })

    expect(screen.getByRole('link', { name: 'painting' })).toHaveAttribute('href', '/category/painting')
    expect(screen.getByRole('link', { name: 'digital art/3D' })).toHaveAttribute('href', '/category/digital%20art%2F3D')
    expect(mockFetch()).toHaveBeenCalledExactlyOnceWith('/api/v1/categories/types/used', {
      signal: args.request.signal, headers: expect.any(Headers),
    })
  })

  it('renders the empty state when no types are in use', async () => {
    mockFetch().mockResolvedValue(Response.json([]))
    const loaderData = await clientLoader(homeLoaderArgs())
    render(<Home {...homeProps(loaderData)} />, { wrapper: MemoryRouter })

    expect(screen.getByRole('alert')).toHaveTextContent('No categories are currently available.')
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
  })

  it.each(['HTTP', 'network'])('propagates %s failures to the shared route boundary', async (failure) => {
    if (failure === 'HTTP') mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    else mockFetch().mockRejectedValue(new TypeError('Network unavailable'))

    await expect(clientLoader(homeLoaderArgs())).rejects.toThrow(failure === 'HTTP' ? 'HTTP 503' : 'Network unavailable')
  })

  it('loads and renders updated types on the next loader execution', async () => {
    mockFetch().mockResolvedValueOnce(Response.json(categoryTypes))
      .mockResolvedValueOnce(Response.json([{ value: 'sculpture' }]))
    const loaderData = await clientLoader(homeLoaderArgs())
    const { rerender } = render(<Home {...homeProps(loaderData)} />, { wrapper: MemoryRouter })
    expect(screen.getByRole('link', { name: 'painting' })).toBeInTheDocument()

    rerender(<Home {...homeProps(await clientLoader(homeLoaderArgs()))} />)
    expect(screen.getByRole('link', { name: 'sculpture' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'painting' })).not.toBeInTheDocument()
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })
})
