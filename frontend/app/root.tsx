import {
  isRouteErrorResponse,
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
} from 'react-router'
import type { ReactNode } from 'react'

import type { Route } from './+types/root'
import { AppProviders } from './providers'
import 'bootstrap/dist/css/bootstrap.min.css'
import './app.css'

export function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  )
}

export default function App() {
  return (
    <AppProviders>
      <Outlet />
    </AppProviders>
  )
}

export function HydrateFallback() {
  return (
    <main className="mx-auto max-w-6xl px-8 py-24" aria-live="polite">
      <div
        className="size-8 animate-spin rounded-full border-[3px] border-stone-300 border-t-rust"
        aria-hidden="true"
      />
      <p>Loading Artseek…</p>
    </main>
  )
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  let message = 'Oops!'
  let details = 'An unexpected error occurred.'
  let stack: string | undefined

  if (isRouteErrorResponse(error)) {
    message = error.status === 404 ? '404' : 'Error'
    details = error.status === 404
      ? 'The requested page could not be found.'
      : error.statusText || details
  } else if (import.meta.env.DEV && error instanceof Error) {
    details = error.message
    stack = error.stack
  }

  return (
    <main className="mx-auto max-w-6xl px-8 py-24">
      <h1>{message}</h1>
      <p>{details}</p>
      {stack && (
        <pre className="max-w-full overflow-x-auto bg-white p-4">
          <code>{stack}</code>
        </pre>
      )}
    </main>
  )
}
