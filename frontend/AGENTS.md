# Frontend Architecture & Stack Strategy: Artseek

This document defines the Artseek frontend standard: feature-driven modules, React Router client loaders for essential route data, and an imperative TanStack Query cache. Route components render loaderData.

## 1. The Core Stack

*   **Framework:** React + Vite
*   **Routing and Route Data:** React Router framework mode (SPA navigation and `clientLoader`)
*   **Server State Management:** TanStack Query (React Query)
*   **Client State Management:** Zustand, only when shared client state is needed
*   **Component Library:** React-Bootstrap with Bootstrap 5
*   **Styling:** Tailwind CSS

## 2. Route Loading, Server State, and Actions

React Router coordinates route loading and rendering. TanStack Query provides a cache behind client loaders. Pages render the typed `loaderData` returned by their loader; they do not subscribe to the query cache.

### Responsibilities and Usage

| Tool | When to use it | Responsibility |
| --- | --- | --- |
| `clientLoader` | Backend data needed by a route | Read route inputs, await the QueryClient, and return typed loader data |
| `QueryClient` | Cache-aware requests and cache invalidation | Reuse fresh responses, deduplicate concurrent requests, and fetch stale or missing data |
| Feature `queries/` modules | Resource-specific query configuration | Define plain query-option factories, keys, freshness, and retries |
| Route components | Display a loaded page | Render `loaderData` from generated `Route.ComponentProps` |
| `useActionState` | User submissions and local validation or confirmation results | Own the submission result and pending state; call feature API functions |
| Local React state / Zustand | Browser-only UI state | Keep state local by default; add Zustand only when shared UI state needs it |

### Shared QueryClient and Query Options

*   Define and export one QueryClient for the browser application in `app/lib/query-client.ts`. All client loaders use that instance. Do not create a client per loader, component, or navigation. If runtime SSR is introduced later, scope server clients per request.
*   Define options with TanStack's `queryOptions` helper in feature `queries/<resource>-queries.ts` modules. Export plain factories such as `categoriesByTypeQueryOptions(type)`; these are configuration functions, not React hooks.
*   Keep keys, query functions, freshness, and retry policies in those factories. Loaders call `await queryClient.query(options)` using the installed TanStack API, then return the result as loader data.
*   Keep HTTP calls, response validation, and DTO mapping in feature `api/` modules.
*   Do not introduce `useQuery`, `useSuspenseQuery`, or wrapper query hooks for these pages. A `QueryClientProvider` is unnecessary for imperative QueryClient calls, so do not add a provider just for loaders.
*   Add a `hooks/` folder only for actual custom React hooks required by a feature. Plain query options belong in `queries/`.

### Client Loaders and Route Components

*   Export `clientLoader` from routes needing backend data. Use generated `Route.ClientLoaderArgs` when consuming loader arguments, and `Route.ComponentProps` for component props. This project uses SPA mode (`ssr: false`), so these requests run in the browser.
*   Read path variables from `params`: `category/:categoryType` supplies `params.categoryType`. Read query strings from `new URL(request.url).searchParams`. Include relevant inputs in cache keys.
*   Return meaningful named data such as `{ categories }`; render it using `const { categories } = loaderData`. Do not populate the cache and then independently fetch or subscribe to the same data in the component.
*   Use `HydrateFallback` for initial loading and `useNavigation` for pending navigation. Use only the shared `ErrorBoundary` in `app/root.tsx`; do not add route-specific boundaries unless explicitly requested. Loader failures propagate to the root boundary. Preserve explicit diagnostic outcomes such as the health page returning `null` for an unavailable backend.
*   `loaderData` is a snapshot from the latest loader execution. Cache changes alone do not update a mounted page. There is no automatic background refresh, focus refetching, or polling with this design; navigation or explicit router revalidation must run the loader again.
*   Pass the query function's `AbortSignal` to `fetch`. Router navigation cancellation and cached request cancellation are separate lifecycles; do not indiscriminately cancel requests shared by concurrent loaders.

For example, returning to a category page runs its client loader again. If its cached response is still fresh, the loader returns it without another HTTP request. If missing, stale, or invalidated, the loader awaits a request and returns the new data. Category queries currently use a one-minute `staleTime`; health uses zero to check every visit.

See [React Router client data loading](https://reactrouter.com/start/framework/data-loading), [TanStack router integration](https://tanstack.com/query/latest/docs/framework/react/guides/prefetching), and [query options](https://tanstack.com/query/latest/docs/framework/react/guides/query-options).

### QueryOptions Contract

*   Use stable keys containing every input that changes the requested server data.
*   Query functions must return defined data or throw. For successful empty responses, return a meaningful result or `null`, never `undefined` or `void`.
*   Set `retry` deliberately: imperative `queryClient.query` disables retries unless configured. Avoid retrying failures that cannot recover automatically.
*   Set `staleTime` for each resource. Cached data does not guarantee request-free navigation after it becomes stale or invalidated.
*   `gcTime` controls cache retention, independently of freshness. Its browser default is five minutes. Without query observers, cache entries can become inactive even while their page is displayed; eviction does not remove already-returned loader data.
*   Do not fabricate `initialData` to bypass route loading. Keep query configuration in feature `queries/` modules rather than duplicating it in routes.

Reference: [TanStack Query `QueryOptions`](https://tanstack.com/query/latest/docs/framework/react/reference/interfaces/QueryOptions).

### Submissions and `useActionState`

Use `useActionState` for submission pending state, returned validation errors, and confirmation messages. Actions call feature API functions. Repeated actions are queued sequentially; this hook does not provide a response cache.

If a successful write changes cached resources, invalidate or update the affected entries through the shared QueryClient. Keep resource-specific invalidation logic in the feature's `queries/` module. To refresh a currently displayed page, explicitly revalidate its loader or navigate to the relevant route after invalidation. Router revalidation alone may return fresh cached data; cache invalidation alone does not update `loaderData`.

Choose one owner for a submission's pending/error state; do not add a TanStack mutation hook alongside `useActionState` for the same request. Query subscription and mutation hooks are not required by this architecture.

See the [React `useActionState` reference](https://react.dev/reference/react/useActionState) and [TanStack cache invalidation](https://tanstack.com/query/latest/docs/framework/react/guides/query-invalidation).

### Browser-Only UI State

Keep local UI state in React. Introduce Zustand only when multiple components need shared browser-only state, such as layout preferences. Do not mirror API responses or loader data in a UI store.

## 3. Feature-Driven Architecture

Group files by product domain. Category types belong to the categories feature because their behavior and contracts are closely related.

```text
app/
├── root.tsx                # Document shell, hydration fallback, navigation UI
├── app.css                 # Global styles and Tailwind entry point
├── components/             # Shared UI components, added when needed
├── lib/
│   └── query-client.ts     # Shared imperative QueryClient
├── features/
│   ├── backend-status/
│   │   ├── api/
│   │   └── queries/
│   │       └── backend-status-queries.ts
│   └── categories/
│       ├── api/
│       │   ├── category-api.ts
│       │   └── category-type-api.ts
│       ├── queries/
│       │   ├── category-queries.ts
│       │   └── category-type-queries.ts
│       └── model/
│           ├── CategoryDTO.ts
│           └── CategoryTypeDTO.ts
└── routes/                 # Client loaders, components rendering loaderData, errors
    ├── HomeRoute.tsx
    ├── CategoryRoute.tsx
    └── HealthRoute.tsx
```

## 4. API, Query, and Model Design

Keep API operations and cache configuration inside their owning feature. Group behavioral files by cohesive resource or workflow, rather than one catch-all file per feature or one file per operation.

### Responsibilities

*   **`api/`:** HTTP functions, validation, error handling, and transport mapping. No React, React Router, or TanStack dependencies.
*   **`queries/`:** Plain query-option factories, cache keys, policies, and resource-specific invalidation helpers. No component subscriptions or routing dependencies.
*   **`model/`:** Named API data contracts. Every object received from or sent to the API must have a model here instead of an inline object type in API, loader, or component code.
*   **Route loaders:** Call the shared QueryClient with feature query options and return data.
*   **Route components:** Render typed `loaderData`. Submission actions may call feature APIs and cache helpers. Do not call `fetch` directly or duplicate query keys.

Do not create global `api/` or `queries/` folders containing unrelated domains. Shared HTTP configuration may live in `app/lib` when needed.

### Naming Rules

*   Feature directories use kebab-case: `backend-status`, `categories`.
*   API files use `<resource>-api.ts`; query files use `<resource>-queries.ts`, for example `category-type-api.ts` and `category-type-queries.ts`.
*   Option factories use `<operationOrResource>QueryOptions`: `usedCategoryTypesQueryOptions`, `categoriesByTypeQueryOptions`.
*   API functions use an operation verb and domain object: `fetchUsedCategoryTypes`, `fetchCategoriesByType`, `createArtwork`.
*   When multiple queries need a key factory, name it `<domain>Keys`. A single key inside an option factory is sufficient for one query.
*   API model files and exports match backend contracts in PascalCase, preserving `DTO`: `model/CategoryTypeDTO.ts` exports `CategoryTypeDTO`.
*   React component files and exports use PascalCase. Other TypeScript filenames use lowercase kebab-case, except DTO model files and conventional `store.ts`.

### Cohesion and Models

Related operations such as fetching used and supported category types belong together in `category-type-api.ts`; their options and keys belong together in `category-type-queries.ts`. Split by resource or workflow when complexity or independent ownership warrants it. A complex operation may have its own file, but neither file length nor one-function-per-file consistency is a reason to split.

DTO models remain one named file per contract. Prefer simple TypeScript interfaces or types composed of primitives, arrays, and other named models. Do not introduce classes, methods, or client state in DTOs. Keep validation and mapping in `api/`.

Add a `store.ts` only for shared client state; fetched resources remain in the query cache and route loader data.

## 5. React-Bootstrap and Navigation

Use React-Bootstrap components for standard interface elements such as lists, buttons, alerts, forms, navigation, and loading indicators instead of rebuilding equivalent components with raw markup. Import Bootstrap's stylesheet once in `app/root.tsx`; feature and route files must not import it again.

React-Bootstrap does not replace React Router. Use React Router's `Link` for internal SPA navigation so navigation does not reload the document. When a Bootstrap component should navigate, compose it with `Link` through its `as` prop:

```tsx
import { ListGroup } from 'react-bootstrap'
import { Link } from 'react-router'

<ListGroup.Item as={Link} to="/category/painting" action>
  Painting
</ListGroup.Item>
```

Use ordinary anchors only for external destinations, downloads, or protocols handled outside the SPA. Keep routing concerns in routes and navigation components; feature API and query modules must not depend on React Router or React-Bootstrap.

## 6. Unit Testing

Use Vitest with React Testing Library, jest-dom matchers, and jsdom. Run commands from `frontend/`:

```powershell
pnpm test          # Complete suite once; exits nonzero on failure
pnpm test:watch    # Watch mode for development
pnpm typecheck
pnpm build
```

### Test Structure and Naming

*   Keep unit tests under `test/`, mirroring the source directories under `app/`. For example, `app/features/categories/api/category-api.ts` is tested by `test/features/categories/api/category-api.test.ts`, and `app/routes/CategoryRoute.tsx` by `test/routes/CategoryRoute.test.tsx`.
*   Use `.test.ts` for API and query tests and `.test.tsx` for rendered behavior. Shared root behavior belongs in `test/root.test.tsx`.
*   Keep global setup and cleanup in `test/setup.ts`. Keep cross-cutting helpers, such as mocked fetch access and deferred promises, in `test/test-utils.ts`.

### Fixtures and Route Helpers

Organize shared test data by cohesion and reuse. These are project conventions; a separate fixture file for every model is not required.

*   Keep `test/fixtures.ts` while it contains only a few small, related collections. Avoid turning it into a repository-wide collection of unrelated feature data.
*   As fixtures grow across features, move reusable model data into the mirrored feature's `model/` directory under `test/`. Use kebab-case `<model-name>-fixtures.ts`, for example `test/features/categories/model/category-fixtures.ts` and `category-type-fixtures.ts`.
*   Create model fixture files only when tests need reusable examples. Keep unusual or single-test inputs inline. Type shared fixtures with the application's model contracts, and create independent copies when a test needs to mutate data.
*   Route prop and loader-argument builders belong under `test/routes/`. Prefer `route-test-utils.ts` for these helpers when creating or reorganizing them; the existing `route-fixtures.ts` currently serves this purpose.
*   Keep small, related route builders together. Split into route-specific helper files when their setup develops substantial independent behavior, rather than automatically creating one file per route.

### Coverage and Isolation

*   Exercise the existing architecture: API functions, query-option factories, client loaders, and components rendering returned `loaderData`. Do not introduce query subscription hooks or a `QueryClientProvider` for loader-based tests.
*   Cover successful data loading and display, empty collections where applicable, HTTP/network failures, and pending UI. Test initial hydration, pending navigation, and loader error propagation at the shared root. Preserve diagnostic behavior such as the health loader returning `null` on failure.
*   Verify meaningful query behavior such as cache reuse, parameter-specific keys, stale or invalidated data, retries, and cancellation where relevant.
*   Mock requests so tests run without a backend, database, or development server. Use `mockFetch()` and `deferred()` from `test/test-utils.ts` for deterministic responses and pending states.
*   Preserve the setup's per-test fetch replacement, shared QueryClient reset, DOM cleanup, and mock/global restoration. Retry delays may be removed for speed while preserving feature retry counts.
*   Return a fresh `Response` for each request that reads JSON; response bodies can only be consumed once. Avoid tests that depend on execution order or data left by another test.
*   Prefer accessible-role and visible-text assertions. Add tests for observable behavior and meaningful regressions rather than mirroring implementation details or relying on snapshots alone.

Both frontend CI jobs must run `pnpm test` alongside type checking and production building. Preserve the PR job's exact name, **Frontend test suite**, because the active `main` ruleset requires it for merging. See [the frontend testing guide](README.md#unit-tests) for examples and focused test commands.
