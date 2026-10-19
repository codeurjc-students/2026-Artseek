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
                    -> Spring Data JPA
```

Code in the domain or application layers must never import controllers, repository adapters, or Spring Data repositories. Controllers must not inject repositories directly.

## Directory and Package Architecture

Use the following structure:

```text
src/main/java/com/artseek/
  domain/
    model/<entity>/                 Entities, value objects, and enums
    exceptions/<entity>/            Entity-related domain exceptions
    repository/                     Repository interfaces (outbound ports)

  application/
    usecase/<domain-area>/
      <SharedDTO>.java              Reusable application output DTOs
      <feature>/
        <Feature>UseCase.java       Inbound port
        <Feature>Service.java       Use-case implementation

  infrastructure/
    api/                            REST controllers and HTTP concerns
    bootstrap/                      Application startup and sample-data initialization
    config/                         Spring/framework configuration
    repository/<entity>/            Persistence adapters and Spring Data repositories
```

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

### JPA entities are an intentional compromise

Domain entities may use Spring/Jakarta persistence annotations such as `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, and `@Enumerated`. This avoids maintaining duplicate domain and persistence models in a degree project.

Trade-offs:

- The domain is not completely persistence-framework independent.
- Persistence mapping is simpler and avoids repetitive mapper code.
- Business logic must still remain inside the entity and must not depend on repositories, controllers, HTTP, or Spring Data.
- Do not turn entities into anemic public data structures. Avoid public setters; expose meaningful domain operations such as `rename` and `changeType`.
- Provide the protected no-argument constructor required by JPA and public constructors/factories that enforce domain invariants.

### Entity identity

Entities have persistent identity even if attributes change. `Category` is an entity because it has an independent lifecycle and can be referenced by many artworks. A newly constructed JPA entity may have a null generated identifier until it is persisted.

### Closed business vocabularies

Use Java enums when the domain defines a closed set of values. `CategoryType` is the Java equivalent of the desired string-enum behavior:

- Enum constants provide type safety.
- Each constant owns a stable lowercase public value such as `"painting"`.
- Parse external strings through a domain method such as `CategoryType.fromValue`.
- Persist enums with `@Enumerated(EnumType.STRING)`, never ordinal values.
- Do not invent enum values without a confirmed business requirement.

### Domain exceptions

Use entity-specific exceptions under `domain/exceptions/<entity>/` instead of leaking generic `NullPointerException` or `IllegalArgumentException` for domain validation failures.

Domain exceptions should:

- Have a stable, fixed message exposed as `MESSAGE` when appropriate.
- Store the rejected input as structured data for logging, debugging, and error analysis.
- Provide an accessor for that rejected value.
- Be translated to HTTP semantics only at the infrastructure/API boundary.

Examples are `InvalidCategoryNameException` and `InvalidCategoryTypeException`.

## Repository Ports and Persistence Adapters

Repository interfaces belong in `domain/repository` and use domain types. They are outbound ports describing what the core needs, not how data is stored.

Persistence implementations belong in `infrastructure/repository/<entity>/`:

```text
CategoryRepository                  Domain port
CategoryRepositoryAdapter           Hexagonal persistence adapter
SpringDataCategoryRepository        Spring Data/JPA mechanism
```

`CategoryRepositoryAdapter` is the accepted name. A technology-specific name such as `JpaCategoryRepositoryAdapter` is only needed if multiple persistence technologies create ambiguity.

Application services depend only on repository ports. They must never import `CategoryRepositoryAdapter` or `SpringDataCategoryRepository`. Spring resolves the adapter because it is a `@Repository` implementing the domain interface.

Use explicit JPQL where derived-query naming cannot clearly express the operation. For example, used category types are obtained with `select distinct` and deterministic ordering.

## Database Initialization

Keep sample-data startup logic in `infrastructure/bootstrap/DatabaseInitializer`. This is an infrastructure concern because it depends on the Spring application lifecycle and writes to persistence.

The accepted initialization approach is:

- Register `DatabaseInitializer` as a Spring `@Service`.
- Restrict it to the `local` Spring profile with `@Profile("local")`; sample data must not be loaded during ordinary tests or production startup.
- Inject domain repository ports through constructor injection.
- Trigger initialization with `@PostConstruct` after Spring has supplied its dependencies.
- Build domain entities through their validated public constructors.
- Persist batches through a `saveAll` operation on the domain repository port; the infrastructure adapter delegates to Spring Data JPA.
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

Never expose JPA/domain entities directly from the API.

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

Controllers belong in `infrastructure/api` because they adapt HTTP requests/responses to application use cases and depend heavily on Spring MVC.

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

| Subject | Unit tests | Integration tests |
| --- | --- | --- |
| Domain | Plain JUnit; construct entities directly | Only when persistence behavior needs verification |
| Handlers / application services | `MockitoExtension`, mocked repository ports, direct construction or `@InjectMocks` | `@DataJpaTest` + `@Import` of the handler/service and real repository adapter; Spring Data JPA and H2 |
| Repository adapters | Mock Spring Data and verify delegation | `@DataJpaTest` + `@Import` of the real adapter |
| REST controllers | `@WebMvcTest` + `MockMvc`; `@MockitoBean` for all constructor dependencies, stubbing/verifying `Mediator.send()` or `dispatch()` where used | **Required:** `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`; real mediator, handlers, adapters, and test database |
| Mediator | Construct directly with explicit sample-handler lists; no Spring context | Add separately when verifying Spring wiring |

- Field `@Autowired` is acceptable in Spring tests: the test framework owns their lifecycle and dependency setup. Production classes use constructor injection with `final` fields to make required dependencies explicit and allow plain Java construction. `@Autowired` itself is not forbidden; a single production constructor does not need the annotation.
- Handler unit tests cover query/command type registration, DTO mapping, empty results, validation, rejected-input exception data, and absence of repository calls for invalid input. Verify immutable snapshots where that is part of the output contract.
- Handler integration tests verify persistence-backed behavior: generated IDs in DTOs, filtering, distinct/order semantics, and exclusion of supported-but-unused types. Controller tests verify routing, JSON, delegation, and semantic HTTP errors.
- Use controlled test databases and explicit fixtures, never the development/production database. Exact result assertions are appropriate; do not assume generated IDs or unspecified ordering.
- `@DataJpaTest` rolls back after each test by default. Full-context tests need `@Transactional` for rollback of same-thread writes; startup hooks execute before that transaction. Keep `local` disabled and test the initializer by constructing it and calling `init()` within the test transaction.
- Sample mediator requests/handlers must be private nested types without Spring annotations. Explicit test registries exclude production handlers; test fixtures must never register with the real mediator.
- HTTP system tests follow `HealthEndpointSystemTests`: `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`, `@AutoConfigureRestTestClient`, and an injected `RestTestClient` making requests through the running HTTP server. Verify public endpoint behavior with real application wiring. Server-side writes run outside the test thread's transaction, so arrange explicit cleanup for mutating system tests rather than relying on test-level `@Transactional` rollback.

Run the complete backend suite after code/test changes:

```powershell
mvn test
```

The project targets the Java version declared in the repository-root `.java-version` file (currently Java 25 LTS). Keep Maven, CI, and local `JAVA_HOME` aligned with that file before running Maven.

## Current Architectural Decisions Summary

- DDD Lite plus hexagonal architecture.
- JPA-annotated domain entities are accepted to avoid duplicate persistence models.
- Business invariants remain in the domain.
- Domain repository interfaces are outbound ports; Spring/JPA implementations are infrastructure adapters.
- Explicit use-case ports and application services are used instead of a mediator.
- Application services stay in the application layer and may use `@Service`/`@Transactional`.
- Controllers depend only on use-case interfaces.
- Application DTOs protect the HTTP API from entities and domain-specific types.
- Shared DTOs are preferred where reuse is expected.
- Controllers always return `ResponseEntity` and translate domain failures to HTTP statuses.
- Controllers require both focused web-layer tests and real integration coverage.
- Sample data is initialized centrally and idempotently through an infrastructure `@Service` using `@Profile("local")`, `@PostConstruct`, and repository-port `saveAll`.
- Integration tests use a controlled test database; the initializer test invokes `init()` manually within its rollback transaction.
