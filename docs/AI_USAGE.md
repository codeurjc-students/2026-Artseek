# AI Tool Usage Log

This document records the use of artificial intelligence tools during the conception and development of Artseek. Related interactions are grouped by purpose so that the work remains traceable without reproducing every individual prompt.

AI-generated material is treated as a design and development aid. All proposals are reviewed, directed, and validated by the student before being incorporated into the project.

## AI-2026-09-12-001 — Application screen drafts and visual iteration

- **Date or period:** 12–16 September 2026.
- **Phase:** UI/UX design and visual prototyping.
- **Objective:** Create the first drafts of the application's screens from a landing-page draft previously designed by the student in Figma, while maintaining a consistent visual identity and translating the product concept into a coherent set of interfaces.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** The student supplied the application description, the colour palette, the self-made Figma landing-page draft, and instructions describing how the remaining screens had been conceived and how they were expected to look and behave.
- **How it was used:** Codex was asked to produce screen drafts based on the supplied visual reference and product requirements. The work followed an iterative, screen-by-screen process: each proposal was reviewed by the student, who requested changes to details, layout, styling, and overall appearance. Codex then revised the draft until the expected result was achieved.
- **Complementary tools and resources:** Figma was used by the student to create the original landing-page draft that served as the primary visual reference. No additional plugins, skills, MCP servers, or external connectors are recorded for this use.
- **Result and student review:** A consistent set of application screen drafts was produced. The student directed the iterations, selected the accepted visual solutions, and validated the final appearance of each screen.

## AI-2026-09-19-001 — Role-aware screen navigation diagram

- **Date:** 19 September 2026.
- **Phase:** Analysis and project documentation.
- **Objective:** Create a screen navigation diagram for inclusion in `README.md` that represented the relationships between the application's screens and the permissions required to follow each navigation path.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** Codex was instructed to derive the navigation structure and access rules from the content of `README.md`, especially Section 7, **Analysis**, including **Screens and navigation**, **Application Screens Drafts**, and **User permissions**. The screen mock-ups stored in `docs/images/` were also provided as the visual source material for the diagram.
- **How it was used:** Codex analysed the documented screens, the transitions between them, and the permissions assigned to anonymous, registered, artist, owner, and administrator contexts. It then organized the screens into a role-aware navigation flow and represented the minimum permission required for each transition through distinct arrow styles and colours.
- **Complementary tools and resources:** Local repository inspection and direct SVG generation were used. The existing PNG screen drafts in `docs/images/` were embedded as visual references in the diagram. No external plugins, MCP servers, or connectors are recorded for this use.
- **Result and student review:** The resulting diagram was saved as `docs/navigation-diagram.svg` and linked from the **Navigation Diagram** subsection of `README.md`. It depicts both the application's screen navigation and the access permissions associated with its transitions.

## AI-2026-09-27-001 — Backend and frontend project setup and connectivity check

- **Date or period:** 27 September–5 October 2026.
- **Phase:** Initial implementation and technical project setup.
- **Objective:** Create the initial backend and frontend projects, configure their respective development environments, and verify that the frontend could communicate successfully with the backend.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** Codex was given the Artseek repository and the project's technical requirements and was asked to establish the initial structure for both application layers and connect them through a minimal end-to-end health check.
- **How it was used:** Codex generated and configured the Spring Boot backend project and the React/TypeScript frontend project. For the backend, it created `backend/pom.xml` and the required `application.properties` files, defining the project metadata, Java and Spring Boot configuration, dependencies, build plugins, server settings, database connection, JPA behaviour, and CORS properties. For the frontend, it created `frontend/vite.config.js`, `frontend/tailwind.config.ts`, and the remaining configuration files required to install, build, type-check, route, style, and run the application, including the package, TypeScript, React Router, and pnpm configuration. The Vite configuration also established the development proxy used to forward `/api` requests to the backend. Codex then created a backend health endpoint and a frontend health page that called that endpoint and displayed whether the backend was reachable.
- **Complementary tools and resources:** Local file creation and editing, terminal commands, Maven, Java, pnpm, Vite, React Router, and Spring Boot were used as part of the setup and verification workflow. No external plugins, MCP servers, or connectors are recorded for this use.
- **Result and student review:** The backend and frontend foundations, their dependency declarations, and their required configuration files were created under `backend/` and `frontend/`. The backend health controller was added at `backend/src/main/java/com/artseek/infrastructure/api/health/HealthController.java`, and the corresponding frontend status page was added at `frontend/app/routes/HealthRoute.tsx`, supported by the backend-status API and query files. The successful health response and status display confirmed that the connection between both projects had been configured correctly.

## AI-2026-10-05-001 — Continuous integration workflows

- **Date or period:** 5–6 October 2026.
- **Phase:** Continuous integration and development quality assurance.
- **Objective:** Create GitHub Actions workflows that automatically validated backend and frontend changes during branch development and before changes were integrated into the `main` branch.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** Codex was given the newly created backend and frontend projects and was asked to establish separate continuous-integration behaviour for ordinary development branches and for pull requests targeting the main branch.
- **How it was used:** Codex created two YAML workflow definitions under `.github/workflows/`, configured their triggers and repository permissions, and defined independent backend and frontend jobs using Ubuntu runners. It also configured the required Java, Maven, Node.js, and pnpm environments, dependency caching, reproducible dependency installation, and the commands used to compile, test, type-check, and build the projects.
- **Development CI workflow:** `.github/workflows/development-ci.yml` runs whenever code is pushed to a branch other than `main`. Its backend job sets up the Temurin Java version declared by the repository, caches Maven dependencies, compiles the backend, and runs the backend unit tests. Its frontend job sets up pnpm and the repository's configured Node.js version, installs dependencies using the lockfile, runs the TypeScript type check, and builds the frontend. This provides fast feedback while a feature or fix is being developed.
- **Pull request quality-assurance workflow:** `.github/workflows/pull-request-qa.yml` runs for pull requests opened, updated, reopened, or marked ready for review against `main`, as well as for pushes to `main`. Its backend job executes the unit, integration, and system test suites separately. Its frontend job performs a frozen-lockfile dependency installation, type-checks the source code, and verifies that the production build completes successfully. This workflow provides the broader validation required before or immediately after integration into the main branch.
- **Complementary tools and resources:** GitHub Actions, the official checkout and Java/Node.js setup actions, the pnpm setup action, Maven, Java, Node.js, and pnpm were used. No GitHub connector, plugin, skill, or MCP server is recorded for this use.
- **Result and student review:** The repository gained automated branch-level and pull-request-level quality checks for both application layers. These workflows made compilation, unit testing, broader backend testing, frontend type checking, and production-build verification repeatable within GitHub.

## AI-2026-10-07-001 — Backend unit, integration, and system tests

- **Date or period:** 7–15 October 2026.
- **Phase:** Backend verification and automated testing.
- **Objective:** Create a comprehensive automated test suite for the backend classes and verify their intended behaviour at unit, integration, and system levels.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** Codex was given the backend implementation and asked to create tests for the classes and functionality developed up to that point. During the iterations, the student specified what each class should be responsible for, which behaviours each test should cover, the relevant success and error cases, and the appropriate boundary between unit, integration, and system testing.
- **How it was used:** Codex inspected the production classes, proposed test cases, generated the test code under `backend/src/test/java/`, ran the relevant Maven test suites, and revised the tests in response to the student's feedback. The generated coverage included domain rules and validation, application services and use cases, repository adapters and persistence behaviour, REST controllers and response contracts, application context and database initialization, and the health endpoint.
- **Unit tests:** Isolated individual classes or narrow web slices from external infrastructure. Mockito mocks and interaction verification were used where collaborators needed to be replaced, while AssertJ and MockMvc assertions checked returned values, validation failures, HTTP status codes, response bodies, and delegation to dependencies.
- **Integration tests:** Verified cooperation between real Spring components and infrastructure boundaries. These tests covered application services with repositories, JPA repository behaviour, REST controller flows, database initialization, persistence queries, mappings, transactions, and Spring application-context startup using controlled test configuration and an isolated test database.
- **System tests:** Started the backend with a real web environment on a random port and exercised the health endpoint through HTTP, confirming that the application could start and respond correctly as a complete running system.
- **Student intervention and review:** The student actively directed the iterations by identifying the responsibility and expected coverage of each class test and by correcting or refining proposed scenarios. The final review checked that each test type followed an appropriate testing approach, that unit tests remained isolated, that integration and system tests exercised the intended boundaries, and that no flaky tests based on timing, execution order, shared state, or uncontrolled external dependencies had been introduced.
- **Complementary tools and resources:** JUnit 5, Spring Boot Test, MockMvc, RestTestClient, Mockito, AssertJ, Maven Surefire, Spring Data JPA test support, and the isolated test database were used. No external plugins, skills, MCP servers, or connectors are recorded for this use.
- **Result and student review:** The backend gained repeatable unit, integration, and system test suites covering the classes and functionality implemented during this phase. The naming conventions also allowed the continuous-integration workflows to run each test category separately.

## AI-2026-10-13-001 — TanStack Query configuration and initial API/hooks pattern

- **Date:** 13 October 2026.
- **Phase:** Frontend architecture and backend communication.
- **Objective:** Configure TanStack Query in the frontend and establish the repository's first reusable pattern for separating API request functions from React data-fetching hooks.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** Codex was asked to integrate TanStack Query into the existing React frontend and to implement an initial API-and-hooks structure that could serve as the convention for subsequent features communicating with the backend.
- **How it was used:** Codex added the `@tanstack/react-query` dependency, created a shared `QueryClient`, exposed it through `QueryClientProvider` in `frontend/app/providers.tsx`, and wrapped the application's route outlet with that provider. It then separated the health-check functionality into a typed API function in `frontend/app/features/backend-status/api/api.ts` and a query hook in `frontend/app/features/backend-status/hooks/hooks.ts`. The API function handled the HTTP request, abort signal, unsuccessful responses, and typed result, while the hook defined the TanStack query key, query function, cancellation propagation, retry behaviour, and query state consumed by the health page.
- **Pattern established:** Feature-specific network operations were placed in an `api/` module, while TanStack Query integration and React-facing state were placed in a `hooks/` module. Components consumed the hook rather than calling `fetch` directly, providing a consistent boundary for server-state loading, success, and error handling that later frontend features could follow.
- **Complementary tools and resources:** TanStack React Query, React, TypeScript, the browser Fetch API, React Router, pnpm, and local file editing were used. No external plugins, skills, MCP servers, or connectors are recorded for this use.
- **Result and student review:** The frontend gained a centralized TanStack Query configuration and its first working API/hooks implementation. The backend health status became the reference example for future server-state features in the repository.

## AI-2026-10-17-001 — OpenAPI generation configuration

- **Date:** 17 October 2026.
- **Phase:** Backend API documentation.
- **Objective:** Add automated OpenAPI specification and documentation generation to the backend.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **How it was used:** Codex updated `backend/pom.xml` with the Springdoc OpenAPI dependency and the Maven plugins required to export the API specification and generate its HTML documentation.
- **Complementary tools and resources:** Springdoc OpenAPI, OpenAPI Generator, Maven, and local file editing were used.
- **Result:** The backend could generate `docs/api/api-docs.yaml` and `docs/api/api-docs.html` through its Maven OpenAPI profile.

## AI-2026-10-25-001 — Frontend data-loading architecture refactor

- **Date:** 25 October 2026.
- **Phase:** Frontend architecture and data loading.
- **Objective:** Refactor the initial API/hooks data-loading pattern into a centralized TanStack Query client combined with React Router client loaders.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-6.1 Sol.
- **Configuration:** `high` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** Codex was given the existing TanStack Query integration, frontend routes, and the architectural rules documented in `frontend/README.md` and `frontend/AGENTS.md`.
- **How it was used:** Codex reorganized frontend data loading around a single shared `QueryClient`. Route-level client loaders became responsible for requesting and caching the data required before rendering, replacing the initial feature API/hooks consumption pattern with a consistent loader-based flow for request execution, cache reuse, navigation, and error handling.
- **Complementary tools and resources:** TanStack Query, React Router client loaders, React, TypeScript, and local file editing were used. No external plugins, skills, MCP servers, or connectors are recorded for this use.
- **Result and student review:** The frontend gained a centralized query-client and client-loader architecture for current and future routes, superseding the initial API/hooks pattern recorded on 13 October 2026.
