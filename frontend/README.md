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

## Unit tests

From `frontend/`, install the locked dependencies and run the complete suite once:

```powershell
pnpm install --frozen-lockfile
pnpm test
```

`pnpm test` runs `vitest run` without watch mode and exits with a nonzero status
if any test fails. No backend, database, or development server is needed.
For watch mode during development, use `pnpm test:watch`. To run a subset:

```powershell
pnpm test test/routes
pnpm test test/features/categories
```

Tests use [Vitest](https://vitest.dev/guide/),
[React Testing Library](https://testing-library.com/docs/react-testing-library/intro/),
and jsdom. Keep test files under `test/`, mirroring the source paths under `app/`:

```text
app/features/categories/api/category-api.ts
test/features/categories/api/category-api.test.ts
app/features/categories/queries/category-queries.ts
test/features/categories/queries/category-queries.test.ts
app/routes/CategoryRoute.tsx
test/routes/CategoryRoute.test.tsx
app/root.tsx
test/root.test.tsx
```

Use `.test.ts` for services and query configuration and `.test.tsx` for rendered
behavior. Import `describe`, `it`, and `expect` from `vitest`. Add reusable DTO
fixtures to `test/fixtures.ts`. Configure requests through `mockFetch()` from
`test/test-utils.ts`, for example:

```ts
mockFetch().mockResolvedValue(Response.json([]))
await expect(fetchCategoriesByType('painting')).resolves.toEqual([])
```

`test/setup.ts` replaces `fetch` before every test, so requests never reach a
real backend. It also resets the shared QueryClient, cleans up rendered DOM,
and removes retry delays while preserving feature retry counts. Vitest restores
mocks and stubbed globals between tests. Return a new `Response` for each request
when testing repeated JSON reads (`mockImplementation` or chained
`mockResolvedValueOnce` calls), because response bodies can only be read once.

Exercise real query-option factories and client loaders, then render pages with
their returned `loaderData`; no query hooks or QueryClientProvider are needed.
The route-props helpers in `test/routes/route-fixtures.ts` supply generated router
props. Use `deferred<Response>()` to keep a request pending deterministically.
Shared hydration/loading UI and loader error propagation are covered by real
in-memory navigation in `test/root.test.tsx`. Health accepts a successful empty
HTTP response; an empty-list state only applies to category collections.

For new functionality, cover successful results, pending UI, empty results where
applicable, and HTTP/network failures. Prefer accessible-role and visible-text
assertions over implementation details or snapshots.

Both GitHub Actions workflows run `pnpm test` alongside TypeScript checks and a
production build. `development-ci.yml` runs on feature-branch pushes;
`pull-request-qa.yml` runs on pull requests targeting `main` and pushes to `main`.
The PR job retains the exact name **Frontend test suite**, which is required by
the repository's active `main` ruleset. A failed test fails that job and blocks
merging. Keep that required check enabled if repository rules are changed.
