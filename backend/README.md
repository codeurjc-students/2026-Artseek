# Artseek backend

Spring Boot REST API for Artseek.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL 16+ with a database and user named `artseek`

## Run

```powershell
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. Verify it with `GET /api/health`.
Database settings can be changed with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and
`JPA_DDL_AUTO`. Change the permitted browser origin with `FRONTEND_ORIGIN`.
