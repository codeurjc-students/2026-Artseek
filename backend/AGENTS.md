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

## Testing Policy

Use the suffixes `UnitTests` and `IntegrationTests` to make test scope explicit.

### Unit tests

- Domain tests instantiate entities directly and verify invariants and exception data.
- Application service unit tests mock repository ports and verify orchestration, validation, DTO mapping, and that invalid input does not access persistence.
- Repository adapter unit tests mock the Spring Data repository and verify delegation.
- Controller unit/web-slice tests use `@WebMvcTest`, mocked use-case ports, and `MockMvc` to verify routing, status codes, JSON shapes, and delegation.

### Integration tests

- Repository adapter integration tests use `@DataJpaTest`, the real adapter, Spring Data JPA, and H2.
- **Every controller must have integration tests.** Controller integration tests use `@SpringBootTest`, `@AutoConfigureMockMvc`, real application services, the real repository adapter, JPA, and H2.
- Make controller integration tests transactional so inserted data rolls back between tests.
- Controller integration tests must cover successful DTO serialization, filtering/query semantics, and applicable failure status codes.
- Do not replace required controller integration tests with mocked `@WebMvcTest` tests; keep both when useful.

Tests must verify important persistence choices, including generated identifiers, enum-by-name persistence, distinct queries, and exclusion of nonmatching types.

Run the complete backend suite after changes:

```powershell
mvn test
```

The project targets Java 21. On the current development machine Maven may inherit a JDK 11 `JAVA_HOME`; set `JAVA_HOME` to an installed JDK 21+ before running Maven.

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
