---
name: backend-architect
description: Create, fix, or review Artseek backend elements using DDD Lite, CQRS Lite, and hexagonal architecture. Apply the project's layer boundaries, package layout, command/query handler contracts, mediator wiring, domain invariants, repository ports, DTOs, REST controllers, and local bootstrap conventions.
---

# Backend architect

Use this skill for backend implementation, refactoring, bug fixes, and architecture reviews. Apply the rules to new domain areas and features, not just the existing category operations. Preserve confirmed business requirements and ask the user about unclear behavior or architectural decisions rather than inventing them.

Artseek uses **DDD Lite + CQRS Lite + hexagonal architecture**. The domain is plain Java with no persistence annotations; each aggregate's table shape and mapping live in infrastructure. Application handlers deliberately use Spring service/transaction annotations to avoid transaction-decorator boilerplate.

The user confirmed the existing mediator and handlers as the intended architecture. This replaces the older explicit `<Feature>UseCase` / `<Feature>Service` and `application/usecase/` rules still present in `backend/AGENTS.md`. Its domain and infrastructure package layout follows the per-aggregate structure below, but its application `UseCase`/`Service` wording is retained at the user's request; do not restore the former structure because of that older wording.

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
          -> mapper + Spring Data JDBC -> PostgreSQL (schema from Flyway)
```

- Domain and application code must never import controllers, infrastructure adapters, or Spring Data repositories.
- Entities own business invariants and meaningful state changes. Their business logic must not depend on repositories, HTTP, or controllers.
- Handlers orchestrate use cases through domain types and repository ports; they do not implement complex business rules themselves.
- Controllers performing application operations inject the application `Mediator` interface, not `SpringMediator`, individual handlers, repositories, or concrete application services.
- Use constructor injection and `final` fields for production dependencies. A single constructor does not need `@Autowired`; the annotation itself is not forbidden.

`src/test/java/com/artseek/HexagonalArchitectureUnitTests` enforces these boundaries with ArchUnit: the domain depends only on `java..` and `com.artseek.domain..`; no class uses `jakarta.persistence` or `org.hibernate`; persistence models, mappers and Spring Data repositories are used only inside their own `infrastructure/<aggregate>/repository` package; the application imports nothing from infrastructure; controllers do not depend on handlers or repositories; and one aggregate's infrastructure does not depend on another aggregate's infrastructure. Keep it passing. Change a rule only when the user confirms an architectural change.

## Directory and package layout

```text
src/main/java/com/artseek/
  domain/
    shared/                             Cross-aggregate domain types (DomainValidationException)
    <aggregate>/                        Entities, value objects, enums
      exceptions/                       Aggregate-specific domain exceptions
      repository/                       Repository interfaces: outbound ports

  application/
    mediator/                           Command, Query, handler and bus contracts
    <aggregate>/
      <SharedDTO>.java                  Reusable application output DTO
      <feature>/
        <Feature>Query.java             Query request record
        <Feature>QueryHandler.java      Read use-case implementation
        # or <Feature>Command.java and <Feature>CommandHandler.java for writes

  infrastructure/
    <aggregate>/
      api/                              REST controllers for the aggregate
      repository/                       Adapter, persistence model, mapper, Spring Data repository
      exceptions/                       Only when an infrastructure exception exists
    api/                                Cross-cutting HTTP: health/, shared error DTOs and advice
    bootstrap/                          Startup/sample-data initialization
    config/                             Spring/framework configuration
    mediator/                           SpringMediator and registration errors
```

All three layers are organized by aggregate (currently `category` and `user`), so a concept's domain, application and infrastructure code share the same package name. Cross-cutting infrastructure such as `config/`, `mediator/`, `bootstrap/`, top-level `api/` and the future `security/` and `config/security/` stays at the top level of `infrastructure/`. Split an aggregate's `api/` into `controller/` and `requests/` only when it grows. Create `infrastructure/<aggregate>/exceptions/` only for an actual infrastructure exception.

Organize application code by aggregate and feature/vertical slice. Put a request and its handler in the same feature package. Nest related concepts within the domain area when needed, and put shared DTOs at the nearest common application package. Current examples:

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

The domain is persistence-ignorant plain Java: no `jakarta.persistence`, `org.hibernate` or Spring annotations, and no constructors that exist only for a framework. ArchUnit enforces that it depends only on the JDK and itself.

- Provide validated public constructors or creation factories for new aggregates, such as `new Category(name, type)` or `User.register(...)`. A new aggregate has `id == null`; the database generates the id on the first save.
- Provide a `restore(...)` factory that rebuilds an existing aggregate from persisted values, such as `Category.restore(id, name, type)` or `User.restore(id, username, email, passwordHash, role)`. It runs the same validation as creation, so stored data that breaks a current rule fails when loaded. When a rule becomes stricter, ship a Flyway data migration that fixes the existing rows in the same PR.
- Validation lives in the type that owns the rule. Value objects such as `Username`, `Email` and `PasswordHash` validate in their constructors; the aggregate validates its other fields through explicit `validateX` methods that throw its domain exception. Do not use `Objects.requireNonNull` or generic null-check helpers, and do not re-check a value object the aggregate receives.
- Make fields `final` unless a domain operation changes them. Avoid public setters and anemic data structures. Expose meaningful operations such as `rename` or `changeType`, enforcing invariants on construction and state changes.
- Entities have persistent identity independent of mutable attributes. Do not assume new entities already have IDs.
- The domain has no timestamp fields. `created_at` / `updated_at` belong to the database; add them to the domain only for a confirmed requirement.

### Closed vocabularies and exceptions

- Use Java enums for confirmed closed business vocabularies. Each constant owns a stable lowercase public string, such as `CategoryType.PAINTING.value()` returning `"painting"`.
- Parse external strings through domain methods such as `CategoryType.fromValue`, retaining the domain's established validation and normalization behavior. Do not invent new enum values.
- Store enums by their constant **name** (for example `PAINTING`), never the ordinal. Only the mapper converts with `name()` / `valueOf(...)`; `fromValue` parses public API strings. The stored name and the public value serve different boundaries.
- Put aggregate-specific validation exceptions in `domain/<aggregate>/exceptions/`, extending `domain/shared/DomainValidationException`. Do not expose generic `NullPointerException` or `IllegalArgumentException` as domain validation failures.
- Give exceptions a stable fixed message, exposed as `MESSAGE` when appropriate, and structured rejected-input data with an accessor. HTTP translation belongs at the API boundary.

## CQRS Lite requests and handlers

The existing CQRS implementation separates reads and writes through synchronous in-process dispatch over the shared domain model and PostgreSQL database. The mediator routes requests; handlers implement the use cases.

### Request input and result contracts

| Operation | Request | Handler input | Handler output | Routing |
| --- | --- | --- | --- | --- |
| Read | Immutable record implementing `Query<R>` | The concrete query record in `handle(Q query)` | The declared application DTO result, or immutable collection of DTOs | `mediator.send(query)` |
| Write | Immutable record implementing `Command<Void>` by default | The concrete command record in `handle(C command)` | No application result; with the existing generic API, `Void handle(...)` returns `null` after successful execution | `mediator.dispatch(command)` |

Requests contain the application-level inputs needed by the operation, such as IDs, strings, and other simple values. An operation with no input uses an empty record. Controllers construct requests from HTTP input; handlers receive the application request, not HTTP request/response objects or a domain aggregate supplied by the controller. Handlers convert and validate external values through domain types before accessing persistence when that validation does not require loading data.

Commands should in principle return nothing. The existing `Command<R>` / `CommandHandler<C, R>` / `CommandBus` contracts permit a generic result, but that capability does not establish a result-returning command convention. Use `Void` for new commands and ask the user before introducing an exception that returns a DTO, generated ID, or another result. Do not change the shared generic interfaces as part of adding an ordinary feature.

Query results must not expose domain aggregates or domain enums. For example:

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
- Commands load required aggregates through ports, invoke their domain operations, and **always** call `repository.save(aggregate)` afterwards, continuing with the returned aggregate. There is no dirty checking: a mutated aggregate that is not saved is not persisted. Use `@Transactional` for the write use-case boundary, then return `null` for `Void`.
- Keep handlers in the application layer despite Spring annotations. Layer membership follows responsibility; moving them into infrastructure would move use-case behavior out of its layer.
- Keep handlers directly constructible and unit-testable without Spring.

### Mediator contracts and implementation

`application/mediator/` owns `Command<R>`, `Query<R>`, `CommandHandler<C, R>`, `QueryHandler<Q, R>`, `CommandBus`, `QueryBus`, and `Mediator`. `Mediator` combines the two buses. Keep these contracts independent of Spring and HTTP.

`infrastructure/mediator/SpringMediator` is a Spring `@Component`. It receives injected lists of command/query handlers and builds separate immutable registries keyed by `commandType()` / `queryType()`. Routing uses the concrete request class. Preserve duplicate-registration rejection through `DuplicateHandlerException` and missing-handler errors through `HandlerNotFoundException`. Add a feature's handler as a bean rather than adding a feature-specific switch or registration branch to the mediator.

## Repository ports and persistence adapters

Repository interfaces in `domain/<aggregate>/repository/` use domain types and express what the core needs. They are outbound ports, not Spring Data interfaces. Handlers depend on those ports only.

For each aggregate, `infrastructure/<aggregate>/repository/` contains:

```text
<Aggregate>Repository              domain/<aggregate>/repository/: domain port
<Aggregate>RepositoryAdapter       @Repository implementing the port; the only user of the three below
<Aggregate>PersistenceModel        package-private @Table record: the table row, primitive columns only
<Aggregate>Mapper                  package-private: toPersistence(aggregate) / toDomain(model)
SpringData<Aggregate>Repository    package-private ListCrudRepository<<Aggregate>PersistenceModel, Long>
```

Persistence models, mappers and Spring Data interfaces never leave their `repository` package; ArchUnit enforces it. Do not import adapters or Spring Data repositories into application code. A technology-specific name such as `Jdbc<Aggregate>RepositoryAdapter` is needed only when multiple persistence technologies make the ordinary name ambiguous.

**Persistence model.** A record annotated only with Spring Data annotations (`org.springframework.data.annotation.*`, `org.springframework.data.relational.core.mapping.*`), never `jakarta.persistence`:

```java
@Table("categories")
record CategoryPersistenceModel(
        @Id Long id,
        String name,
        String type,                         // CategoryType name, e.g. PAINTING
        // Managed by the database: DEFAULT now() and trg_categories_set_updated_at (V1, V2)
        @ReadOnlyProperty Instant createdAt,
        @ReadOnlyProperty Instant updatedAt) {

    static final String NAME_TYPE_CONSTRAINT = "uq_categories_name_type";

    /** Lets Spring Data set the generated id on the saved copy. */
    CategoryPersistenceModel withId(Long id) { ... }
}
```

- Columns are plain types (`String`, `Long`, `Instant`). Value objects and enums are converted only by the mapper.
- The model carries no lengths, constraints or defaults: the Flyway migration is the source of truth and the model follows it. Add `@Column("...")` only when the default snake_case naming does not produce the column name.
- Database-owned timestamps are `@ReadOnlyProperty`: never written (an explicit `NULL` would bypass `DEFAULT now()`), but read on SELECT.
- Constraint-name constants used by tests or error translation live on the persistence model, not in the domain.

**Mapper.** A `final` class with a private constructor and static `toPersistence(...)` / `toDomain(...)`. It is the only place that turns value objects into strings and back, and enums into stored names and back. `toPersistence` passes `null` for the read-only timestamps; `toDomain` always rebuilds through the aggregate's `restore(...)`, so stored values are validated again.

**Spring Data repository.** Prefer derived queries. Their parameters are the stored strings, which only the adapter supplies from value objects (`username.value()`, `type.name()`), so they are always normalized. Write SQL with `@Query` only when a derived query cannot express the operation clearly, as in `SELECT DISTINCT type FROM categories ORDER BY type`. Always use bound parameters (`:name`), never string concatenation. Do not promise ordering for queries whose contract does not specify it.

**Adapter.** Maps domain arguments to stored values, delegates to Spring Data, and maps results back through the mapper. `save` and `saveAll` always map and return the result of Spring Data's save, which carries the generated id. A `null` id means INSERT; a non-null id means UPDATE.

## Database schema and Flyway migrations

The schema comes only from hand-written Flyway migrations in `src/main/resources/db/migration/`, in production and tests. Nothing is generated from code, and Spring Data JDBC does not validate the schema at startup.

- Write one `V<n>__<description>.sql` per change. Flyway applies them in order and checksums each one in `flyway_schema_history`. Never edit a migration once it is merged; every schema change is a new migration.
- For a schema change: write the migration, update the persistence model, mapper and adapter, then run the tests. Flyway builds a fresh Testcontainers database, and the adapter integration tests catch mismatches between model and schema.
- Ids are `BIGINT GENERATED ALWAYS AS IDENTITY`. Every table has `created_at` / `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()` and a `BEFORE UPDATE` trigger calling the shared `set_updated_at()` function (V1), which refreshes `updated_at` only when the row actually changed.
- Name every database object explicitly with the prefix convention `<type>_<table>_<columns>`, in snake_case and at most 63 characters (the PostgreSQL limit):

| Object | Pattern | Example |
| --- | --- | --- |
| Primary key | `pk_<table>` | `pk_categories` |
| Unique | `uq_<table>_<columns>` | `uq_categories_name_type`, `uq_users_email` |
| Foreign key | `fk_<table>_<column>_<referenced_table>` | `fk_artworks_author_id_users` |
| Index | `ix_<table>_<columns>` | `ix_categories_type` |
| Check | `ck_<table>_<description>` | `ck_users_role_valid` |
| Trigger | `trg_<table>_<action>` | `trg_categories_set_updated_at` |
| Function | `<verb>_<object>` | `set_updated_at` |

Distinguish a vocabulary's **supported values** (all domain enum values, independent of storage) from **used values** (values represented by persisted rows). A supported-value query reads the enum without querying persistence; a used-value query calls the domain repository port. These are separate operations.

## Application DTOs

Never expose domain aggregates directly from the API. Use reusable application DTO records at the nearest common package when current or future related operations can share them.

- Prefer transport-safe basic values such as `String`, `Long`, `Boolean`, and time primitives.
- Map domain enums to their stable public strings rather than placing domain enum types in DTOs.
- Provide focused factories such as `CategoryDTO.from(Category)`.
- Return immutable collections using `Stream.toList()`, `List.copyOf`, or equivalent.
- Name shared DTOs after the concept rather than one use case when reuse is expected.

Examples are `application/category/CategoryDTO(Long id, String name, String type)` and `application/category/categorytype/CategoryTypeDTO(String value)`. These illustrate placement and mapping for future domain areas; they do not require every feature to have the same fields.

## REST API adapters

Aggregate controllers belong in `infrastructure/<aggregate>/api/`, such as `infrastructure/category/api/CategoryRestController`; cross-cutting HTTP such as `infrastructure/api/health/HealthController` stays in `infrastructure/api/`. Controllers adapt HTTP inputs and outputs. Keep business decisions in the domain and orchestration in handlers.

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
