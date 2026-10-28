# Frontend Architecture & Stack Strategy: Artseek

This document defines the Artseek frontend standard: feature-driven modules, React Router client loaders for essential route data, and a shared native-fetch wrapper. Route components render loaderData.

## 1. The Core Stack

*   **Framework:** React + Vite
*   **Routing and Route Data:** React Router framework mode (SPA navigation and `clientLoader`)
*   **API Requests:** Application-owned fetch wrapper under `app/utils/`
*   **Client State Management:** Zustand, only when shared client state is needed
*   **Component Library:** React-Bootstrap with Bootstrap 5
*   **Styling:** Tailwind CSS

## 2. Route Loading, Server State, and Actions

React Router coordinates route loading, cancellation, revalidation, and rendering. Client loaders call feature API services, which use the shared fetch wrapper. Pages render the typed `loaderData` returned by their loader.

### Responsibilities and Usage

| Tool | When to use it | Responsibility |
| --- | --- | --- |
| `clientLoader` | Backend data needed by a route | Read route inputs, pass `request.signal` to feature APIs, and return typed loader data |
| Feature `api/` modules | Resource-specific API operations | Define endpoints, validate response shapes, and map DTOs |
| `utils/api-request.ts` | Shared HTTP transport | Preserve request options, parse responses, and reject unsuccessful HTTP responses |
| Route components | Display a loaded page | Render `loaderData` from generated `Route.ComponentProps` |
| `useActionState` | User submissions and local validation or confirmation results | Own the submission result and pending state; call feature API functions |
| Local React state / Zustand | Browser-only UI state | Keep state local by default; add Zustand only when shared UI state needs it |

### Shared Fetch Wrapper

*   All feature API services call `apiRequest<T>` from `app/utils/api-request.ts`, using their DTO result types. Keep native `fetch` calls and common HTTP status checks and parsing in that utility.
*   The wrapper accepts a path and native `RequestInit` options, including headers, any HTTP method, body, credentials, cache options, and cancellation signal. Preserve caller-supplied values. Normalize a copy of the headers and default string bodies to JSON only when no content type is supplied. Leave multipart content types to the browser.
*   Feature API functions accept domain inputs and an optional cancellation signal, and construct request options internally. Do not expose generic `RequestInit` arguments on feature APIs. Serialize JSON bodies in the feature service.
*   Parse JSON and return `undefined` for a successful empty body. Use `void` for no-content operations such as health checks. Check HTTP status before parsing.
*   Unsuccessful responses throw `HttpError` containing HTTP status and status text without assuming an error-body format. Propagate network, cancellation, and JSON parsing failures unchanged.
*   Keep resource endpoints, runtime response validation, and DTO mapping in feature `api/` modules. Generic types do not validate network responses. Category collections require an array; an absent body is not an empty collection.
*   Add a `hooks/` folder only for actual custom React hooks required by a feature.

### Client Loaders and Route Components

*   Export `clientLoader` from routes needing backend data. Use generated `Route.ClientLoaderArgs` when consuming loader arguments, and `Route.ComponentProps` for component props. This project uses SPA mode (`ssr: false`), so these requests run in the browser.
*   Read path variables from `params`: `category/:categoryType` supplies `params.categoryType`. Read query strings from `new URL(request.url).searchParams`. Pass relevant inputs to feature API functions.
*   Return meaningful named data such as `{ categories }`; render it using `const { categories } = loaderData`. Do not independently request the same route data in the component.
*   Use `HydrateFallback` for initial loading and `useNavigation` for pending navigation. Use only the shared `ErrorBoundary` in `app/root.tsx`; do not add route-specific boundaries unless explicitly requested. Loader failures propagate to the root boundary. Preserve explicit diagnostic outcomes such as the health page returning `null` for an unavailable backend.
*   `loaderData` is a snapshot from the latest loader execution. Navigation or explicit router revalidation runs the loader again and requests current backend data. There is no application response cache, automatic retry, background refresh, focus refetching, or polling.
*   Pass `request.signal` through the feature API to the shared wrapper. Let React Router cancel interrupted loads and discard stale navigation results. Cancellation must propagate; the health loader must not report an aborted request as an unavailable backend.

For example, returning to a category page runs its client loader again and makes a new request. Calling `useRevalidator().revalidate()` refreshes the currently displayed route. The health page also checks availability on every loader execution and accepts a successful empty HTTP response.

See [React Router client data loading](https://reactrouter.com/start/framework/data-loading) and [race conditions](https://reactrouter.com/explanation/race-conditions).

### Submissions and `useActionState`

Use `useActionState` for submission pending state, returned validation errors, and confirmation messages. Actions call feature API functions. Repeated actions are queued sequentially; this hook does not provide a response cache.

After a successful write changes displayed resources, explicitly revalidate the route loader or navigate to the relevant route. The loader requests current data from the backend.

Choose one owner for a submission's pending/error state. Keep local interaction state in React rather than duplicating submission state across hooks.

See the [React `useActionState` reference](https://react.dev/reference/react/useActionState).

### Browser-Only UI State

Keep local UI state in React. Introduce Zustand only when multiple components need shared browser-only state, such as layout preferences. Do not mirror API responses or loader data in a UI store.

## 3. Feature-Driven Architecture

Group files by product domain. Category types belong to the categories feature because their behavior and contracts are closely related.

```text
app/
├── root.tsx                # Document shell, hydration fallback, navigation UI
├── app.css                 # Global styles and Tailwind entry point
├── components/             # Shared UI components, added when needed
├── utils/
│   └── api-request.ts        # Shared fetch wrapper, response parsing, HTTP errors
├── features/
│   ├── backend-status/
│   │   ├── api/
│   │   └── model/
│   │       └── BackendStatus.ts
│   └── categories/
│       ├── api/
│       │   ├── category-api.ts
│       │   └── category-type-api.ts
│       └── model/
│           ├── CategoryDTO.ts
│           └── CategoryTypeDTO.ts
└── routes/                 # Client loaders, components rendering loaderData, errors
    ├── HomeRoute.tsx
    ├── CategoryRoute.tsx
    └── HealthRoute.tsx
```

## 4. API, Utility, and Model Design

Keep resource-specific API operations inside their owning feature and shared transport under `utils/`. Group behavioral files by cohesive resource or workflow, rather than one catch-all file per feature or one file per operation.

### Responsibilities

*   **`api/`:** Endpoints, resource-specific validation, and DTO mapping through the shared wrapper. No React or routing dependencies, duplicated fetch calls, status checks, or JSON parsing.
*   **`utils/`:** Shared transport, response parsing, and HTTP errors. No feature or routing dependencies.
*   **`model/`:** Named API data contracts. Every object received from or sent to the API must have a model here instead of an inline object type in API, loader, or component code.
*   **Route loaders:** Call feature API functions with route inputs and `request.signal`, then return data.
*   **Route components:** Render typed `loaderData`. Submission actions may call feature APIs and revalidate the router after a successful write. Do not call `fetch` directly.

Do not create a global `api/` folder containing unrelated domains. Shared HTTP configuration belongs in `app/utils`.

### Naming Rules

*   Feature directories use kebab-case: `backend-status`, `categories`.
*   API files use `<resource>-api.ts`, for example `category-type-api.ts`.
*   API functions use an operation verb and domain object: `fetchUsedCategoryTypes`, `fetchCategoriesByType`, `createArtwork`.
*   API model files and exports match backend contracts in PascalCase, preserving `DTO`: `model/CategoryTypeDTO.ts` exports `CategoryTypeDTO`.
*   React component files and exports use PascalCase. Other TypeScript filenames use lowercase kebab-case, except DTO model files and conventional `store.ts`.

### Cohesion and Models

Related operations such as fetching used and supported category types belong together in `category-type-api.ts`. Split by resource or workflow when complexity or independent ownership warrants it. A complex operation may have its own file, but neither file length nor one-function-per-file consistency is a reason to split.

DTO models remain one named file per contract. Prefer simple TypeScript interfaces or types composed of primitives, arrays, and other named models. Do not introduce classes, methods, or client state in DTOs. Keep validation and mapping in `api/`.

Add a `store.ts` only for shared client state; fetched resources remain in route loader data.

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

Use ordinary anchors only for external destinations, downloads, or protocols handled outside the SPA. Keep routing concerns in routes and navigation components; feature APIs and shared utilities must not depend on React Router or React-Bootstrap.

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
*   Use `.test.ts` for utility and API tests and `.test.tsx` for rendered route behavior.
*   Keep global setup and cleanup in `test/setup.ts`. Keep cross-cutting helpers, such as mocked fetch access and deferred promises, in `test/test-utils.ts`.

### Fixtures and Route Helpers

Organize shared test data by cohesion and reuse. These are project conventions; a separate fixture file for every model is not required.

*   Keep `test/fixtures.ts` while it contains only a few small, related collections. Avoid turning it into a repository-wide collection of unrelated feature data.
*   As fixtures grow across features, move reusable model data into the mirrored feature's `model/` directory under `test/`. Use kebab-case `<model-name>-fixtures.ts`, for example `test/features/categories/model/category-fixtures.ts` and `category-type-fixtures.ts`.
*   Create model fixture files only when tests need reusable examples. Keep unusual or single-test inputs inline. Type shared fixtures with the application's model contracts, and create independent copies when a test needs to mutate data.
*   Route prop and loader-argument builders belong in `test/routes/route-test-utils.ts`.
*   Keep small, related route builders together. Split into route-specific helper files when their setup develops substantial independent behavior, rather than automatically creating one file per route.

### Coverage and Isolation

*   Exercise the shared fetch wrapper, feature API services, client loaders, and components rendering returned `loaderData`.
*   Cover successful data loading and display, empty collections where applicable, HTTP/network failures, and pending route behavior. Preserve diagnostic behavior such as the health loader returning `null` on failure.
*   Verify request option preservation, JSON and empty response handling, parameter-specific endpoints, updated data after navigation or revalidation, and cancellation where relevant.
*   Mock requests so tests run without a backend, database, or development server. Use `mockFetch()` and `deferred()` from `test/test-utils.ts` for deterministic responses and pending states.
*   Preserve the setup's per-test fetch replacement, DOM cleanup, and mock/global restoration.
*   Return a fresh `Response` for each request that reads JSON; response bodies can only be consumed once. Avoid tests that depend on execution order or data left by another test.
*   Prefer accessible-role and visible-text assertions. Add tests for observable behavior and meaningful regressions rather than mirroring implementation details or relying on snapshots alone.

Both frontend CI jobs must run `pnpm test` alongside type checking and production building. Preserve the PR job's exact name, **Frontend test suite**, because the active `main` ruleset requires it for merging. See [the frontend testing guide](README.md#unit-tests) for examples and focused test commands.
