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
  - Marks work orders as `COMPLETED` upon finishing repairs, recording relevant maintenance notes.

---

## 3. User Stories

### Epic 1: Work Order Lifecycle & Management
- **US-1.1**: As a supervisor, I want to create a work order with a title, description, equipment identifier, and location so that a maintenance task is officially recorded.
- **US-1.2**: As a supervisor or engineer, I want to retrieve details of a specific work order by ID so that I can inspect its complete history and current status.
- **US-1.3**: As a supervisor or engineer, I want to list all work orders with sorting and pagination so that I can navigate existing work efficiently.
- **US-1.4**: As a supervisor, I want to update work order details (such as description or location) while the order is still open.

### Epic 2: State Transitions
- **US-2.1**: As a field engineer, I want to transition a work order from `OPEN` to `IN_PROGRESS` when I begin physical work on the equipment.
- **US-2.2**: As a field engineer, I want to transition a work order from `IN_PROGRESS` to `COMPLETED` when maintenance is finished.
- **US-2.3**: As a supervisor, I want to transition a work order from `COMPLETED` to `CLOSED` after verifying that the work meets quality and safety standards.
- **US-2.4**: As a user, I want the system to reject any invalid state transitions (e.g., `OPEN` directly to `CLOSED`, or transitions from terminal states) with an explanatory error message.

### Epic 3: Assignment & Search
- **US-3.1**: As a supervisor, I want to assign an open or in-progress work order to a specific engineer by identifier so that ownership is clear.
- **US-3.2**: As a field engineer, I want to filter work orders assigned specifically to me so that I can focus on my queue.
- **US-3.3**: As a supervisor, I want to filter work orders by status (e.g., all `OPEN` orders) or equipment name so that I can monitor bottlenecks.

---

## 4. Functional Requirements

- **FR-1 (Creation)**: The system shall allow creating a work order with `title`, `description`, `equipmentName`, `equipmentId`, `location`, and `createdBy`. Newly created work orders must default to `OPEN` status.
- **FR-2 (Auto-Timestamps)**: The system shall automatically record `createdAt` and `updatedAt` timestamps in UTC for every work order.
- **FR-3 (Status Workflow)**: The system shall enforce the following sequential lifecycle:
  `OPEN` -> `IN_PROGRESS` -> `COMPLETED` -> `CLOSED`.
- **FR-4 (Transition Enforcement)**: The system shall reject any out-of-sequence status transition and return an informative HTTP 400 Bad Request or HTTP 409 Conflict.
- **FR-5 (Assignment)**: The system shall allow updating the `assignedTo` field of a work order.
- **FR-6 (Listing & Retrieval)**: The system shall provide endpoints to fetch a single work order by ID and list all work orders.
- **FR-7 (Filtering)**: The system shall support filtering the list of work orders by `status`, `assignedTo`, and `equipmentId`.
- **FR-8 (Data Validation)**: The system shall validate required fields (non-blank title, non-blank equipment details) and return descriptive validation errors.

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
- User authentication and authorization (no JWT, Spring Security, or session tokens; worker IDs are passed as plain strings).
- Priority or severity classification (omitted from v1).
- File/photo attachments for work orders.
- Real-time IoT sensor telemetry ingestion or alerting.
- Outgoing notifications (SMS, email, webhooks).

---

## 7. Assumptions

1. The system is deployed in an internal network environment where worker identifiers (`createdBy`, `assignedTo`) are supplied by trusted client requests.
2. All timestamps are captured and stored in standard UTC.
3. Equipment identifiers (`equipmentId`) follow an organization-defined alphanumeric naming convention (e.g., `TR-101`, `PNL-04`).

---

## 8. Clarifying Questions for Review & Refinement

The following 5 questions are staged for review. You can edit this section directly with your requirements:

### Q1: Equipment Entity vs. Value Object
- **Question**: Should industrial equipment in v1 be modeled as embedded string attributes on the work order (`equipmentId`, `equipmentName`, `location`), or as a distinct relational `Equipment` entity with a foreign key relationship?
- **Decision / User Response**: *(Edit here)*

### Q2: Assignee Representation
- **Question**: Should assignees and creators be modeled purely as identifier strings (e.g., `"eng_patel"`), or is a dedicated `User` entity needed in v1?
- **Decision / User Response**: *(Edit here)*

### Q3: Terminal Status & Reopening
- **Question**: Is `CLOSED` strictly a terminal, immutable state, or should supervisors have the capability to reopen a closed work order if an issue recurs?
- **Decision / User Response**: *(Edit here)*

### Q4: Assignment Status Coupling
- **Question**: Can an order remain `OPEN` when assigned, or does assigning an engineer automatically transition the order to `IN_PROGRESS`?
- **Decision / User Response**: *(Edit here)*

### Q5: Location Granularity
- **Question**: Is location represented as a simple descriptive string (e.g., `"Substation 4, Bay B"`), or does it require structured hierarchical fields (site, building, room)?
- **Decision / User Response**: *(Edit here)*