import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import Home, { clientLoader } from '../../app/routes/HomeRoute'
import { categoryTypes } from '../fixtures'
import { mockFetch } from '../test-utils'
import { homeProps } from './route-test-utils'

describe('Home route', () => {
  it('loads category types and displays encoded links', async () => {
    mockFetch().mockResolvedValue(Response.json(categoryTypes))
    const loaderData = await clientLoader()
    render(<Home {...homeProps(loaderData)} />, { wrapper: MemoryRouter })

    expect(screen.getByRole('link', { name: 'painting' })).toHaveAttribute('href', '/category/painting')
    expect(screen.getByRole('link', { name: 'digital art/3D' })).toHaveAttribute('href', '/category/digital%20art%2F3D')
    await clientLoader()
    expect(mockFetch()).toHaveBeenCalledTimes(1)
  })

  it('renders the empty state when no types are in use', async () => {
    mockFetch().mockResolvedValue(Response.json([]))
    const loaderData = await clientLoader()
    render(<Home {...homeProps(loaderData)} />, { wrapper: MemoryRouter })

    expect(screen.getByRole('alert')).toHaveTextContent('No categories are currently available.')
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
  })

  it.each(['HTTP', 'network'])('propagates %s failures to the shared route boundary', async (failure) => {
    if (failure === 'HTTP') mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    else mockFetch().mockRejectedValue(new TypeError('Network unavailable'))

    await expect(clientLoader()).rejects.toThrow(failure === 'HTTP' ? 'HTTP 503' : 'Network unavailable')
  })

  it('renders the loader snapshot without subscribing to later cache changes', async () => {
    const { queryClient } = await import('../../app/lib/query-client')
    const { usedCategoryTypesQueryOptions } = await import('../../app/features/categories/queries/category-type-queries')
    mockFetch().mockResolvedValue(Response.json(categoryTypes))
    const loaderData = await clientLoader()
    render(<Home {...homeProps(loaderData)} />, { wrapper: MemoryRouter })

    queryClient.setQueryData(usedCategoryTypesQueryOptions().queryKey, [{ value: 'sculpture' }])
    expect(screen.getByRole('link', { name: 'painting' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'sculpture' })).not.toBeInTheDocument()
  })
})
