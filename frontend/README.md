# Artseek frontend

React SPA built with Vite, React Router framework mode, React-Bootstrap, and Tailwind CSS.

The source follows a feature-driven structure under `app/features`. Client loaders
prepare essential route data by calling feature API services, and pages render
the returned `loaderData`. Services use the shared native-fetch wrapper in
`app/utils/api-request.ts` for request options, response parsing, and HTTP errors.
Every loader execution requests current data and forwards `request.signal` to
fetch. Navigation and explicit router revalidation refresh route data. Keep
browser-only state in React; add a store only when shared UI state requires one.

```text
clientLoader -> feature API service -> utils/api-request -> fetch -> REST API
clientLoader result -> loaderData -> route component
```

The generic `apiRequest<T>(path, init)` wrapper accepts native `RequestInit` options,
including headers, any HTTP method, body, and cancellation signal. It copies
headers, preserves supplied options, and defaults string bodies to
`Content-Type: application/json` only when no content type is supplied. Multipart
bodies retain their browser-generated content type. JSON is parsed once; a
successful empty response returns `undefined` (use `void` for no-content operations).
Unsuccessful responses throw `HttpError` with HTTP `status` and `statusText`,
independently of the error body's format. Network, cancellation, and JSON parsing
errors propagate.

Feature services accept domain inputs and an optional cancellation signal; they
construct the wrapper's method, headers, body, and other transport options
internally. Callers do not pass generic fetch options to feature APIs. Services
use their DTO return types and retain runtime response validation. Category
endpoints require arrays (including `[]`); health accepts a successful empty response.

Initial loading uses the root `HydrateFallback`; pending navigations use
`useNavigation`. Loader errors reach the shared root error boundary. The health
loader returns `backendStatus: null` for an unavailable backend and propagates
cancellation. Refresh displayed data with `useRevalidator().revalidate()` or
navigation after a successful write. Requests run once per loader execution;
there is no application response cache, automatic retry, polling, or focus refresh.

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
responses, covering request options and cancellation, category route parameters,
fresh data on reload, loading failures, and health recovery without a backend.

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
app/utils/api-request.ts
test/utils/api-request.test.ts
app/routes/CategoryRoute.tsx
test/routes/CategoryRoute.test.tsx
```

Use `.test.ts` for utilities and services and `.test.tsx` for rendered
behavior. Import `describe`, `it`, and `expect` from `vitest`. Add reusable DTO
fixtures to `test/fixtures.ts`. Configure requests through `mockFetch()` from
`test/test-utils.ts`, for example:

```ts
mockFetch().mockResolvedValue(Response.json([]))
await expect(fetchCategoriesByType('painting')).resolves.toEqual([])
```

`test/setup.ts` replaces `fetch` before every test, so requests never reach a
real backend. It also cleans up rendered DOM. Vitest restores
mocks and stubbed globals between tests. Return a new `Response` for each request
when testing repeated JSON reads (`mockImplementation` or chained
`mockResolvedValueOnce` calls), because response bodies can only be read once.

Exercise the shared wrapper, feature services, and client loaders, then render
pages with their returned `loaderData`.
The helpers in `test/routes/route-test-utils.ts` supply generated router
props. Use `deferred<Response>()` to keep a request pending deterministically.
Health accepts a successful empty HTTP response; an empty-list state only
applies to category collections.

For new functionality, cover successful results, pending UI, empty results where
applicable, and HTTP/network failures. Prefer accessible-role and visible-text
assertions over implementation details or snapshots.

Both GitHub Actions workflows run `pnpm test` alongside TypeScript checks and a
production build. `development-ci.yml` runs on feature-branch pushes;
`pull-request-qa.yml` runs on pull requests targeting `main` and pushes to `main`.
The PR job retains the exact name **Frontend test suite**, which is required by
the repository's active `main` ruleset. A failed test fails that job and blocks
merging. Keep that required check enabled if repository rules are changed.
