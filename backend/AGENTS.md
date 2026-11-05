# Artseek Backend Architecture Guide

## Scope

These instructions apply to the entire `backend/` project. Preserve these decisions unless the user explicitly changes them.

Artseek uses a pragmatic **DDD Lite + Hexagonal Architecture**. Keep business concepts and use-case orchestration clear, while accepting limited Spring coupling where it substantially reduces boilerplate for this degree project.

## Dependency Direction

Keep dependencies pointing inward:

```text
Infrastructure adapters
    -> Application use cases
        -> Domain

Controller
    -> Use-case input port
        <- Application service
            -> Domain repository port
                <- Infrastructure repository adapter
                    -> Mapper + Spring Data JDBC (schema from Flyway)
```

Code in the domain or application layers must never import controllers, repository adapters, or Spring Data repositories. Controllers must not inject repositories directly.

`src/test/java/com/artseek/HexagonalArchitectureUnitTests` enforces these boundaries with ArchUnit: the domain depends only on `java..` and `com.artseek.domain..`; no class uses `jakarta.persistence` or `org.hibernate`; persistence models, mappers and Spring Data repositories are used only inside their own `infrastructure/<aggregate>/repository` package; the application imports nothing from infrastructure; controllers do not depend on handlers or repositories; and one aggregate's infrastructure does not depend on another aggregate's infrastructure.

## Directory and Package Architecture

Use the following structure:

```text
src/main/java/com/artseek/
  domain/
    shared/                         Cross-aggregate domain types (DomainValidationException)
    <aggregate>/                    Entities, value objects, and enums
      exceptions/                   Aggregate-related domain exceptions
      repository/                   Repository interfaces (outbound ports)

  application/
    usecase/<domain-area>/
      <SharedDTO>.java              Reusable application output DTOs
      <feature>/
        <Feature>UseCase.java       Inbound port
        <Feature>Service.java       Use-case implementation

  infrastructure/
    <aggregate>/
      api/                          REST controllers for the aggregate
      repository/                   Adapter, persistence model, mapper and Spring Data repository
      exceptions/                   Only when an infrastructure exception exists
    api/                            Cross-cutting HTTP concerns (health/)
    bootstrap/                      Application startup and sample-data initialization
    config/                         Spring/framework configuration
    mediator/                       SpringMediator and registration errors
```

Domain and infrastructure code are organized by aggregate (currently `category` and `user`). Cross-cutting infrastructure (`config/`, `mediator/`, `bootstrap/`, top-level `api/`, and the future `security/` and `config/security/`) stays at the top level of `infrastructure/`.

Organize application code by feature/vertical slice. For example:

```text
application/usecase/category/
  CategoryDTO.java
  categorytype/
    CategoryTypeDTO.java
    findusedtypes/
      FindUsedCategoryTypesUseCase.java
      FindUsedCategoryTypesService.java
  findbytype/
    FindCategoriesByTypeUseCase.java
    FindCategoriesByTypeService.java
```

## Domain Layer

### The domain is persistence-ignorant

Domain classes are plain Java: no `jakarta.persistence`, `org.hibernate` or Spring annotations, and no constructors that exist only for a framework. The table shape and the conversion to and from it live in infrastructure (see [Repository Ports and Persistence Adapters](#repository-ports-and-persistence-adapters)).

- Business logic remains inside the aggregate and must not depend on repositories, controllers, HTTP, or Spring Data.
- Do not turn entities into anemic public data structures. Avoid public setters; expose meaningful domain operations such as `rename` and `changeType`. Make fields `final` unless a domain operation changes them.
- Provide public constructors or creation factories that enforce domain invariants for new aggregates (`new Category(name, type)`, `User.register(...)`).
- Provide a `restore(...)` factory per aggregate that rebuilds an existing aggregate from persisted values (`Category.restore(id, name, type)`, `User.restore(id, username, email, passwordHash, role)`). It re-runs validation through the value objects and validators, so stored data that breaks a current rule fails when loaded. When a rule becomes stricter, ship a Flyway data migration in the same PR.
- Validation lives in the type that owns the rule: value objects validate in their constructors, and the aggregate validates its other fields with explicit `validateX` methods. Do not use `Objects.requireNonNull` or generic null-check helpers.
- The domain has no timestamp fields; `created_at` and `updated_at` are owned by the database.

### Entity identity

Entities have persistent identity even if attributes change. `Category` is an entity because it has an independent lifecycle and can be referenced by many artworks. Ids are `Long` values generated by the database: a new aggregate has a `null` id until its first save, and saving an aggregate with a non-null id updates its row.

### Closed business vocabularies

Use Java enums when the domain defines a closed set of values. `CategoryType` is the Java equivalent of the desired string-enum behavior:

- Enum constants provide type safety.
- Each constant owns a stable lowercase public value such as `"painting"`.
- Parse external strings through a domain method such as `CategoryType.fromValue`.
- Store enums by their constant name (`PAINTING`), never ordinal values. Only the infrastructure mapper converts them, with `name()` and `valueOf(...)`.
- Do not invent enum values without a confirmed business requirement.

### Domain exceptions

Use aggregate-specific exceptions under `domain/<aggregate>/exceptions/`, extending `domain/shared/DomainValidationException`, instead of leaking generic `NullPointerException` or `IllegalArgumentException` for domain validation failures.

Domain exceptions should:

- Have a stable, fixed message exposed as `MESSAGE` when appropriate.
- Store the rejected input as structured data for logging, debugging, and error analysis.
- Provide an accessor for that rejected value.
- Be translated to HTTP semantics only at the infrastructure/API boundary.

Examples are `InvalidCategoryNameException` and `InvalidCategoryTypeException`.

## Repository Ports and Persistence Adapters

Repository interfaces belong in `domain/<aggregate>/repository` and use domain types. They are outbound ports describing what the core needs, not how data is stored.

Persistence implementations belong in `infrastructure/<aggregate>/repository/`:

```text
domain/category/repository/
  CategoryRepository                  Domain port: domain types only

infrastructure/category/repository/
  CategoryRepositoryAdapter           @Repository implementing the port; the only user of the three below
  CategoryPersistenceModel            Package-private @Table record: the table row, primitive columns only
  CategoryMapper                      Package-private: toPersistence(Category) / toDomain(CategoryPersistenceModel)
  SpringDataCategoryRepository        Package-private ListCrudRepository<CategoryPersistenceModel, Long>
```

`User` follows the same layout (`UserPersistenceModel`, `UserMapper`, `SpringDataUserRepository`, `UserRepositoryAdapter`). `CategoryRepositoryAdapter` is the accepted name. A technology-specific name such as `JdbcCategoryRepositoryAdapter` is only needed if multiple persistence technologies create ambiguity.

Application services depend only on repository ports. Persistence models, mappers and Spring Data interfaces never leave their `repository` package. Spring resolves the adapter because it is a `@Repository` implementing the domain interface.

- **Persistence model:** a record with Spring Data annotations only (`@Table`, `@Id`, `@ReadOnlyProperty`, and `@Column` only when default naming does not fit), never `jakarta.persistence`. It carries no lengths, constraints or defaults; the migration is the source of truth. `created_at`/`updated_at` are `@ReadOnlyProperty`, so they are read but never written. Constraint-name constants (`UserPersistenceModel.USERNAME_CONSTRAINT`) live here, not in the domain.
- **Mapper:** the only place that converts value objects to and from strings and enums to and from stored names. `toPersistence` passes `null` timestamps; `toDomain` rebuilds through `restore(...)`.
- **Spring Data repository:** prefer derived queries; write SQL with `@Query` and bound parameters only when a derived query cannot express the operation. For example, used category types come from `SELECT DISTINCT type FROM categories ORDER BY type`.
- **Adapter:** maps arguments to stored values, delegates, maps results back, and always returns the result of `save()`/`saveAll()`, which carries the generated id.

Command handlers always call `repository.save(...)` after mutating an aggregate and use the returned instance. There is no dirty checking.

## Database Schema and Flyway Migrations

Hand-written Flyway migrations in `src/main/resources/db/migration/` are the single source of truth for the schema, in production and tests. Flyway only applies them; nothing is generated from code, and Spring Data JDBC does not validate the schema at startup.

1. Write a new `V<n>__<description>.sql`. Never edit a migration once it is merged.
2. Update the persistence model, mapper and adapter.
3. Run the tests. Flyway applies every migration to a fresh Testcontainers database, and the adapter integration tests catch mismatches between model and schema.

Ids are `BIGINT GENERATED ALWAYS AS IDENTITY`. Tables have `created_at`/`updated_at TIMESTAMPTZ NOT NULL DEFAULT now()` and a `trg_<table>_set_updated_at` trigger calling the shared `set_updated_at()` function, which refreshes `updated_at` only when the row actually changed.

Name database objects explicitly with the prefix convention `<type>_<table>_<columns>`, in snake_case, at most 63 characters (the PostgreSQL limit):

| Object | Pattern | Example |
| --- | --- | --- |
| Primary key | `pk_<table>` | `pk_categories` |
| Unique | `uq_<table>_<columns>` | `uq_categories_name_type`, `uq_users_email` |
| Foreign key | `fk_<table>_<column>_<referenced_table>` | `fk_artworks_author_id_users` |
| Index | `ix_<table>_<columns>` | `ix_categories_type` |
| Check | `ck_<table>_<description>` | `ck_users_role_valid` |
| Trigger | `trg_<table>_<action>` | `trg_categories_set_updated_at` |
| Function | `<verb>_<object>` | `set_updated_at` |

## Database Initialization

Keep sample-data startup logic in `infrastructure/bootstrap/DatabaseInitializer`. This is an infrastructure concern because it depends on the Spring application lifecycle and writes to persistence.

The accepted initialization approach is:

- Register `DatabaseInitializer` as a Spring `@Service`.
- Restrict it to the `local` Spring profile with `@Profile("local")`; sample data must not be loaded during ordinary tests or production startup.
- Inject domain repository ports through constructor injection.
- Trigger initialization with `@PostConstruct` after Spring has supplied its dependencies.
- Build domain entities through their validated public constructors.
- Persist batches through a `saveAll` operation on the domain repository port; the infrastructure adapter maps and delegates to Spring Data JDBC.
- Make initialization idempotent by checking the natural identifying attributes before inserting each sample. Restarting the application must not create another copy of the same sample row.
- Add future entities' sample data to this centralized bootstrap component, or split it into focused bootstrap collaborators if it grows substantially.

Start the application locally with `mvn spring-boot:run "-Dspring-boot.run.profiles=local"`. Ordinary integration tests do not activate `local`. The dedicated initializer integration test constructs `DatabaseInitializer` with the real repository port and calls `init()` inside its `@Transactional` test, ensuring all inserted sample rows are rolled back afterward. This verifies initializer behavior and persistence integration, while automatic `@PostConstruct` execution remains enabled only during an actual `local` application startup.

Distinguish these concepts:

- **Supported category types:** every value the domain enum supports, regardless of stored data. A future `FindSupportedCategoryTypesUseCase` should read `CategoryType.values()` and should not query the repository.
- **Used category types:** types represented by at least one persisted category. `FindUsedCategoryTypesUseCase` queries `CategoryRepository.findUsedTypes()`.

## Application Layer and Use Cases

Use explicit use-case input ports and application services rather than a custom mediator.

Each business operation normally has:

```text
<Feature>UseCase       Interface consumed by inbound adapters
<Feature>Service       Implementation that orchestrates the operation
```

Controllers inject use-case interfaces only. They must not inject repositories, Spring Data interfaces, or concrete services.

Application services are responsible for:

1. Accepting application-level input.
2. Converting/validating it through domain types.
3. Loading entities through repository ports.
4. Invoking domain behavior.
5. Persisting through repository ports when required.
6. Mapping results to application DTOs.

Complex business rules belong in domain entities/value objects, not application services.

### Spring annotations remain in application services

Keep use-case implementations in the application layer even though they use `@Service` and `@Transactional`.

This is an intentional pragmatic decision:

- A class belongs to a layer because of its responsibility, not merely because it has a Spring annotation.
- Use-case orchestration is application behavior and must not be moved into infrastructure.
- `@Service` provides simple dependency injection.
- `@Transactional` defines the use-case transaction boundary; use `readOnly = true` for read operations.
- This introduces limited Spring coupling but avoids infrastructure bean configuration and transaction-decorator boilerplate.
- Services must still be directly constructible and unit-testable without starting Spring.

Do not move application services into infrastructure merely to group Spring annotations together.

### DTO policy

Never expose domain aggregates directly from the API.

Create reusable application DTO records at the nearest common application package when multiple current or future use cases can share them. Examples:

- `application/usecase/category/CategoryDTO`
- `application/usecase/category/categorytype/CategoryTypeDTO`

DTO rules:

- Prefer transport-safe basic values such as `String`, `Long`, `Boolean`, and time primitives.
- Convert domain enums to their stable string values; do not expose `CategoryType` in a DTO.
- Supply focused mapping factories such as `CategoryDTO.from(Category)`.
- Return immutable collections (`Stream.toList()`, `List.copyOf`, etc.).
- Name a DTO after the shared concept, not one particular use case, when reuse is expected.

Current `CategoryDTO` contains `Long id`, `String name`, and `String type`. `CategoryTypeDTO` contains the public string value and is shared by used/supported type use cases.

## Infrastructure/API Layer

Aggregate controllers belong in `infrastructure/<aggregate>/api` (cross-cutting HTTP such as health stays in `infrastructure/api`) because they adapt HTTP requests/responses to application use cases and depend heavily on Spring MVC.

Controller rules:

- Inject only use-case interfaces through constructor injection.
- Do not inject repositories, Spring Data interfaces, or application service implementations directly.
- Do not return domain entities.
- Return application DTOs.
- Always return a `ResponseEntity` to make HTTP status and headers explicit.
- Use a typed response such as `ResponseEntity<List<CategoryDTO>>` when the body type is known.
- Use `ResponseEntity<?>` only when a stable body type is genuinely not defined.
- Prefer `ResponseEntity` builders such as `ResponseEntity.ok(body)`, `ResponseEntity.status(...).body(...)`, and `ResponseEntity.badRequest().build()`.
- Translate domain exceptions into semantic HTTP status codes at the HTTP boundary. For example, an unsupported category type maps to `400 Bad Request`.
- Do not leak exception stack traces or domain objects in response bodies.

The current category routes are:

```text
GET /api/categories/types/used    Used category types
GET /api/categories/{type}        Categories filtered by type
```

`/{type}` is used instead of a query on `/api/categories`. Preserve static routes such as `/types/used` alongside it. If a future category-by-ID route would conflict with `/{type}`, introduce an explicit path that removes the ambiguity.

Local exception handling in a controller is acceptable for the current scope. If error handling becomes repetitive, introduce a shared `@RestControllerAdvice` with consistent error DTOs rather than duplicating `try/catch` blocks.

## Tests

Keep separate `<Class>UnitTests` and `<Class>IntegrationTests` classes in matching packages under `src/test/java`.

System tests use the suffix `SystemTests` and mirror their controller's production package under `src/test/java`, alongside its other tests. For example, `HealthEndpointSystemTests` belongs in `com/artseek/infrastructure/api/health`, matching `HealthController` under `src/main/java`.

Shared abstract bases live under `src/test/java/com/artseek/shared`:

- `IntegrationTest` imports `PostgresTestConfiguration` only. It does not declare `@SpringBootTest`, `@DataJdbcTest` or `@Transactional`.
- `JdbcIntegrationTest extends IntegrationTest` declares `@DataJdbcTest` and `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`. It provides a focused Spring Data JDBC slice: Flyway builds the schema and each test rolls back.
- `FullContextIntegrationTest extends IntegrationTest` declares `@SpringBootTest` and `@Transactional`. It loads the full application in the default mock web environment and rolls back test-thread writes after each test. Use it for full-context integration tests, including read-only application tests; it does not start a listening HTTP server.
- `SystemTest extends IntegrationTest` declares `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` and `@AutoConfigureRestTestClient`, and exposes the injected, protected `RestTestClient restTestClient`. It does not declare `@Transactional` because HTTP server writes use separate transactions.

Concrete test classes must follow this inheritance and annotation policy:

| Test category | Extend | Additional annotations and setup on the concrete class |
| --- | --- | --- |
| Domain unit tests | No shared Spring base | Plain JUnit `@Test`; construct entities directly. |
| Handler / application-service unit tests | No shared Spring base | `@ExtendWith(MockitoExtension.class)`, mocked repository ports and direct construction or `@InjectMocks`. |
| Repository adapter unit tests | No shared Spring base | `@ExtendWith(MockitoExtension.class)`; mock Spring Data with persistence models and verify the mapping in both directions. |
| Mapper unit tests | No shared Spring base | Plain JUnit `@Test`; `toPersistence`/`toDomain`, enum names, value-object conversion and invalid stored values failing through `restore`. |
| Mediator unit tests | No shared Spring base | Plain JUnit `@Test`; construct directly with explicit sample-handler lists and no Spring context. |
| Controller web-slice tests (`*UnitTests`) | No shared Spring base | `@WebMvcTest(Controller.class)`, injected MockMvc and `@MockitoBean` for every constructor dependency; stub/verify the mediator where used. |
| Handler / application-service integration tests | `JdbcIntegrationTest` | `@Import` the handler/service and required real repository adapters. |
| Repository adapter integration tests | `JdbcIntegrationTest` | `@Import` the real adapter being tested; save and reload every field, since these tests are the schema check. |
| Controller integration tests | `FullContextIntegrationTest` | **Required:** `@AutoConfigureMockMvc`; real mediator, handlers, adapters and persistence. Full-context and transaction annotations are inherited. |
| Application / bootstrap integration tests | `FullContextIntegrationTest` | JUnit `@Test` methods and injected collaborators; inherit full-context configuration and rollback even for read-only tests. |
| HTTP system tests | `SystemTest` | JUnit `@Test` methods using the inherited `restTestClient`; explicit cleanup for committed fixtures and server-side writes. |

Concrete JDBC-slice/full-context/system tests inherit their Spring test annotations from their base; do not duplicate them or combine `@DataJdbcTest` with `@SpringBootTest`. Keep focused imports on concrete JDBC-slice tests. All concrete classes declare their own `@Test` methods.

Choose scope by the collaboration being verified. `JdbcIntegrationTest` loads Spring Data JDBC infrastructure, Flyway and the Spring Data repositories, plus explicitly imported handlers/services and adapters: test mappings, queries, database-backed filtering, generated IDs and DTO mapping by invoking the handler/adapter directly. It does not automatically load controllers, the mediator or unrelated application beans. `FullContextIntegrationTest` loads the complete application and verifies real dependency wiring or behavior across layers; with MockMvc, controller tests exercise routing, validation, serialization and the real controller/mediator/handler/adapter/database path without starting an HTTP server. Application/bootstrap tests can instead assert context wiring or initializer behavior. A full context makes beans available; each test's entry point and assertions still determine its actual coverage. Both bases use PostgreSQL and test-transaction rollback. `SystemTest` adds real HTTP transport and requires explicit cleanup of committed writes.

- Field `@Autowired` is acceptable in Spring tests: the test framework owns their lifecycle and dependency setup. Production classes use constructor injection with `final` fields to make required dependencies explicit and allow plain Java construction. `@Autowired` itself is not forbidden; a single production constructor does not need the annotation.
- Handler unit tests cover query/command type registration, DTO mapping, empty results, validation, rejected-input exception data, and absence of repository calls for invalid input. Verify immutable snapshots where that is part of the output contract.
- Handler integration tests verify persistence-backed behavior: generated IDs in DTOs, filtering, distinct/order semantics, and exclusion of supported-but-unused types. Controller tests verify routing, JSON, delegation, and semantic HTTP errors.
- Use controlled test databases and explicit fixtures, never the development/production database. Exact result assertions are appropriate; do not assume generated IDs or unspecified ordering.
- Keep container declarations exclusively in `src/test/java/com/artseek/infrastructure/config/PostgresTestConfiguration`. The `shared.IntegrationTest` base imports this configuration; concrete database tests inherit it through `JdbcIntegrationTest`, `FullContextIntegrationTest` or `SystemTest`, as specified above. Plain unit and controller web-slice tests must not extend any of these bases. Use test-scoped `spring-boot-testcontainers` and `testcontainers-postgresql` dependencies managed by Spring Boot.
- Use the pinned `postgres:17.11` image, matching the development PostgreSQL 17 engine and inspected local image version, with a separate `artseek_test` database and dynamically allocated host ports. `@ServiceConnection` supplies connection details automatically. Do not connect tests to the development database, bind port 5432, mount its volume or fall back to an embedded database.
- Every JDBC slice must extend `JdbcIntegrationTest` to inherit `@DataJdbcTest` and disabled datasource replacement, and retain its focused handler/adapter imports. Full-context/controller/system tests inherit the container configuration through their appropriate base while retaining real application wiring.
- Let Spring manage the container bean lifecycle: one container per cached context, shared by compatible tests, stopped only after its dependent datasource beans are destroyed. Do not use JUnit `@Container` lifecycle management, per-method containers or persistent container reuse. Different context configurations receive separate databases, each migrated from empty by Flyway.
- Tests get their schema from the same Flyway migrations as production; never add schema generation or test-only DDL. Docker is required for database tests; missing Docker must fail clearly rather than skip tests. Plain unit and web-slice tests must run without Docker or database access.
- `JdbcIntegrationTest` inherits rollback through `@DataJdbcTest`; `FullContextIntegrationTest` provides it through `@Transactional`. This covers same-thread writes, including synchronous MockMvc requests. Startup hooks execute before the test transaction. Keep `local` disabled and test the initializer by constructing it and calling `init()` manually within the inherited rollback transaction.
- Sample mediator requests/handlers must be private nested types without Spring annotations. Explicit test registries exclude production handlers; test fixtures must never register with the real mediator.
- HTTP system tests extend `SystemTest`, following `HealthEndpointSystemTests`, and use the inherited `restTestClient` through the running HTTP server. Keep these tests in the same relative package as their controller. Do not add `@DataJdbcTest` or rely on test-level `@Transactional` to roll back HTTP writes: server-side writes run in another thread with their own transactions. Commit fixture setup before making HTTP requests. For mutating system tests, clean up committed fixtures and server writes after each test, even after assertion failures, using focused `@AfterEach` repository cleanup or `@Sql(executionPhase = AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))` cleanup scripts. Remove dependent rows as needed and delete only test-owned data. Read-only health system tests need no cleanup.

Run the complete backend suite after code/test changes:

```powershell
mvn test
```

The project targets the Java version declared in the repository-root `.java-version` file (currently Java 25 LTS). Keep Maven, CI, and local `JAVA_HOME` aligned with that file before running Maven.

For unit and controller web-slice tests without Docker, run `mvn test "-Dtest=*UnitTests" "-Dsurefire.failIfNoSpecifiedTests=true"`. With Docker running, `mvn --batch-mode --no-transfer-progress verify` runs the complete suite and generates JaCoCo reports. The PR/main workflow checks `docker info` before verification; the development workflow runs only the Docker-independent group. See `README.md` for fixture ownership, HTTP cleanup, lifecycle and isolation checks.

## Current Architectural Decisions Summary

- DDD Lite plus hexagonal architecture.
- The domain is plain Java; each aggregate's persistence model, mapper and Spring Data JDBC repository stay inside `infrastructure/<aggregate>/repository/`.
- Aggregates are rebuilt with `restore(...)`, which re-runs validation; handlers persist explicitly with `save(...)`.
- Business invariants remain in the domain.
- Domain repository interfaces are outbound ports; Spring Data JDBC implementations are infrastructure adapters.
- The schema comes only from hand-written Flyway migrations, with database-owned timestamps and the `<type>_<table>_<columns>` naming convention.
- Explicit use-case ports and application services are used instead of a mediator.
- Application services stay in the application layer and may use `@Service`/`@Transactional`.
- Controllers depend only on use-case interfaces.
- Application DTOs protect the HTTP API from entities and domain-specific types.
- Shared DTOs are preferred where reuse is expected.
- Controllers always return `ResponseEntity` and translate domain failures to HTTP statuses.
- Controllers require both focused web-layer tests and real integration coverage.
- Sample data is initialized centrally and idempotently through an infrastructure `@Service` using `@Profile("local")`, `@PostConstruct`, and repository-port `saveAll`.
- Integration tests use a controlled test database; the initializer test invokes `init()` manually within its rollback transaction.
