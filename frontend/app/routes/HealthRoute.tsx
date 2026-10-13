import type { Route } from './+types/HealthRoute'
import { useBackendStatusQuery } from '../features/backend-status/hooks/hooks'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Artseek' },
    { name: 'description', content: 'Discover art with Artseek' },
  ]
}

export default function Health() {
  const backendStatus = useBackendStatusQuery()

  const statusMessage = backendStatus.isPending
    ? 'Connecting to the backend…'
    : backendStatus.isError
      ? 'Backend unavailable'
      : 'Backend is up and running'

  return (
    <main className="mx-auto max-w-6xl px-8 py-24">
      <p className="text-xs font-bold tracking-[0.14em] text-rust uppercase">
        Explore · Connect · Create
      </p>
      <h1 className="my-2 font-serif text-5xl sm:text-7xl lg:text-8xl">Artseek</h1>
      <p role="status">{statusMessage}</p>
    </main>
  )
}
