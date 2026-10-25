# Artseek frontend

React SPA built with Vite, React Router framework mode, TanStack Query, and Tailwind CSS.

The source follows a feature-driven structure under `app/features`. Client loaders
prepare essential route data through the shared TanStack Query client, and pages
render the returned `loaderData`. Feature `queries/` modules define cache keys and
query options; no query subscription hooks or provider are needed. Category lists
stay fresh for one minute; health is checked on every route visit. Add a
client-state store only when shared UI state requires one.

## Requirements

- Node.js 22+
- pnpm 10+

## Run

```powershell
pnpm install
pnpm dev
```

Open the URL printed by the development server. During development, Vite proxies
`/api` requests to the backend target configured in `vite.config.js`.

## Verify

```powershell
pnpm test
pnpm typecheck
pnpm build
```

The tests exercise real route loaders and rendered pages with mocked HTTP
responses, covering cache reuse, category route parameters, loading failures, and
health recovery without requiring the backend.
