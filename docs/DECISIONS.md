# Architecture Decision Records (ADRs)

This document records the architectural and design decisions made throughout the project lifecycle. Each decision is documented concisely (2-3 lines of rationale) to provide clear talking points for technical interviews.

---

### ADR-001: Layered Architecture with DTO Boundary
- **Decision**: Structure the backend strictly into Controller -> Service -> Repository layers, using dedicated Data Transfer Objects (DTOs) for request/response payloads rather than exposing JPA entities directly.
- **Rationale**: Isolates web and serialization concerns from the domain model, prevents inadvertent database modifications via mass assignment, and allows business rules in the Service layer to be tested independently using Mockito.

---

### ADR-002: Dual Database Strategy (H2 for Dev/Test, PostgreSQL for Production)
- **Decision**: Configure an in-memory H2 database for local development and automated testing, while providing standard PostgreSQL driver support for deployment environments.
- **Rationale**: Ensures fast, isolated test execution with zero external installation prerequisites for peer reviewers, while leveraging JPA/Hibernate abstractions to ensure compatibility with relational PostgreSQL in production.

---

### ADR-003: Checkstyle Integration with Phased Enforcement
- **Decision**: Integrate `maven-checkstyle-plugin` with standard formatting and import conventions, configured initially with `failOnViolation=false` during early development.
- **Rationale**: Establishes visibility into code consistency and formatting standards without impeding early architectural prototyping, with strict build failure enforcement planned for the GitHub Actions CI pipeline in Phase 4.

---

### ADR-004: Detail Modification Safeguards via Status Check (FR-8)
- **Decision**: Enforce that core work order details (title, description, equipmentName, equipmentId, location) can be modified strictly while the status is `OPEN`, rejecting updates in subsequent states with HTTP 409 Conflict.
- **Rationale**: Preserves audit integrity by ensuring work order parameters cannot be altered once execution begins in the field, preventing discrepancies between logged scope and physical maintenance.

---

### ADR-005: Custom PageResponse DTO & Page Size Upper Bound
- **Decision**: Encapsulate paginated list responses inside a dedicated `PageResponse<T>` DTO rather than returning Spring Data's `Page<T>` directly, while clamping page size to a maximum of 100 in the service layer.
- **Rationale**: Decouples API client contracts from Spring-specific serialization formats, prevents unexpected contract breakage on framework upgrades, and safeguards server memory against unbounded query loads.

---

### ADR-006: Centralized State Transition & Assignment Lifecycle Rules (FR-3, FR-4, FR-5)
- **Decision**: Model work order lifecycle transitions and assignment rules strictly in the Service layer using an immutable transition map (`ALLOWED_TRANSITIONS`) and explicit validation checks, throwing `InvalidWorkOrderStateException` (HTTP 409 Conflict) on violations.
- **Rationale**: Keeps state progression strictly one-way (`OPEN -> IN_PROGRESS -> COMPLETED -> CLOSED`) and prevents invalid transitions, unassigned execution, or post-completion mutations at a single source of truth without scattering state logic across controllers or entities.

---

### ADR-007: Composable Query Filtering via JPA Specifications & Wildcard Escaping (FR-9)
- **Decision**: Implement dynamic query filtering across `status`, `assignedTo`, `equipmentName`, and `equipmentId` using Spring Data `JpaSpecificationExecutor` with composable predicates. Match `equipmentName` using case-insensitive contains (with `%` and `_` wildcard characters escaped to treat them as literals), while matching `equipmentId` and `assignedTo` using exact case-insensitive equality.
- **Rationale**: Avoids a combinatorial explosion of derived repository query methods, ensures full composability with pagination and sorting, and eliminates SQL injection or accidental wildcard pattern expansion when searching equipment names.

---

### ADR-008: Centralized API Error Handling via RestControllerAdvice (NFR-3)
- **Decision**: Centralize all application and framework exception handling in `GlobalExceptionHandler` (`@RestControllerAdvice`) extending `ResponseEntityExceptionHandler`. Return a unified `ErrorResponse` schema (`timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`) for client errors (400, 404, 405, 409, 415) and unexpected errors (500). The catch-all handler logs the full exception with stack trace on the server (`logger.error`) while returning a sanitized generic message to clients without exposing internal stack traces, Jackson/Java class names, or requirement IDs.
- **Rationale**: Establishes a predictable error contract for frontend integration, prevents sensitive implementation detail leakage, and preserves standard Spring framework HTTP statuses (such as 404 for unknown endpoints, 405 for unsupported HTTP methods, and 415 for unsupported media types).

---

### ADR-009: Optimistic Concurrency Control via JPA @Version
- **Decision**: Add a `@Version Long version` attribute to the `WorkOrder` entity, mapped to a `version` column in the database. When concurrent updates conflict, Spring Data JPA / Hibernate raises an `ObjectOptimisticLockingFailureException`, which is caught and mapped to HTTP 409 Conflict. In v1, clients do not send a version token in request payloads, so conflict detection operates between concurrent server transactions only; client-side ETag / version tokens are deferred to future API iterations.
- **Rationale**: Prevents silent lost updates and race conditions during concurrent status transitions or assignment updates without requiring heavy database row locking (pessimistic locks), maintaining high throughput and transactional safety.
---

### ADR-010: GitHub Actions CI Pipeline & Containerized Multi-Stage Dockerfile (NFR-4, NFR-5)
- **Decision**: Implement an automated GitHub Actions CI workflow triggered on all pull requests and pushes to `main` running `mvn clean verify` with strict Checkstyle enforcement (`failOnViolation=true` including test sources) and test report artifact upload on failure. Provide a containerized multi-stage `Dockerfile` utilizing dependency pre-fetching (`mvn dependency:go-offline`), a minimal JRE 17 Alpine runtime, non-root execution (`appuser`), and default in-memory H2 persistence.
- **Rationale**: Guarantees automated regression testing and style compliance on every contribution before merging, while the containerized multi-stage build creates a minimal, secure deployment image that runs out-of-the-box without requiring an external database.