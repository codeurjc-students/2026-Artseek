import type { Route } from './+types/HealthRoute'
import { fetchBackendStatus } from '../features/backend-status/api/backend-status-api'

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
  try {
    const backendStatus = await fetchBackendStatus(request.signal)
    return { backendStatus }
  } catch {
    request.signal.throwIfAborted()
    // Failure is a diagnostic result here, not a reason to hide the page.
    return { backendStatus: null }
  }
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Artseek' },
    { name: 'description', content: 'Discover art with Artseek' },
  ]
}

export default function Health({ loaderData }: Route.ComponentProps) {
  const { backendStatus } = loaderData

  const statusMessage = backendStatus === null
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
