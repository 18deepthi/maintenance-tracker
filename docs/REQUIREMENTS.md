# Requirements Specification: Maintenance Work-Order Tracker

## 1. Purpose & Scope
The Maintenance Work-Order Tracker is a backend service for industrial facilities to record, assign, and track maintenance tasks performed on physical equipment (such as step-down transformers, motor control panels, and vibration sensors). 

The primary goal of v1 is to provide a dependable, well-tested REST API implementing strict lifecycle state management, validation, and search capabilities.

---

## 2. Target Users & Personas

- **Maintenance Supervisor**:
  - Creates new maintenance work orders when equipment requires routine inspection or unscheduled repairs.
  - Reviews open work orders and assigns them to qualified field engineers.
  - Tracks work order progress and formally closes completed orders.

- **Field Engineer**:
  - Views assigned work orders and inspects task requirements and equipment details.
  - Updates work order status to `IN_PROGRESS` upon commencing work.
  - Marks work orders as `COMPLETED` upon finishing repairs.

---

## 3. User Stories

### Epic 1: Work Order Lifecycle & Management
- **US-1.1**: As a supervisor, I want to create a work order with a title, description, equipment name, optional equipment ID, and location so that a maintenance task is officially recorded.
- **US-1.2**: As a supervisor or engineer, I want to retrieve details of a specific work order by ID so that I can inspect its current details and current status.
- **US-1.3**: As a supervisor or engineer, I want to list all work orders with sorting and pagination so that I can navigate existing work efficiently.
- **US-1.4**: As a supervisor, I want to update work order details while the order is still in `OPEN` status.

### Epic 2: State Transitions
- **US-2.1**: As a field engineer, I want to transition a work order from `OPEN` to `IN_PROGRESS` when I begin physical work on the equipment.
- **US-2.2**: As a field engineer, I want to transition a work order from `IN_PROGRESS` to `COMPLETED` when maintenance is finished.
- **US-2.3**: As a supervisor, I want to transition a work order from `COMPLETED` to `CLOSED` after verifying that the work meets quality and safety standards.
- **US-2.4**: As a user, I want the system to reject any invalid state transitions (e.g., `OPEN` directly to `CLOSED`, or transitions from terminal states) with an explanatory error message.

### Epic 3: Assignment & Search
- **US-3.1**: As a supervisor, I want to assign an open or in-progress work order to a specific engineer by identifier so that ownership is clear.
- **US-3.2**: As a field engineer, I want to filter work orders assigned specifically to me so that I can focus on my queue.
- **US-3.3**: As a supervisor, I want to filter work orders by status, assigned engineer, equipment name, or equipment ID so that I can monitor bottlenecks.

---

## 4. Functional Requirements

- **FR-1 (Creation)**: The system shall allow creating a work order with `title`, `description`, `equipmentName` (required), `equipmentId` (optional), `location` (required), and `createdBy` (required). The `assignedTo` field is optional at creation. Newly created work orders must default to `OPEN` status.
- **FR-2 (Auto-Timestamps)**: The system shall automatically record `createdAt` and `updatedAt` timestamps in UTC for every work order.
- **FR-3 (Status Workflow)**: The system shall enforce the following strictly one-way sequential lifecycle:
  `OPEN` -> `IN_PROGRESS` -> `COMPLETED` -> `CLOSED`.
  `CLOSED` is a terminal status; reopening closed work orders is not permitted.
- **FR-4 (Transition Enforcement & Error Handling)**: The system shall reject any invalid or out-of-sequence status transition with HTTP 409 Conflict. Input validation failures shall return HTTP 400 Bad Request.
- **FR-5 (Assignment Rules)**: The system shall enforce the following assignment rules:
  - `assignedTo` is optional at creation.
  - Assigning a work order does not change its status.
  - A work order cannot transition from `OPEN` to `IN_PROGRESS` without an assigned engineer (`assignedTo` must not be null/blank).
  - Reassignment is permitted while status is `OPEN` or `IN_PROGRESS`.
  - Unassigning (`assignedTo` set to null) is permitted only while status is `OPEN`.
  - No reassignment or unassignment is permitted once the status is `COMPLETED` or `CLOSED`.
- **FR-6 (Listing & Retrieval)**: The system shall provide endpoints to fetch a single work order by ID and list work orders.
- **FR-7 (Pagination & Sorting)**: The list endpoint shall support pagination (page index and page size) and sorting (e.g., by `createdAt` ascending/descending).
- **FR-8 (Updating Details)**: The system shall allow updating core work order details (`title`, `description`, `equipmentName`, `equipmentId`, `location`) only while the work order is in `OPEN` status. Modification attempts in any subsequent status shall be rejected with HTTP 409 Conflict.
- **FR-9 (Filtering)**: The system shall support filtering work orders by `status`, `assignedTo`, `equipmentName`, and `equipmentId`.
- **FR-10 (Data Validation)**: The system shall validate required fields (`title`, `equipmentName`, `location`, `createdBy` must not be blank) and return HTTP 400 Bad Request with descriptive validation details upon failure.

---

## 5. Non-Functional Requirements

- **NFR-1 (Clean Architecture)**: The application must adhere strictly to a 3-tier layered architecture (Controller -> Service -> Repository). Controllers must contain no domain or persistence logic.
- **NFR-2 (Test Coverage)**: All business logic and status transition rules in the Service layer must be covered by unit tests using JUnit 5 and Mockito.
- **NFR-3 (Consistent Error Responses)**: All API error responses must adhere to a standardized schema (timestamp, HTTP status code, error message, and validation details if applicable).
- **NFR-4 (Self-Contained Execution)**: The application must execute locally without requiring an external database service installed, defaulting to in-memory H2.
- **NFR-5 (Code Style Consistency)**: Java code must comply with Checkstyle formatting rules.

---

## 6. Out-of-Scope (v1)

To maintain focus and avoid over-engineering, the following are explicitly excluded from v1:
- Priority or severity classification (omitted from v1).
- Distinct relational `Equipment` entity or equipment catalog management (equipment details are stored directly on the work order).
- User entity, authentication, and authorization (no JWT, Spring Security, or session tokens; worker IDs are passed as plain strings).
- Reopening closed work orders (`CLOSED` is terminal).
- Structured hierarchical location models (location is stored as a simple descriptive string).
- File/photo attachments for work orders.
- Real-time IoT sensor telemetry ingestion or alerting.
- Outgoing notifications (SMS, email, webhooks).

---

## 7. Assumptions

1. The system is deployed in an internal network environment where worker identifiers (`createdBy`, `assignedTo`) are supplied by trusted client requests.
2. Roles (maintenance supervisor, field engineer) are defined for business context only; v1 does not enforce role-based security on who can perform specific actions or transitions.
3. All timestamps are captured and stored in standard UTC.
4. Equipment identifiers (`equipmentId`) are optional; when provided, they follow an organization-defined alphanumeric naming convention (e.g., `TR-101`, `PNL-04`).

---

## 8. Decisions

- **Equipment**: Modeled as free-form strings directly on the work order. `equipmentName` is required, `equipmentId` is optional, and `location` is required. No dedicated `Equipment` entity in v1.
- **Users**: Modeled as plain string identifiers for `createdBy` and `assignedTo`. No dedicated `User` entity and no authentication or authorization in v1.
- **Status Workflow**: Strictly one-way sequential flow: `OPEN` -> `IN_PROGRESS` -> `COMPLETED` -> `CLOSED`. `CLOSED` is strictly terminal; reopening is not supported.
- **Location**: Modeled as a simple descriptive string (e.g., `"Substation 4, Bay B"`).
- **Assignment**:
  - `assignedTo` is optional at creation.
  - Assigning does not alter work order status.
  - Transitioning to `IN_PROGRESS` requires an assigned engineer.
  - Reassignment is permitted while `OPEN` or `IN_PROGRESS`.
  - Unassignment is permitted only while `OPEN`.
  - No reassignment or unassignment is permitted once `COMPLETED` or `CLOSED`.