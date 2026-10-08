# Student Manager

A Student Management System for managing student records (create, read, update, delete).

> **Status:** The repository currently contains the project conventions (`.claude/`) and CI workflows only. The application source (`pom.xml`, `src/`) has not been added yet, so the sections below describe the target stack and architecture defined in those conventions.

## Technology Stack

| Layer     | Technology                                              |
|-----------|---------------------------------------------------------|
| Backend   | Java 25, Spring Boot 4.1.0, Maven                       |
| Database  | H2 (in-memory)                                          |
| Frontend  | Vanilla JS SPA, a single static HTML file served by Spring Boot |
| Testing   | JUnit 5 (integration tests only)                        |

## Commands

| Task    | Command              |
|---------|----------------------|
| Build   | `mvn clean compile`  |
| Package | `mvn clean package`  |
| Run     | `mvn spring-boot:run`|
| Test    | `mvn clean test`     |

## Architecture

Root package: `com.darshana.students`

The backend uses a layered structure:

| Package      | Responsibility                                                        |
|--------------|-----------------------------------------------------------------------|
| `config`     | Spring and third-party configuration (prefer `@ConfigurationProperties`) |
| `controller` | REST endpoints; request handling and delegation only                  |
| `dto`        | Immutable API request/response models (Java `record`s)                |
| `entity`     | JPA persistence models                                                |
| `repository` | Spring Data JPA interfaces, used only by the service layer            |
| `service`    | Business logic and transactional boundaries                           |
| `mapper`     | Entity ↔ DTO conversion                                               |
| `exceptions` | Custom domain/application exceptions                                  |
| `advice`     | Global exception handling via `@RestControllerAdvice`                 |
| `util`       | Stateless helpers                                                     |

### Key conventions

- Entities are never exposed in API responses; DTOs are used instead.
- Constructor injection only (`private final` fields).
- REST conventions with proper HTTP methods and status codes (`200`, `201`, `204`, ...).
- Request validation with Jakarta Validation and `@Valid` on controller parameters.
- Errors are returned as RFC 7807 `ProblemDetail`; stack traces and internals are never exposed.

### Frontend

A single static HTML page (vanilla JavaScript) served by Spring Boot that consumes the REST API.

## Testing

- Integration tests only; no unit tests and no E2E frameworks (e.g. Playwright).
- Tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` against in-memory H2, with `@Transactional` rollback.
- Application layers are not mocked; tests cover the full request → service → database flow.
- Error scenarios assert the HTTP status and `ProblemDetail` body.

See `.claude/rules/` for the full engineering and testing standards.
