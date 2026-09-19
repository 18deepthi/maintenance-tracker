# Maintenance Work-Order Tracker

> **Project Disclaimer**: This is a self-directed portfolio project designed to demonstrate clean backend engineering practices targeting entry-level Java development roles. It operates entirely on synthetic sample data for industrial equipment maintenance. It contains no simulated production metrics, artificial user scale, or claims of high-load deployment.

---

## Overview

The **Maintenance Work-Order Tracker** is a Spring Boot REST API for managing industrial equipment maintenance workflows (e.g., transformers, power distribution panels, telemetry sensors). It allows maintenance supervisors and field engineers to track work orders from initial creation through execution and final sign-off.

---

## Technology Stack

- **Language & Runtime**: Java 17 (Java 21 compatible)
- **Framework**: Spring Boot 3.3.x
- **Persistence**: Spring Data JPA, Hibernate 6
- **Databases**: 
  - H2 Database (in-memory for local development and automated testing)
  - PostgreSQL (target database for production deployment)
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test (78 automated tests)
- **Code Quality**: Maven Checkstyle Plugin (0 violations)
- **Documentation**: OpenAPI 3 / Swagger UI (`springdoc-openapi`)

---

## API Endpoints Overview

Base path: `/api/v1/work-orders`

| Method | Endpoint | Description | Success Status | Key Error Statuses |
|--------|----------|-------------|----------------|-------------------|
| `POST` | `/api/v1/work-orders` | Create a new work order (defaults to `OPEN` status) | `201 Created` | `400 Bad Request` |
| `GET` | `/api/v1/work-orders/{id}` | Get work order details by ID | `200 OK` | `404 Not Found` |
| `GET` | `/api/v1/work-orders` | List work orders with pagination, sorting, and filtering | `200 OK` | `400 Bad Request` |
| `PUT` | `/api/v1/work-orders/{id}` | Update core work order details (only while `OPEN`) | `200 OK` | `400`, `404`, `409 Conflict` |
| `PATCH` | `/api/v1/work-orders/{id}/status` | Transition status (`OPEN -> IN_PROGRESS -> COMPLETED -> CLOSED`) | `200 OK` | `400`, `404`, `409 Conflict` |
| `PATCH` | `/api/v1/work-orders/{id}/assignment` | Assign, reassign, or unassign work order | `200 OK` | `400`, `404`, `409 Conflict` |

### Query Parameters for List Endpoint

- **Pagination**: `page` (0-indexed, default: `0`), `size` (default: `20`, maximum: `100`).
- **Sorting**: `sort` (e.g., `sort=createdAt,desc` or `sort=title,asc`). Supported fields: `id`, `title`, `description`, `equipmentName`, `equipmentId`, `location`, `createdBy`, `assignedTo`, `status`, `createdAt`, `updatedAt`. Any invalid sort field returns `400 Bad Request`.
- **Filtering**:
  - `status`: Filter by work order status (`OPEN`, `IN_PROGRESS`, `COMPLETED`, `CLOSED`).
  - `assignedTo`: Exact match (case-insensitive) on assigned engineer username.
  - `equipmentName`: Substring match (case-insensitive) with automatic SQL wildcard character escaping.
  - `equipmentId`: Exact match (case-insensitive) on equipment identifier.

---

## Standardized Error Response

All error responses across the API return a uniform JSON schema:

```json
{
  "timestamp": "2026-09-19T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/work-orders",
  "fieldErrors": [
    {
      "field": "title",
      "message": "Title is required",
      "rejectedValue": ""
    }
  ]
}
```

- **400 Bad Request**: Input validation failures, malformed JSON, invalid enum values, invalid sort fields, or illegal pagination parameters.
- **404 Not Found**: Work order not found or unrecognized endpoint route.
- **405 Method Not Allowed**: HTTP method not supported for target resource.
- **409 Conflict**: Invalid status transitions, invalid assignment operations, modifying non-`OPEN` work orders, or concurrent optimistic locking conflicts.
- **415 Unsupported Media Type**: Request Content-Type not supported.
- **500 Internal Server Error**: Unexpected server exceptions (logged with full stack trace on server, returning generic sanitized message to client).

---

## Concurrency & Data Integrity

- **Optimistic Locking**: The `WorkOrder` entity includes a JPA `@Version` column. Concurrent update collisions trigger an `ObjectOptimisticLockingFailureException`, which is automatically translated to `409 Conflict`.
- **Audit Timestamps**: `createdAt` and `updatedAt` are managed automatically in UTC. `createdAt` is immutable (`updatable = false`).

---

## Interactive API Documentation (Swagger UI)

Interactive OpenAPI 3 documentation is automatically generated via SpringDoc OpenAPI.

### Running the Application

```bash
mvn spring-boot:run
```

Once started, access:
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI 3 JSON Specification**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Getting Started & Testing

### Prerequisites

- Java 17 or higher
- Apache Maven 3.8+

### Build & Run Tests

```bash
# Compile and run full test suite (78 tests)
mvn clean test

# Run Checkstyle audit (0 violations)
mvn checkstyle:check
```