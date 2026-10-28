import { act, fireEvent, render, screen, waitFor } from '@testing-library/react'
import type { ComponentProps } from 'react'
import { createRoutesStub, Link, MemoryRouter, useRevalidator } from 'react-router'
import { describe, expect, it } from 'vitest'
import Category, { clientLoader } from '../../app/routes/CategoryRoute'
import { categories } from '../fixtures'
import { deferred, mockFetch } from '../test-utils'
import { categoryLoaderArgs, categoryProps } from './route-test-utils'

function CategoryWithControls(props: ComponentProps<typeof Category>) {
  const revalidator = useRevalidator()
  return (
    <>
      <Category {...props} />
      <Link to="/category/sculpture">Sculpture</Link>
      <button onClick={() => { void revalidator.revalidate() }}>Refresh categories</button>
    </>
  )
}

function mountCategoryRoute() {
  const Routes = createRoutesStub([
    {
      path: '/category/:categoryType',
      Component: CategoryWithControls,
      HydrateFallback: () => <p>Loading categories…</p>,
      loader: ({ params, request }) => clientLoader({
        ...categoryLoaderArgs(params.categoryType), request,
      }),
    },
    { path: '/', Component: () => <h1>Category types</h1> },
  ])
  return render(<Routes initialEntries={['/category/painting']} />)
}

describe('Category route', () => {
  it('loads and displays categories for the route parameter', async () => {
    mockFetch().mockResolvedValue(Response.json(categories))
    const args = categoryLoaderArgs()
    const loaderData = await clientLoader(args)
    render(<Category {...categoryProps(loaderData)} />, { wrapper: MemoryRouter })

    expect(screen.getByText('Watercolour')).toBeInTheDocument()
    expect(screen.getByText('Oil painting')).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 1, name: 'painting' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Back to category types' })).toHaveAttribute('href', '/')
    expect(mockFetch()).toHaveBeenCalledWith('/api/v1/categories/painting', {
      signal: args.request.signal, headers: expect.any(Headers),
    })
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

  it('requests new data for each route parameter and on return visits', async () => {
    const sculpture = [{ id: 3, name: 'Bronze', type: 'sculpture' }]
    mockFetch().mockResolvedValueOnce(Response.json(categories))
      .mockResolvedValueOnce(Response.json(sculpture))
      .mockResolvedValueOnce(Response.json([]))

    await expect(clientLoader(categoryLoaderArgs())).resolves.toEqual({ categories })
    await expect(clientLoader(categoryLoaderArgs('sculpture'))).resolves.toEqual({ categories: sculpture })
    await expect(clientLoader(categoryLoaderArgs())).resolves.toEqual({ categories: [] })
    expect(mockFetch()).toHaveBeenCalledTimes(3)
    expect(mockFetch()).toHaveBeenNthCalledWith(2, '/api/v1/categories/sculpture', {
      signal: expect.any(AbortSignal), headers: expect.any(Headers),
    })
  })

  it('displays current backend data after router revalidation', async () => {
    mockFetch().mockResolvedValueOnce(Response.json(categories))
      .mockResolvedValueOnce(Response.json([]))
    mountCategoryRoute()
    expect(await screen.findByText('Watercolour')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Refresh categories' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('No categories are currently available for this type.')
    expect(screen.queryByText('Watercolour')).not.toBeInTheDocument()
    expect(mockFetch()).toHaveBeenCalledTimes(2)
  })

  it('cancels interrupted navigation and ignores late category data', async () => {
    const response = deferred<Response>()
    // Ignore cancellation in the mock so the late-response case is exercised too.
    mockFetch().mockResolvedValueOnce(Response.json(categories)).mockReturnValueOnce(response.promise)
    mountCategoryRoute()
    expect(await screen.findByText('Watercolour')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('link', { name: 'Sculpture' }))
    await waitFor(() => expect(mockFetch()).toHaveBeenCalledTimes(2))
    const signal = mockFetch().mock.calls[1][1]?.signal
    expect(signal?.aborted).toBe(false)

    fireEvent.click(screen.getByRole('link', { name: 'Back to category types' }))
    expect(await screen.findByRole('heading', { name: 'Category types' })).toBeInTheDocument()
    expect(signal?.aborted).toBe(true)

    await act(async () => {
      response.resolve(Response.json([{ id: 3, name: 'Bronze', type: 'sculpture' }]))
    })
    expect(screen.queryByText('Bronze')).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Category types' })).toBeInTheDocument()
  })
})
