import type { ComponentProps } from 'react'
import { RouterContextProvider } from 'react-router'
import type Category from '../../app/routes/CategoryRoute'
import type { clientLoader as categoryLoader } from '../../app/routes/CategoryRoute'
import type Health from '../../app/routes/HealthRoute'
import type Home from '../../app/routes/HomeRoute'

const rootMatch = { id: 'root', pathname: '/', params: {}, loaderData: undefined, handle: undefined } as const

export function categoryLoaderArgs(type = 'painting'): Parameters<typeof categoryLoader>[0] {
  return {
    params: { categoryType: type },
    request: new Request(`http://localhost/category/${encodeURIComponent(type)}`),
    url: new URL(`http://localhost/category/${encodeURIComponent(type)}`),
    pattern: '/category/:categoryType',
    context: new RouterContextProvider(),
    serverLoader: async () => { throw new Error('These SPA routes have no server loader') },
  }
}

export function categoryProps(
  loaderData: ComponentProps<typeof Category>['loaderData'],
  type = 'painting',
): ComponentProps<typeof Category> {
  const params = { categoryType: type }
  return {
    params, loaderData, actionData: undefined,
    matches: [rootMatch, {
      id: 'routes/CategoryRoute', pathname: `/category/${type}`, params,
      loaderData, handle: undefined,
    }],
  }
}

export function homeProps(loaderData: ComponentProps<typeof Home>['loaderData']): ComponentProps<typeof Home> {
  return {
    params: {}, loaderData, actionData: undefined,
    matches: [rootMatch, {
      id: 'routes/HomeRoute', pathname: '/', params: {}, loaderData, handle: undefined,
    }],
  }
}

export function healthProps(loaderData: ComponentProps<typeof Health>['loaderData']): ComponentProps<typeof Health> {
  return {
    params: {}, loaderData, actionData: undefined,
    matches: [rootMatch, {
      id: 'routes/HealthRoute', pathname: '/health', params: {}, loaderData, handle: undefined,
    }],
  }
}
