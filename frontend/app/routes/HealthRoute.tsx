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
    <main>
      <p>Explore · Connect · Create</p>
      <h1>Artseek</h1>
      <p role="status">{statusMessage}</p>
    </main>
  )
}
