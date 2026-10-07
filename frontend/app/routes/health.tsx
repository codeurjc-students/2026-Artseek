import { useEffect, useState } from 'react'

import type { Route } from './+types/health'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Artseek' },
    { name: 'description', content: 'Discover art with Artseek' },
  ]
}

export default function Home() {
  const [backendStatus, setBackendStatus] = useState('Connecting to the backend…')

  useEffect(() => {
    const controller = new AbortController()

    fetch('/api/health', { signal: controller.signal })
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        return response.json() as Promise<{ message: string }>
      })
      .then((status) => setBackendStatus(status.message))
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setBackendStatus('Backend unavailable')
        }
      })

    return () => controller.abort()
  }, [])

  return (
    <main className="home-page">
      <p className="eyebrow">Explore · Connect · Create</p>
      <h1>Artseek</h1>
      <p>{backendStatus}</p>
    </main>
  )
}
