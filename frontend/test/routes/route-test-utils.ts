import type { ComponentProps } from 'react'
import { RouterContextProvider } from 'react-router'
import type Category from '../../app/routes/CategoryRoute'
import type { clientLoader as categoryLoader } from '../../app/routes/CategoryRoute'
import type Health from '../../app/routes/HealthRoute'
import type Home from '../../app/routes/HomeRoute'
import type { clientLoader as healthLoader } from '../../app/routes/HealthRoute'
import type { clientLoader as homeLoader } from '../../app/routes/HomeRoute'

const rootMatch = { id: 'root', pathname: '/', params: {}, loaderData: undefined, handle: undefined } as const

function loaderArgs(path: string, pattern = path, signal?: AbortSignal) {
  return {
    params: {},
    request: new Request(`http://localhost${path}`, { signal }),
    url: new URL(`http://localhost${path}`),
    pattern,
    context: new RouterContextProvider(),
    serverLoader: async () => { throw new Error('These SPA routes have no server loader') },
  }
}

export function categoryLoaderArgs(type = 'painting', signal?: AbortSignal): Parameters<typeof categoryLoader>[0] {
  return {
    ...loaderArgs(`/category/${encodeURIComponent(type)}`, '/category/:categoryType', signal),
    params: { categoryType: type },
  }
}

export function homeLoaderArgs(signal?: AbortSignal): Parameters<typeof homeLoader>[0] {
  return loaderArgs('/', '/', signal)
}

export function healthLoaderArgs(signal?: AbortSignal): Parameters<typeof healthLoader>[0] {
  return loaderArgs('/health', '/health', signal)
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
