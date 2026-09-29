# Request Management Application

This is a backend service for managing the lifecycle of requests, built with Java and Spring Boot, based on a Hexagonal Architecture (Ports and Adapters) with separated Gradle modules with a Rich Domain Model.

## Assumptions & Architecture Decisions

Below are all the assumptions I made in this project that are **not** explicitly stated in the specification.

### Architecture & Technology Stack
- **Hexagonal Architecture (Ports & Adapters)** was chosen with three Gradle modules (`domain`, `application`, `infrastructure`) to strictly separate business logic from framework concerns. The domain module has zero dependencies on Spring or any other framework.
- **Rich Domain Model**: All state transition logic and business rules live inside the `Request` aggregate root rather than in a service layer (avoiding an anemic model).
- **Technology choices**: Java 21, Spring Boot, Gradle, PostgreSQL, Flyway for schema migrations, Testcontainers for integration tests. These were not prescribed by the spec.

### State Diagram Interpretation
- The spec provides a state diagram but does not enumerate every transition textually. Below are the states order as I picture them:
  - `CREATED → VERIFIED → ACCEPTED → PUBLISHED` (happy path)
  - `CREATED → DELETED` (with reason)
  - `VERIFIED → REJECTED` (with reason)
  - `ACCEPTED → REJECTED` (with reason)
- **Terminal states**: `PUBLISHED`, `DELETED`, and `REJECTED` are terminal — no further transitions are allowed from these states.
- **Verify, Accept, Publish are one-directional**: there is no "undo" or reversal of state transitions.

### Request Identifiers
- Each request is identified by a **UUID** (generated server-side at creation). This is the primary key in the database.
- The **unique numeric identifier** assigned at publication is a separate, sequential number generated via a PostgreSQL `SEQUENCE`. This is stored as `sequenceNumber` alongside the UUID and is only populated when the request reaches `PUBLISHED` state.

### Deletion Semantics
- **Soft delete**: Deleting a request transitions it to the `DELETED` state. The record remains in the database and is still retrievable. Physical deletion is not performed — this preserves audit trail integrity and data consistency.

### Reason for Rejection / Deletion
- The spec says "a reason must be supplied" but does not define the format. This implementation uses a **predefined enum** (`ActionReason`: `DUPLICATE`, `INAPPROPRIATE_CONTENT`, `NOT_NEEDED`, `OTHER`) rather than free-text. This ensures consistency and supports filtering/reporting, but limits flexibility.
- A single `actionReason` field is used for both rejection and deletion reasons.

### Idempotency
- An **idempotency mechanism** was added (not required by the spec) for all POST (state-mutating) operations using an `X-Idempotency-Key` header (UUID format). This prevents duplicate resource creation on network retries.
- Idempotency records are stored in a dedicated database table. A scheduled cleanup job purges records older than 24 hours (configurable via `idempotency.cleanup.rate`).

### REST API Design
- **HTTP methods**: `POST` for creation and state transitions, `PUT` for content updates, `DELETE` for deletion, `GET` for retrieval/listing.
- **HTTP status codes**: `201 Created` for new requests, `200 OK` for updates and transitions, `400 Bad Request` for validation errors, `404 Not Found` for missing resources, `409 Conflict` for invalid state transitions and concurrent processing.
- State transition endpoints follow the pattern `POST /api/requests/{id}/{action}` (e.g., `/verify`, `/accept`, `/publish`, `/reject`).
- The `DELETE /api/requests/{id}` endpoint expects a JSON body containing the reason.

### Validation
- **Double-layered validation**: Jakarta Bean Validation annotations on DTOs (infrastructure layer) + explicit invariant checks in the domain constructor and methods. This ensures the domain layer is independently testable and framework-agnostic.
- `name` has a maximum length of **255 characters** (not specified in the requirements).
- Both `name` and `content` must be non-blank at creation time.

### Audit Log
- The audit log captures **every state change** with the request ID, previous state, new state, and timestamp.
- Audit log entries are immutable (append-only).
- The audit log is an infrastructure concern — it is written in the repository adapter during `save()`, not in the domain model.

### Pagination & Filtering
- Default page size is **10** (as specified). Page numbering is zero-based.
- Name filtering uses exact match.
- State filtering uses the enum value directly.

### Content Editing
- Editing content in the `VERIFIED` state does **not** revert the state back to `CREATED`. The request stays in `VERIFIED` state with updated content.

## Best Practices for Next Steps

The following are recommended improvements to bring this application closer to production readiness, ordered by priority.

### High Priority
1. **Add OpenAPI / Swagger Documentation** — Integrate `springdoc-openapi` to auto-generate API documentation from the controller annotations.
2. **Introduce Spring Profiles** — Create `application-dev.yml` and `application-prod.yml` to separate environment-specific configuration (logging, connection pools, etc.).
3. **Add Structured Logging** — Integrate SLF4J MDC with request IDs and correlation IDs for traceability. Log all state transitions at INFO level.

### Medium Priority
4. **Add an Audit Log Query Endpoint** — Expose `GET /api/requests/{id}/history` to retrieve the audit trail for a specific request.
5. **Implement HATEOAS Links** — Add hypermedia links to responses indicating which state transitions are available from the current state.
6. **Add Rate Limiting** — Implement request rate limiting to protect against abuse.

### Lower Priority
7. **Containerize the Application** — Add a `Dockerfile` and `docker-compose.yml` for simplified local development and deployment.
8. **Add Health Checks** — Integrate Spring Boot Actuator with `/actuator/health` for monitoring and readiness/liveness probes.
9. **CI/CD Pipeline** — Add a GitHub Actions workflow for automated build, test, and static analysis on every push.

## Requirements
- Java 21
- Docker (for Testcontainers during tests, and running PostgreSQL locally)

## How to run locally
1. Start a local PostgreSQL instance:
   ```bash
   docker run --name request-postgres -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=mypassword -e POSTGRES_DB=request_db -p 5432:5432 -d postgres:15
   ```
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## How to test
- Run tests (Uses Testcontainers for integration tests):
  ```bash
  ./gradlew test
  ```

## Sample Requests (Postman / cURL)
You can directly import these cURL commands into Postman by selecting **Import -> Raw text** and pasting the command. 
Ensure the application is running locally on `localhost:8080`.

*(Note: Replace `YOUR_UUID_HERE` with the actual UUID returned when creating the request).*

### 1. Create a Request
```bash
curl --location --request POST 'http://localhost:8080/api/requests' \
--header 'X-Idempotency-Key: 123e4567-e89b-12d3-a456-426614174000' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "First Request",
    "content": "This is the content of my first request."
}'
```

### 2. Edit a Request
```bash
curl --location --request PUT 'http://localhost:8080/api/requests/YOUR_UUID_HERE' \
--header 'Content-Type: application/json' \
--data-raw '{
    "content": "Updated content goes here."
}'
```

### 3. Verify a Request
```bash
curl --location --request POST 'http://localhost:8080/api/requests/YOUR_UUID_HERE/verify' \
--header 'X-Idempotency-Key: 123e4567-e89b-12d3-a456-426614174001' \
--header 'Content-Type: application/json'
```

### 4. Accept a Request
```bash
curl --location --request POST 'http://localhost:8080/api/requests/YOUR_UUID_HERE/accept' \
--header 'X-Idempotency-Key: 123e4567-e89b-12d3-a456-426614174002' \
--header 'Content-Type: application/json'
```

### 5. Publish a Request
```bash
curl --location --request POST 'http://localhost:8080/api/requests/YOUR_UUID_HERE/publish' \
--header 'X-Idempotency-Key: 123e4567-e89b-12d3-a456-426614174003' \
--header 'Content-Type: application/json'
```

### 6. Reject a Request (Requires Reason Enum)
```bash
curl --location --request POST 'http://localhost:8080/api/requests/YOUR_UUID_HERE/reject' \
--header 'X-Idempotency-Key: 123e4567-e89b-12d3-a456-426614174004' \
--header 'Content-Type: application/json' \
--data-raw '{
    "reason": "INAPPROPRIATE_CONTENT"
}'
```

### 7. Delete a Request (Requires Reason Enum)
```bash
curl --location --request DELETE 'http://localhost:8080/api/requests/YOUR_UUID_HERE' \
--header 'Content-Type: application/json' \
--data-raw '{
    "reason": "NOT_NEEDED"
}'
```

### 8. List Requests (with Pagination and Filters)
```bash
curl --location --request GET 'http://localhost:8080/api/requests?page=0&size=10&name=First%20Request&state=CREATED'
```
