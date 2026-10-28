# Artseek backend

Spring Boot REST API for Artseek.

## Requirements

- Java version declared in the repository's `.java-version` file
- Maven 3.9+
- PostgreSQL 17 with a database and user named `artseek` for local application runs
- A running Docker-compatible engine using Linux containers for integration/system tests

## Run locally

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

The `local` profile enables `DatabaseInitializer`, which adds the sample categories
without duplicating rows that already exist. Run without the profile when sample data
must not be initialized:

```powershell
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. Verify it with `GET /api/health`.
Database settings can be changed with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and
`JPA_DDL_AUTO`. Change the permitted browser origin with `FRONTEND_ORIGIN`.

## Tests and coverage

Run the complete unit, integration, and HTTP system test suite with:

```powershell
mvn --batch-mode --no-transfer-progress verify
```

Database tests use disposable `postgres:17.11` Testcontainers instances, matching
the development PostgreSQL 17 engine and the inspected local image version. No
separate test database needs to be started manually. Start Docker Desktop (with
Linux containers) or another Docker-compatible engine and check `docker info`
before running database tests. The first run downloads the image. When Docker is
unavailable, database tests fail rather than being skipped or using an embedded
database.

All shared test base classes live under `src/test/java/com/artseek/common`:

| Base class | Inherited configuration |
| --- | --- |
| `IntegrationTest` | Imports `PostgresTestConfiguration` for the isolated PostgreSQL database. Does not select a Spring test scope or start a test transaction. |
| `JpaIntegrationTest extends IntegrationTest` | `@DataJpaTest` and `@AutoConfigureTestDatabase(replace = NONE)`: focused persistence slice, PostgreSQL datasource and automatic rollback after each test. |
| `FullContextIntegrationTest extends IntegrationTest` | `@SpringBootTest` and `@Transactional`: full application context with automatic rollback of test-thread writes after each test, including read-only tests. Uses the default mock web environment, without starting an HTTP server. |
| `SystemTest extends IntegrationTest` | `@SpringBootTest(webEnvironment = RANDOM_PORT)` and `@AutoConfigureRestTestClient`; supplies the injected, protected `restTestClient` for real HTTP requests. |

Choose the base class and add the following annotations to each concrete test:

| Test category | Extend | Additional annotations/setup | Docker required |
| --- | --- | --- | --- |
| Domain and mediator unit tests (`*UnitTests`) | None | Plain JUnit `@Test`; construct subjects directly. | No |
| Handler/service and adapter unit tests (`*UnitTests`) | None | `@ExtendWith(MockitoExtension.class)`, mocked dependencies and direct construction or `@InjectMocks`. | No |
| Controller web-slice tests (`*UnitTests`) | None | `@WebMvcTest(Controller.class)`, `@MockitoBean` for constructor dependencies, injected MockMvc. | No |
| Handler/service integration tests (`*IntegrationTests`) | `JpaIntegrationTest` | `@Import` the handler/service and real repository adapters it needs. | Yes |
| Repository adapter integration tests (`*IntegrationTests`) | `JpaIntegrationTest` | `@Import` the real adapter being tested. | Yes |
| Controller integration tests (`*IntegrationTests`) | `FullContextIntegrationTest` | `@AutoConfigureMockMvc`; real mediator, handlers, adapters and persistence. | Yes |
| Application/bootstrap integration tests (`*IntegrationTests`) | `FullContextIntegrationTest` | JUnit `@Test` methods and injected collaborators; full-context and transaction annotations are inherited, including for read-only tests. | Yes |
| HTTP system tests (`*SystemTests`) | `SystemTest` | JUnit `@Test` methods using the inherited `restTestClient`; explicit cleanup for committed fixture/server writes. | Yes |

Concrete JPA, full-context and system tests inherit their Spring annotations; do not repeat them
or combine `@DataJpaTest` with `@SpringBootTest`. Each concrete class still owns its
`@Test` methods, fixtures and focused imports. Keep its package aligned with the
production subject, including system tests alongside their controller's other tests.

`JpaIntegrationTest` verifies a focused persistence path: repository adapters,
entity mappings, queries and database-backed handler/service behavior. The slice
loads JPA infrastructure and repositories; `@Import` adds only the handler/service
and adapters needed by that test. It does not load controllers, the real mediator
or the rest of the application automatically. For example, a category-handler test
calls the handler directly and checks filtering, persisted IDs and DTO mapping
through the real PostgreSQL-backed repository.

`FullContextIntegrationTest` verifies collaboration and configuration across the
application's layers. It loads the full Spring application with real mediator,
handlers, repository adapters and persistence. With `@AutoConfigureMockMvc`, a
controller test can exercise routing, validation, JSON and the complete
controller-to-database path without a listening HTTP server. Application/bootstrap
tests use the same base to check application wiring and initializer behavior.
Loading the full context does not mean every test exercises every bean: its entry
point and assertions determine the functionality covered.

Both bases use isolated PostgreSQL containers and roll back their test transactions.
Choose the JPA slice for focused persistence/use-case behavior and the full context
when real application wiring or multiple layers must work together. Use `SystemTest`
when the test must send requests over HTTP to a running server; its committed writes
require explicit cleanup.

Run unit and controller web-slice tests independently, including without Docker:

```powershell
mvn test "-Dtest=*UnitTests" "-Dsurefire.failIfNoSpecifiedTests=true"
```

Select database test groups or one class with:

```powershell
mvn test "-Dtest=*IntegrationTests" "-Dsurefire.failIfNoSpecifiedTests=true"
mvn test "-Dtest=*SystemTests" "-Dsurefire.failIfNoSpecifiedTests=true"
mvn test "-Dtest=CategoryRepositoryAdapterIntegrationTests"
```

`PostgresTestConfiguration` lives only under `src/test/java/com/artseek/infrastructure/config`.
The abstract `IntegrationTest` base imports its Spring-managed container bean.
Concrete database tests extend `JpaIntegrationTest`, `FullContextIntegrationTest` or `SystemTest`,
according to the table above. Unit and web slices do not extend these bases. Spring
Boot's `@ServiceConnection` supplies JDBC credentials and a
dynamically allocated host port, so tests never use the development database on
port 5432, its credentials or its volume. The test database is named `artseek_test`.

Each cached Spring context owns one container, reused by every test sharing that
context. Different slice imports or web configurations get separate contexts and
containers. Spring closes JPA and datasource beans before stopping their container.
Containers are not started per test method or retained between Maven runs. Do not
add JUnit `@Container` lifecycle management, fixed host ports or persistent reuse.
The test schema continues to use Hibernate `create-drop`; production settings are
unchanged.

Tests arrange their own fixtures and must not assume execution order or fixed
generated IDs. JPA slices and full-context tests inherit automatic rollback from
their respective bases. Ordinary tests keep `local` disabled. The initializer
integration test constructs `DatabaseInitializer` and calls `init()` manually inside
its rollback transaction. HTTP system tests run through a separate server thread:
future mutating tests must explicitly clean up committed fixtures and server-side
writes, since a test transaction cannot roll those back. `SystemTest` intentionally
does not declare `@Transactional`: real HTTP requests execute in another thread
with their own transactions. Commit database fixtures before making HTTP requests
so the server can see them. Clean up after each mutating test with `@AfterEach`
repository operations or an `@Sql` cleanup script using
`executionPhase = AFTER_TEST_METHOD` and
`@SqlConfig(transactionMode = ISOLATED)` so cleanup commits independently.
Delete only test-owned data, including dependent rows, even if an assertion fails.
The existing health system test is read-only and requires no data cleanup.

For an isolation check, keep the development database running, record its category
rows, run the suite twice and confirm those rows stay unchanged. Database tests also
assert the PostgreSQL version and `artseek_test` database name. The PR/main CI workflow
checks Docker availability and runs the complete suite on its Ubuntu runner; the
development workflow runs only the Docker-independent unit/web-slice group.

JaCoCo attaches its agent before tests and generates `target/site/jacoco/index.html` for local inspection and
`target/site/jacoco/jacoco.xml` for SonarQube Cloud during `verify`.
Running `mvn test` executes tests without generating these reports.

The PR workflow uploads compiled application classes, test classes, and the XML
report for the Sonar analysis job. See the [SonarQube Cloud setup guide](../docs/sonarcloud-setup.md)
for the project binding, Actions secret, quality gate, and required merge checks.

## OpenAPI documentation

While the backend is running, the generated OpenAPI definition is available at
`http://localhost:8080/v3/api-docs.yaml` and the interactive Swagger UI at
`http://localhost:8080/swagger-ui.html`.

With PostgreSQL running, regenerate the repository documentation from the current
controllers with:

```powershell
mvn verify -Popenapi
```

The profile starts the backend, writes `../docs/api/api-docs.yaml`, generates
`../docs/api/api-docs.html` from that specification, and then stops the backend.
Commit both generated files whenever the REST API changes.
