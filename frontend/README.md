# Artseek frontend

React SPA built with Vite, React Router framework mode, TanStack Query, and Tailwind CSS.

The source follows a feature-driven structure under `app/features`. Backend state
belongs in TanStack Query hooks; add a client-state store only when shared UI state
actually requires one.

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
