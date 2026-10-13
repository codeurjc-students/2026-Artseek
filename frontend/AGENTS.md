# Frontend Architecture & Stack Strategy: Artseek

This document outlines the architectural decisions for the Artseek frontend, transitioning from a legacy layer-based structure to a modern, Feature-Driven approach using TanStack Query.

## 1. The Core Stack

*   **Framework:** React + Vite
*   **Routing:** React Router (SPA navigation)
*   **Server State Management:** TanStack Query (React Query)
*   **Client State Management:** Zustand, only when shared client state is needed
*   **Styling:** Tailwind CSS

## 2. Server State vs. Client State

In previous architectures, global stores (like Redux or raw Zustand) were used to hold data fetched from the API. This is now considered an anti-pattern. We divide the state into two distinct categories:

### TanStack Query (Server State)
Used strictly for asynchronous operations and data that lives on the backend (e.g., `Artworks`, `Artist Profiles`, `Search Results`).
*   **What we get:** Automatic caching, request deduplication, background re-fetching, and built-in `isLoading` / `isError` flags without writing a single `useState` or `useEffect`.
*   **Impact on Artseek:** Searching via embeddings is resource-intensive. Query caches the results of a multimodal search so if a user navigates away and back, the results load instantly from the cache without hitting the pgvector database again.

#### QueryOptions Contract

Follow the [TanStack Query `QueryOptions` reference](https://tanstack.com/query/latest/docs/framework/react/reference/interfaces/QueryOptions) when defining feature queries:

*   Every query needs a stable `queryKey`. TanStack hashes the key for caching and updates the query when the key changes, so include every input that changes the requested server data.
*   The `queryFn` must resolve to defined data or throw an error. It must never resolve to `undefined`; return a meaningful result object or `null` when a successful endpoint has no response body.
*   Pass the `AbortSignal` from `QueryFunctionContext` to the underlying request so TanStack Query can cancel work that is no longer needed.
*   Configure `retry` deliberately for the endpoint. The client default is three retries and the server default is zero; use fewer retries for fast availability checks and avoid retrying errors that cannot recover automatically.
*   `gcTime` controls how long inactive cached data remains in memory. Its browser default is five minutes. Change it only when the feature's data size or navigation behavior justifies a different lifetime.
*   `initialData` is persisted in the cache and is stale by default unless paired with an appropriate `staleTime`. Do not use fabricated initial data merely to bypass pending UI.
*   Keep query options such as retries, cache lifetime, network mode, and metadata in the feature's `hooks/` layer rather than scattering them through routes and components.

### Zustand (Client State)
Used strictly for synchronous, shared UI state that only exists in the user's browser. Do not create a store for local component state or until a feature genuinely needs one.
*   **Examples:** The current JWT token, the open/closed state of the "Upload Artwork" modal, or the user's selected layout preference (Grid vs. Masonry).

## 3. Feature-Driven Architecture

Instead of organizing files by type (`/routes`, `/services`, `/stores`), we group them by domain features. This makes the codebase scalable and prevents context-switching.

```text
app/                        # React Router framework-mode source root
├── root.tsx                # Global providers and document shell
├── app.css                 # Generic styles and Tailwind entry point
├── components/             # Shared UI components (Buttons, Inputs, Spinners)
├── lib/                    # Shared third-party setup, added only when needed
├── features/               # FEATURE MODULES
│   ├── auth/               
│   │   ├── api/
│   │   │   └── auth-api.ts # Cohesive API auth operations (login, register)
│   │   ├── hooks/  # TanStack Query hooks for the feature
│   │   │   └── auth-hooks.ts
│   │   ├── components/     # LoginForm, RegisterForm
│   │   └── store.ts        # Zustand store (JWT, currentUser)
│   │
│   ├── search/             
│   │   ├── api/
│   │   │   └── artwork-search-api.ts
│   │   ├── hooks/
│   │   │   └── artwork-search-hooks.ts
│   │   ├── model/
│   │   │   └── SearchResultDTO.ts
│   │   └── components/     # SearchBar, ResultGrid
│   │
│   └── portfolio/          
│       ├── api/
│       │   └── portfolio-api.ts
│       ├── hooks/
│       │   └── portfolio-hooks.ts # usePortfolioQuery(), useCreateArtworkMutation()
│       └── components/     
│
└── routes/                 # Lazy-loaded page components that import from features/
    ├── HomeRoute.tsx
    ├── SearchRoute.tsx
    └── ArtistProfileRoute.tsx
```

## 4. API and Query Hook Design

Keep API operations and their TanStack Query hooks inside the feature that owns them. Every feature that communicates with the backend has an `api/` folder, and every feature exposing TanStack Query hooks has a `hooks/` folder. Group files by cohesive resource or workflow: neither one catch-all file per feature nor one file per operation is the default.

### Responsibilities

*   **`api/`:** Contains transport-level functions, request and response types that are private to the transport, response validation, and HTTP error handling. Its files must not depend on React or TanStack Query. A function used as a query function must resolve to defined data on success; return a meaningful result object or `null` for an empty response, never `undefined` or `void`.
*   **`hooks/`:** Contains query keys, query options, and exported TanStack Query hooks. Its files call functions from `api/` and own cache behavior such as retries, stale times, and invalidation.
*   **`model/`:** Contains the feature's API data contracts. Every object received from or sent to the API must have a named model here instead of an inline object type in API, hook, route, or component code.
*   **Components and routes:** Consume hooks. They must not call `fetch` directly or duplicate query keys.

Do not create global `api/` or `hooks/` directories containing unrelated domains. Cross-cutting HTTP configuration may live in `app/lib` once it is genuinely shared.

### Naming Rules

*   Name feature directories in kebab-case: `backend-status`, `artist-profile`.
*   Name API and hook files after their cohesive resource or workflow using `<resource>-api.ts` and `<resource>-hooks.ts`: `api/category-type-api.ts` and `hooks/category-type-hooks.ts`.
*   Name API model files and exported types after the backend contract in PascalCase, preserving the `DTO` suffix when present: `model/CategoryTypeDTO.ts` exports `CategoryTypeDTO`.
*   Name API functions with an operation verb and domain object: `fetchBackendStatus`, `searchArtworks`, `createArtwork`, `updateArtistProfile`, `deleteArtwork`.
*   Name read hooks `use<Domain>Query`: `useBackendStatusQuery`, `usePortfolioQuery`.
*   Name write hooks `use<Operation><Domain>Mutation`: `useCreateArtworkMutation`, `useDeleteArtworkMutation`.
*   Name query-key factories `<domain>Keys`, for example `portfolioKeys`, when a feature has multiple related queries. A single inline key is acceptable for a feature with only one query.
*   Name React component files and exports in PascalCase. Keep non-component TypeScript filenames lowercase and kebab-case, except for the conventional `store.ts` file and PascalCase DTO model files.

### When to Split Files

Start with one API file and one hooks file for each cohesive resource or workflow. Related operations such as `fetchUsedCategoryTypes` and `fetchSupportedCategoryTypes` belong together in `category-type-api.ts`; their query keys and hooks belong together in `category-type-hooks.ts`. This keeps cache identity, invalidation, and related behavior discoverable in one place.

Split a resource file when it develops distinct subdomains, substantially different workflows, or independent ownership. A single operation may get its own file when it has meaningful standalone complexity, supporting types, or specialized cache behavior. Do not create one file per function or hook merely for consistency, and do not use file length alone as the reason to split.

This grouping rule applies to behavior. DTO models remain one named file per API contract because they are stable, independently imported data definitions.

API models should be simple TypeScript `interface` or `type` declarations composed of primitives, arrays, and other named models. Do not use classes, methods, client state, or behavior in DTO models. Keep transport validation and mapping in `api/`.

Choose feature boundaries by cohesive product domain, not one feature per backend DTO. Closely related contracts with limited independent behavior belong together; for example, `CategoryTypeDTO` belongs in `features/categories/model/` because category types are part of the categories domain and do not justify a separate feature.

Add a `store.ts` only for shared client state; server responses remain in TanStack Query.
