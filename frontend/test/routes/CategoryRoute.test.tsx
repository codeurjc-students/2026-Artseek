import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import Category, { clientLoader } from '../../app/routes/CategoryRoute'
import { categories } from '../fixtures'
import { mockFetch } from '../test-utils'
import { categoryLoaderArgs, categoryProps } from './route-test-utils'

describe('Category route', () => {
  it('loads and displays categories for the route parameter', async () => {
    mockFetch().mockResolvedValue(Response.json(categories))
    const loaderData = await clientLoader(categoryLoaderArgs())
    render(<Category {...categoryProps(loaderData)} />, { wrapper: MemoryRouter })

    expect(screen.getByText('Watercolour')).toBeInTheDocument()
    expect(screen.getByText('Oil painting')).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 1, name: 'painting' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Back to category types' })).toHaveAttribute('href', '/')
    expect(mockFetch()).toHaveBeenCalledWith('/api/1.0/categories/painting', { signal: expect.any(AbortSignal) })
  })

  it('renders the empty state from loaded data', async () => {
    mockFetch().mockResolvedValue(Response.json([]))
    const loaderData = await clientLoader(categoryLoaderArgs())
    render(<Category {...categoryProps(loaderData)} />, { wrapper: MemoryRouter })

    expect(screen.getByRole('alert')).toHaveTextContent('No categories are currently available for this type.')
    expect(screen.queryByRole('listitem')).not.toBeInTheDocument()
  })

  it.each(['HTTP', 'network'])('propagates %s failures to the shared route boundary', async (failure) => {
    if (failure === 'HTTP') mockFetch().mockResolvedValue(new Response(null, { status: 503 }))
    else mockFetch().mockRejectedValue(new TypeError('Network unavailable'))

    await expect(clientLoader(categoryLoaderArgs())).rejects.toThrow(failure === 'HTTP' ? 'HTTP 503' : 'Network unavailable')
  })
})
