# Local Job Finder — Backend

A Java REST API for a location-based part-time job matching application. The backend enables users to manage job listings, discover opportunities using location and search criteria, submit applications, and maintain their profiles.

Built with **Java 21, Spring Boot, and MySQL**, with supporting components for authentication, caching, database migrations, and asynchronous email delivery.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Database and Migrations](#database-and-migrations)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Documentation](#project-documentation)
- [Known Limitations](#known-limitations)
- [Future Improvements](#future-improvements)

## Overview

Finding suitable local part-time jobs can be difficult when opportunities are scattered across different sources. Local Job Finder aims to simplify this process by providing APIs for job discovery, listing management, and application tracking.

This repository contains the **backend REST API only**. The Android frontend is maintained separately.

## Features

### Authentication and Account Management
- Account signup with email OTP verification.
- Password-based login using JWT authentication.
- Refresh-token rotation.
- Password-reset workflow.
- Device-session logout.

### Job Management and Search
- Create and manage job listings.
- Soft-delete and complete job listings.
- Paginated job search with supported location, text, category, and minimum-pay filters.

### Job Applications
- Submit applications for jobs.
- Allow job owners to review applicants.
- Accept or reject applications with owner authorization.

### User Profiles
- Retrieve and update user profile information.
- Store geocoded city-location details.

### Caching and Background Processing
- Redis-backed response caching with cache invalidation.
- Cache-error handling and logging for Redis failures.
- Asynchronous email delivery for verification and password-reset workflows.
- Scheduled cleanup of unverified accounts, coordinated across application instances using ShedLock.

> **Implementation note:** Feature descriptions reflect the current project documentation and source review. Verify each workflow and its authorization rules with tests before treating it as production-ready.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.4 |
| Build tool | Maven Wrapper |
| Web layer | Spring MVC |
| Security | Spring Security, JJWT |
| Persistence | Spring Data JPA, Hibernate Spatial |
| Database | MySQL |
| Database migrations | Flyway |
| Caching | Redis, Lettuce |
| Validation | Jakarta Bean Validation |
| Email | Spring Mail |
| Object mapping | MapStruct |
| Scheduled task coordination | ShedLock |

Versions listed here should be kept aligned with the project's Maven configuration.

## Architecture

The application follows a layered Spring Boot architecture.

```text
Android Client / API Consumer
             |
             v
       REST Controllers
             |
             v
      Service Layer
             |
       +-----+------+
       |            |
       v            v
 Repositories   Supporting Services
       |        - Authentication
       v        - Email delivery
   MySQL        - Cache management
                - Scheduled cleanup

Redis supports caching.
Flyway manages database schema migrations.
Spring Security protects authenticated endpoints.
```

This diagram is a high-level representation. Refer to the source code and [Architecture Documentation](docs/ARCHITECTURE.md) for the actual component boundaries and request flows.

## Database and Migrations

MySQL stores application data, while Flyway manages versioned database schema changes.

The project also uses Hibernate Spatial for spatial data functionality.

See [Database and Migrations](docs/DATABASE.md) for the documented schema, relationships, and migration details.

## Getting Started

### Prerequisites

Install or configure the following:

- Java Development Kit (JDK) 21.
- Git.
- MySQL, with a database created for the application.
- Redis, if required by the active application configuration.
- SMTP credentials for email verification and password-reset workflows.

Check the project's Maven configuration and development documentation for exact versions, database settings, and required environment variables.

### Clone the Repository

```bash
git clone <YOUR_BACKEND_REPOSITORY_URL>
cd <YOUR_BACKEND_REPOSITORY_DIRECTORY>
```

Replace the placeholders with your actual GitHub repository URL and directory name.

### Configure Environment Variables

Follow [Development Setup and Configuration](docs/DEVELOPMENT.md) to configure database connectivity, authentication secrets, Redis, and SMTP.

Use environment variables or an appropriate local configuration mechanism for sensitive values. Never commit real passwords, JWT secrets, SMTP credentials, or production configuration.

### Run Tests

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
./mvnw test
```

Record the actual test results before reporting whether the test suite passes.

### Start the Application

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The application uses port `8080` by default, unless overridden by configuration.

Flyway manages schema migrations during application startup when enabled in the active configuration. Email-dependent workflows require valid SMTP settings.

## API Documentation

The API reference documents the available endpoints, request parameters, authentication requirements, and response formats.

See [API Reference](docs/API.md).

Use the documentation alongside the controller implementations to verify the current endpoint paths and supported behavior.

## Testing

Run the test suite using the Maven Wrapper:

```bash
.\mvnw.cmd test
```

The project currently has limited test coverage. Expand testing to cover:

- Authentication and token lifecycle.
- Role-based authorization.
- Job creation, search, and lifecycle transitions.
- Application submission and owner decisions.
- Validation failures and error responses.
- Database migrations and persistence behavior.
- Cache invalidation and Redis failure handling.

These are recommended test areas, not claims that all such tests currently exist.

## Project Documentation

- [Architecture](docs/ARCHITECTURE.md) — component structure and request flows.
- [API Reference](docs/API.md) — endpoint documentation.
- [Database and Migrations](docs/DATABASE.md) — schema and migration details.
- [Development Setup](docs/DEVELOPMENT.md) — local configuration and execution.
- [Interview Preparation and Project Gaps](docs/INTERVIEW_PREP.md) — technical decisions, known gaps, and improvement areas.

## Known Limitations

This project is under active development and is **not yet claimed to be production-ready**.

- Test coverage is limited.
- Some implementation and schema concerns remain unresolved.
- OAuth/social login, push notifications, and an operational review-submission API are not implemented in the current source.

See [Known Gaps and Improvement Areas](docs/INTERVIEW_PREP.md#known-gaps-and-improvement-areas) for further details.

## Future Improvements

Potential next steps include:

- Expand automated unit and integration tests.
- Review authorization rules and token lifecycle security.
- Resolve documented schema and implementation concerns.
- Add CI checks for builds and tests.
- Improve operational logging, monitoring, and deployment documentation.

These are planned improvements, not currently implemented features.

## Related Project

The Android client is maintained in a separate repository.

- **Android Frontend:** Add your actual frontend repository URL here.

## License

No license has been specified in this README. Add a `LICENSE` file only after choosing terms that match your intended use and distribution of the code.
