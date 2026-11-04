---
name: backend-tester
description: Create, fix, or review Artseek backend tests for domain behavior, CQRS handlers, repository adapters, the mediator, REST controllers, application wiring, bootstrap, and real HTTP requests. Apply the project's test scopes, shared base classes, PostgreSQL isolation, and fixture cleanup rules.
---

# Backend tester

Use this skill when creating, fixing, or reviewing tests under `backend/`. Apply the same conventions to future features; category and health tests are examples, not limits on the skill's scope. Preserve the existing test architecture unless the user changes it. Ask when the expected business behavior or required coverage is unclear rather than inventing it.

Paths below are relative to `backend/` unless stated otherwise. Production Java lives under `src/main/java/com/artseek`, organized by layer and then aggregate (for example `domain/category/`, `infrastructure/user/repository/`); tests mirror the subject's package under `src/test/java/com/artseek`.

## Choose the test scope

Inspect the production behavior, existing tests, and shared bases before changing coverage. Use JUnit Jupiter, AssertJ, Mockito, and the existing Spring Boot test support. Keep separate `<Class>UnitTests` and `<Class>IntegrationTests` classes. Real HTTP tests use the `SystemTests` suffix and mirror their controller's package, alongside its other tests.

| Subject / test category | Required base | Concrete-class setup | Verify |
| --- | --- | --- | --- |
| Domain entities, value objects, enums, and domain validation | None | Plain JUnit; construct domain objects directly. | Invariants, domain operations, valid/invalid inputs, normalization, enum conversion, and domain exception data. |
| Query/command handler or application-service unit tests | None | `@ExtendWith(MockitoExtension.class)`; mock repository ports; direct construction or `@InjectMocks`. | Request type registration, orchestration, input validation, domain interaction, query result/DTO mapping, and command effects. |
| Repository adapter unit tests | None | `@ExtendWith(MockitoExtension.class)`; mock Spring Data. | Delegation, arguments, and returned results. |
| Mediator unit tests | None | Plain JUnit; construct the mediator with explicit sample-handler lists. | Query/command routing and results, empty registries, duplicate registrations, and missing-handler errors. |
| Controller web-slice tests (`*UnitTests`) | None | `@WebMvcTest(Controller.class)`, injected `MockMvc`, and `@MockitoBean` for every constructor dependency. | Routing, request construction, mediator delegation where used, JSON, HTTP statuses, and semantic errors. |
| Query/command handler or application-service integration tests | `JpaIntegrationTest` | `@Import` the handler/service and required real repository adapters. Invoke the handler/service directly. | Database-backed use-case behavior and mapping through real persistence. |
| Repository adapter integration tests | `JpaIntegrationTest` | `@Import` the real adapter being tested. | Entity mappings, generated IDs, queries, filtering, and persistence semantics. |
| Domain persistence integration tests, when persistence behavior needs verification | `JpaIntegrationTest` | Focused persistence setup/imports for the mapping under test. | Persistence-specific behavior that plain domain tests cannot verify. |
| Controller integration tests | `FullContextIntegrationTest` | **Required:** `@AutoConfigureMockMvc`; real mediator, handlers, adapters, and persistence. | Routing, validation, serialization, errors, and the real controller-to-database path. |
| Application/bootstrap integration tests | `FullContextIntegrationTest` | Inject collaborators and declare the class's own JUnit tests. | Real application wiring and initializer behavior, including read-only context tests. |
| Architecture rules (`HexagonalArchitectureUnitTests`) | None | ArchUnit `@AnalyzeClasses(packages = "com.artseek")` with `@ArchTest` rules over production classes. | Layer dependency direction, controller dependencies, and isolation between aggregates' infrastructure. |
| HTTP system tests | `SystemTest` | Use inherited `restTestClient`; set up and clean up committed fixtures when needed. | Actual HTTP transport through a running server. |

Controllers require both focused web-slice tests and real integration coverage. Use system tests when real HTTP transport is the collaboration being verified. Add domain persistence tests only when persistence behavior needs checking; domain business rules stay in plain unit tests.

## Shared bases and inherited annotations

The abstract bases are in `src/test/java/com/artseek/shared`:

| Base | Configuration it owns |
| --- | --- |
| `IntegrationTest` | Imports `PostgresTestConfiguration` only. No `@SpringBootTest`, `@DataJpaTest`, or `@Transactional`. |
| `JpaIntegrationTest extends IntegrationTest` | `@DataJpaTest` and `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`. Focused JPA slice with automatic per-test rollback. |
| `FullContextIntegrationTest extends IntegrationTest` | `@SpringBootTest` and `@Transactional`. Full context in the default mock web environment; per-test rollback of test-thread writes. No listening HTTP server. |
| `SystemTest extends IntegrationTest` | `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` and `@AutoConfigureRestTestClient`. Exposes injected, protected `RestTestClient restTestClient`. No test-level `@Transactional`. |

Concrete database tests extend the appropriate specialized base. Do not extend `IntegrationTest` directly to recreate a test scope. Inherit the Spring scope, database configuration, and transaction annotations; do not duplicate them or combine `@DataJpaTest` with `@SpringBootTest`. Keep focused `@Import` declarations on concrete JPA tests. Each concrete class declares its own test methods and fixtures.

Plain unit and controller web-slice tests extend none of these bases and must run without Docker or database access. Field `@Autowired` is acceptable in Spring tests because the test framework owns their lifecycle. Production collaborators use constructor injection with `final` fields.

Choose the base for the collaboration being verified:

- A JPA slice loads persistence infrastructure and repositories plus explicitly imported handlers/services and adapters. It does not automatically load controllers, the mediator, or unrelated application beans.
- A full-context test verifies real wiring and behavior across layers. With MockMvc it can exercise controller -> mediator -> handler -> adapter -> PostgreSQL synchronously, without starting a listening HTTP server.
- A system test sends real HTTP requests through a running server. Server-side writes use separate threads and transactions.

Loading a full context makes beans available; the test's entry point and assertions determine its actual coverage.

## Behavior to cover

### Domain and application

- Construct domain objects directly to test invariants and meaningful operations. Test entity-specific validation exceptions, their fixed `MESSAGE` where provided, and accessors containing the rejected input.
- Handler unit tests verify `queryType()` or `commandType()` returns the matching request class, successful orchestration, and validation through domain types. Query tests cover DTO mapping and empty results where applicable.
- Commands should in principle produce no result. With the existing generic contracts, use `Command<Void>` / `CommandHandler<C, Void>` and return `null`. Command tests verify domain operations, repository writes, and persisted state rather than expecting a returned DTO or generated ID. Ask the user before testing a new result-returning command contract as accepted behavior.
- Verify invalid input is rejected before repository access. Check the rejected-input exception data as well as the exception type/message.
- Verify immutable collections or snapshots when they form part of the output contract.
- Handler integration tests exercise the real repository port and adapter, checking generated IDs in query DTOs, filtering, distinct/order semantics, and persisted state after commands as applicable to the operation.
- For closed vocabularies, distinguish all supported enum values from values actually represented in persisted rows. Used-value queries exclude supported-but-unused values; supported-value operations read the enum without persistence access.

### Persistence, mediator, and HTTP

- Adapter unit tests mock Spring Data and verify delegation. Adapter integration tests use real PostgreSQL to verify mappings and query behavior, including enum names rather than ordinals.
- Mediator fixtures must be private nested request/handler types without Spring annotations. Construct explicit test registries containing only sample handlers; fixtures must never register with the real application mediator.
- Web-slice tests mock the mediator or other constructor dependencies and verify request mapping, JSON, delegation, and semantic HTTP errors. Do not replace the real collaborators in controller integration tests.
- System tests follow `HealthEndpointSystemTests` and use the inherited `restTestClient` against the actual server. Keep their package aligned with the controller.

## PostgreSQL isolation and lifecycle

Keep container declarations exclusively in `src/test/java/com/artseek/infrastructure/config/PostgresTestConfiguration`. The shared `IntegrationTest` imports it; specialized bases provide the chosen scope. Use test-scoped `spring-boot-testcontainers` and `testcontainers-postgresql`, with versions managed by Spring Boot.

Preserve these project constraints:

- Use pinned image `postgres:17.11`, matching the development PostgreSQL 17 engine and the inspected image version, with separate database `artseek_test` and dynamically allocated host ports.
- Let `@ServiceConnection` supply connection details. Never connect tests to development/production databases, bind host port 5432, mount the development volume, or fall back to an embedded database.
- Spring owns the container bean lifecycle: one container per cached context, shared by compatible tests, stopped after dependent JPA/datasource beans are destroyed. Different context configurations receive separate databases to avoid `create-drop` interference.
- Do not add JUnit `@Container` lifecycle management, per-method containers, or persistent container reuse.
- Preserve test Hibernate `create-drop` in `src/test/resources/application.properties` and leave production datasource/schema settings unchanged.
- Docker is required for database tests. Missing Docker must fail clearly rather than skip tests. Unit/web-slice tests stay independent of Docker.

## Fixtures, rollback, and cleanup

Tests arrange their own explicit fixtures in controlled test databases. Do not depend on execution order, sample startup data, fixed generated IDs, or unspecified result ordering. Assert exact results when the contract defines them; compare without ordering when it does not.

`JpaIntegrationTest` inherits rollback from `@DataJpaTest`; `FullContextIntegrationTest` supplies it through `@Transactional`. This covers same-thread writes, including synchronous MockMvc requests, even in read-only tests.

Startup hooks execute before the test transaction. Keep the `local` profile disabled in ordinary tests. Test `DatabaseInitializer` by constructing it with real repository ports and calling `init()` manually inside a `FullContextIntegrationTest` method so inserted sample rows roll back. Verify sample insertion and idempotence when testing or changing that behavior; automatic `@PostConstruct` execution belongs to actual `local` startup.

For mutating HTTP system tests:

1. Commit fixture setup before the HTTP request so the server can see it.
2. Do not add `@DataJpaTest` or rely on test-level `@Transactional`: it cannot roll back server-side writes.
3. Clean up committed fixtures and server writes after every test, including assertion failures. Use focused `@AfterEach` repository cleanup or `@Sql(executionPhase = AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))` cleanup scripts.
4. Delete only test-owned data and remove dependent rows as needed. Read-only health system tests need no cleanup.

## Run and review

Run Maven from `backend/`. Align local `JAVA_HOME`, Maven, and CI with the repository-root `.java-version` before invoking Maven; `pom.xml` declares the project's Java major version.

After backend code/test changes, run the complete suite:

```powershell
mvn test
```

For the Docker-independent unit and controller web-slice group:

```powershell
mvn test "-Dtest=*UnitTests" "-Dsurefire.failIfNoSpecifiedTests=true"
```

With Docker running, check `docker info` and run full verification with coverage:

```powershell
mvn --batch-mode --no-transfer-progress verify
```

`verify` runs the complete suite and generates JaCoCo HTML/XML reports under `target/site/jacoco/`; `mvn test` does not generate those reports. PR/main CI checks Docker before full verification; development CI runs the Docker-independent group. Report what was actually run and any environment blocker; a unit-only run does not establish that database/system tests pass.

When reviewing existing tests, check the matrix, assertions, fixtures, and transaction boundaries against the actual behavior. For runtime architecture changes, also read [backend-architect](../backend-architect/SKILL.md). Consult `backend/README.md` from the repository root for detailed commands, fixture ownership, lifecycle, and isolation checks.
