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