---
name: backend-architect
description: Create, fix, or review Artseek backend elements using DDD Lite, CQRS Lite, and hexagonal architecture. Apply the project's layer boundaries, package layout, command/query handler contracts, mediator wiring, domain invariants, repository ports, DTOs, REST controllers, and local bootstrap conventions.
---

# Backend architect

Use this skill for backend implementation, refactoring, bug fixes, and architecture reviews. Apply the rules to new domain areas and features, not just the existing category operations. Preserve confirmed business requirements and ask the user about unclear behavior or architectural decisions rather than inventing them.

Artseek uses **DDD Lite + CQRS Lite + hexagonal architecture**. Domain entities deliberately carry JPA annotations, and application handlers deliberately use Spring service/transaction annotations to avoid duplicate models and transaction-decorator boilerplate.

The user confirmed the existing mediator and handlers as the intended architecture. This replaces the older explicit `<Feature>UseCase` / `<Feature>Service` and `application/usecase/` rules still present in `backend/AGENTS.md`. That file is being retained unchanged at the user's request; do not restore the former structure because of that older wording.

Paths below are relative to `backend/` unless stated otherwise.

## Layer boundaries and dependency direction

Source dependencies point inward: infrastructure -> application -> domain. The application owns request/handler and mediator contracts; infrastructure supplies the Spring mediator and persistence implementations.

The runtime request path is:

```text
REST controller
  -> application Mediator.send(query) / dispatch(command)
    -> application query / command handler
      -> domain repository port and domain behavior
        -> infrastructure repository adapter
          -> Spring Data JPA -> PostgreSQL
```

- Domain and application code must never import controllers, infrastructure adapters, or Spring Data repositories.
- Entities own business invariants and meaningful state changes. Their business logic must not depend on repositories, HTTP, or controllers.
- Handlers orchestrate use cases through domain types and repository ports; they do not implement complex business rules themselves.
- Controllers performing application operations inject the application `Mediator` interface, not `SpringMediator`, individual handlers, repositories, or concrete application services.
- Use constructor injection and `final` fields for production dependencies. A single constructor does not need `@Autowired`; the annotation itself is not forbidden.

## Directory and package layout

```text
src/main/java/com/artseek/
  domain/
    model/<entity>/                     Entities, value objects, enums
    exceptions/<entity>/                Entity-specific domain exceptions
    repository/                         Repository interfaces: outbound ports

  application/
    mediator/                           Command, Query, handler and bus contracts
    <domain-area>/
      <SharedDTO>.java                  Reusable application output DTO
      <feature>/
        <Feature>Query.java             Query request record
        <Feature>QueryHandler.java      Read use-case implementation
        # or <Feature>Command.java and <Feature>CommandHandler.java for writes

  infrastructure/
    api/                                REST controllers and HTTP concerns
    bootstrap/                          Startup/sample-data initialization
    config/                             Spring/framework configuration
    mediator/                           SpringMediator and registration errors
    repository/<entity>/                Persistence adapters and Spring Data
```

Organize application code by domain area and feature/vertical slice. Put a request and its handler in the same feature package. Nest related concepts within the domain area when needed, and put shared DTOs at the nearest common application package. Current examples:

```text
application/category/
  CategoryDTO.java
  findbytype/
    FindCategoriesByTypeQuery.java
    FindCategoriesByTypeQueryHandler.java
  categorytype/
    CategoryTypeDTO.java
    findusedtypes/
      FindUsedCategoryTypesQuery.java
      FindUsedCategoryTypesQueryHandler.java
```

## Domain model

### Entities and invariants

Domain entities may use Jakarta persistence annotations such as `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, and `@Enumerated`. This is the accepted DDD Lite compromise: persistence coupling avoids duplicate domain/persistence models, while business behavior remains in the domain.

- Provide the protected no-argument constructor required by JPA and validated public constructors/factories for ordinary use.
- Avoid public setters and anemic data structures. Expose meaningful operations such as `rename` or `changeType`, enforcing invariants on construction and state changes.
- Entities have persistent identity independent of mutable attributes. A generated identifier can be `null` until persistence assigns it; do not assume new entities already have IDs.

### Closed vocabularies and exceptions

- Use Java enums for confirmed closed business vocabularies. Each constant owns a stable lowercase public string, such as `CategoryType.PAINTING.value()` returning `"painting"`.
- Parse external strings through domain methods such as `CategoryType.fromValue`, retaining the domain's established validation and normalization behavior. Do not invent new enum values.
- Persist enums with `@Enumerated(EnumType.STRING)`, never ordinal values. The stored enum name and its public string value serve different boundaries.
- Put entity-specific validation exceptions in `domain/exceptions/<entity>/`. Do not expose generic `NullPointerException` or `IllegalArgumentException` as domain validation failures.
- Give exceptions a stable fixed message, exposed as `MESSAGE` when appropriate, and structured rejected-input data with an accessor. HTTP translation belongs at the API boundary.

## CQRS Lite requests and handlers

The existing CQRS implementation separates reads and writes through synchronous in-process dispatch over the shared domain/JPA model and PostgreSQL database. The mediator routes requests; handlers implement the use cases.

### Request input and result contracts

| Operation | Request | Handler input | Handler output | Routing |
| --- | --- | --- | --- | --- |
| Read | Immutable record implementing `Query<R>` | The concrete query record in `handle(Q query)` | The declared application DTO result, or immutable collection of DTOs | `mediator.send(query)` |
| Write | Immutable record implementing `Command<Void>` by default | The concrete command record in `handle(C command)` | No application result; with the existing generic API, `Void handle(...)` returns `null` after successful execution | `mediator.dispatch(command)` |

Requests contain the application-level inputs needed by the operation, such as IDs, strings, and other simple values. An operation with no input uses an empty record. Controllers construct requests from HTTP input; handlers receive the application request, not HTTP request/response objects or a JPA entity supplied by the controller. Handlers convert and validate external values through domain types before accessing persistence when that validation does not require loading data.

Commands should in principle return nothing. The existing `Command<R>` / `CommandHandler<C, R>` / `CommandBus` contracts permit a generic result, but that capability does not establish a result-returning command convention. Use `Void` for new commands and ask the user before introducing an exception that returns a DTO, generated ID, or another result. Do not change the shared generic interfaces as part of adding an ordinary feature.

Query results must not expose domain/JPA entities or domain enums. For example:

```java
public record FindCategoriesByTypeQuery(String type)
        implements Query<List<CategoryDTO>> {}
```

The matching handler implements `QueryHandler<FindCategoriesByTypeQuery, List<CategoryDTO>>`, accepts that record, parses its type through the domain enum, queries the repository port, and maps to DTOs.

### Handler implementation

- Implement `QueryHandler<Q, R>` or `CommandHandler<C, Void>` in the feature's application package.
- Implement `queryType()` or `commandType()` to return the exact concrete request class. The request, handler generic parameters, and `handle` result must agree.
- Register handlers with `@Service` and inject required domain repository ports through constructors.
- Queries read without mutating domain state and use `@Transactional(readOnly = true)` on the use-case boundary.
- Commands load required entities through ports, invoke their domain operations, and persist through ports as needed. Use `@Transactional` for the write use-case boundary, then return `null` for `Void`.
- Keep handlers in the application layer despite Spring annotations. Layer membership follows responsibility; moving them into infrastructure would move use-case behavior out of its layer.
- Keep handlers directly constructible and unit-testable without Spring.

### Mediator contracts and implementation

`application/mediator/` owns `Command<R>`, `Query<R>`, `CommandHandler<C, R>`, `QueryHandler<Q, R>`, `CommandBus`, `QueryBus`, and `Mediator`. `Mediator` combines the two buses. Keep these contracts independent of Spring and HTTP.

`infrastructure/mediator/SpringMediator` is a Spring `@Component`. It receives injected lists of command/query handlers and builds separate immutable registries keyed by `commandType()` / `queryType()`. Routing uses the concrete request class. Preserve duplicate-registration rejection through `DuplicateHandlerException` and missing-handler errors through `HandlerNotFoundException`. Add a feature's handler as a bean rather than adding a feature-specific switch or registration branch to the mediator.

## Repository ports and persistence adapters

Repository interfaces in `domain/repository/` use domain types and express what the core needs. They are outbound ports, not Spring Data interfaces. Handlers depend on those ports only.

For each persisted entity, use:

```text
<Entity>Repository                 domain/repository/: domain port
<Entity>RepositoryAdapter          infrastructure/repository/<entity>/: @Repository
SpringData<Entity>Repository       infrastructure/repository/<entity>/: Spring Data JPA
```

The adapter implements the domain port and delegates to Spring Data through constructor injection. A technology-specific name such as `Jpa<Entity>RepositoryAdapter` is needed only when multiple persistence technologies make the ordinary name ambiguous. Do not import adapters or Spring Data repositories into application code.

Use explicit JPQL when derived-query names cannot clearly express the operation. For operations requiring unique, ordered values, express those semantics explicitly, as in the existing `select distinct ... order by ...` used-type query. Do not promise ordering for queries whose contract does not specify it.

Distinguish a vocabulary's **supported values** (all domain enum values, independent of storage) from **used values** (values represented by persisted rows). A supported-value query reads the enum without querying persistence; a used-value query calls the domain repository port. These are separate operations.

## Application DTOs

Never expose domain/JPA entities directly from the API. Use reusable application DTO records at the nearest common package when current or future related operations can share them.

- Prefer transport-safe basic values such as `String`, `Long`, `Boolean`, and time primitives.
- Map domain enums to their stable public strings rather than placing domain enum types in DTOs.
- Provide focused factories such as `CategoryDTO.from(Category)`.
- Return immutable collections using `Stream.toList()`, `List.copyOf`, or equivalent.
- Name shared DTOs after the concept rather than one use case when reuse is expected.

Examples are `application/category/CategoryDTO(Long id, String name, String type)` and `application/category/categorytype/CategoryTypeDTO(String value)`. These illustrate placement and mapping for future domain areas; they do not require every feature to have the same fields.

## REST API adapters

Controllers belong in `infrastructure/api/` and adapt HTTP inputs and outputs. Keep business decisions in the domain and orchestration in handlers.

- Inject the application `Mediator` interface for business operations and construct the corresponding query or command. Do not access repositories or concrete handlers/services directly.
- Return application DTOs for result-bearing operations. Always use `ResponseEntity` to make HTTP status and headers explicit, including operations with no response body.
- Use a known response type, such as `ResponseEntity<List<CategoryDTO>>` or `ResponseEntity<Void>` for an empty body. Use `ResponseEntity<?>` only when a stable body type is genuinely undefined.
- Prefer builders such as `ResponseEntity.ok(body)`, `ResponseEntity.status(...).body(...)`, and `ResponseEntity.badRequest().build()`.
- Translate domain failures into semantic HTTP statuses at this boundary, such as invalid category types becoming `400 Bad Request`. Do not put HTTP semantics in domain exceptions or handlers, or expose stack traces/domain objects in response bodies.
- Local controller exception handling is acceptable at the current scope. If handling becomes repetitive, use shared `@RestControllerAdvice` and consistent error DTOs instead of repeated `try/catch` blocks.

Preserve established routes and avoid ambiguous paths. The current category API uses `GET /api/v1/categories/types/used` and `GET /api/v1/categories/{type}`; the latter intentionally uses a path parameter rather than a query parameter. Keep static routes alongside it. If adding lookup by ID would conflict with `/{type}`, choose an explicit unambiguous path with the user when the API contract is not specified.

## Local sample-data bootstrap

Keep sample initialization in `infrastructure/bootstrap/DatabaseInitializer`, because it depends on the Spring lifecycle and persistence.

- Register it as `@Service` with `@Profile("local")` and trigger initialization through `@PostConstruct` after dependencies are supplied.
- Inject domain repository ports, construct entities through validated public constructors, and batch persistence through the port's `saveAll` delegated by its adapter.
- Make startup idempotent: check natural identifying attributes before inserting each sample so restarts do not duplicate rows.
- Add future entities' sample data to this centralized initializer, or split into focused bootstrap collaborators when it grows substantially.
- Keep sample data disabled in ordinary tests and production. Initializer integration tests construct it and invoke `init()` inside the test rollback transaction; see the testing skill for the required base.

Run locally from `backend/` with:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Create, fix, and review

For a new feature, locate its domain area, define the required domain behavior and ports, add its request/handler and output DTO where needed, and expose it through a thin controller when HTTP access is required. For a fix, trace the request through those boundaries and change the layer responsible for the behavior. For a review, check dependency direction, file placement, domain invariants, request/result contracts, mediator registration, transaction boundaries, DTO mapping, and HTTP adaptation against the actual implementation.

Use [backend-tester](../backend-tester/SKILL.md) for the required test categories, inherited bases, database isolation, and verification commands. Domain invariants need plain unit tests; handlers/adapters need focused unit and persistence coverage; controllers need web-slice and real integration coverage. Consult `backend/README.md` from the repository root for runtime and API-documentation commands.
