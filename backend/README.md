# Artseek backend

Spring Boot REST API for Artseek.

## Requirements

- Java version declared in the repository's `.java-version` file
- Maven 3.9+
- PostgreSQL 16+ with a database and user named `artseek`

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
