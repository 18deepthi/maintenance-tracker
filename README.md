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
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test
- **Code Quality**: Maven Checkstyle Plugin
- **Documentation**: OpenAPI 3 / Swagger UI (`springdoc-openapi`)

---

## Current Status

- **Phase**: Phase 0 (Repository Skeleton, Build Toolchain & Requirements Specification)
- **Branch**: `chore/repo-init-and-phase0-setup`

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Apache Maven 3.8+ (or 3.9+)

### Build & Run Tests

```bash
# Clone the repository and checkout the working branch
git checkout chore/repo-init-and-phase0-setup

# Compile and run test suite
mvn clean test

# Run Checkstyle audit
mvn checkstyle:check
```