# Artseek backend

Spring Boot REST API for Artseek.

## Requirements

- Java 21
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
