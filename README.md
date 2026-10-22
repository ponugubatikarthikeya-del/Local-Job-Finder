# Local Job Finder — Backend

A Java REST API for a location-based part-time job matching application. The backend enables users to manage job listings, discover opportunities using location and search criteria, submit applications, and maintain their profiles.

Built with **Java 21, Spring Boot, and MySQL**, with supporting components for authentication, caching, database migrations, and asynchronous email delivery.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Database and Migrations](#database-and-migrations)
- [Getting Started](#getting-started)
- [Start the Application](#start-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Documentation](#project-documentation)
- [Related Project](#related-project)

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

The current tests include a Spring application context-load smoke test and Redis JSON serializer round-trip tests. They do not constitute end-to-end verification of the MySQL, Redis, SMTP, or API workflows.

## Project Documentation

- [Architecture](docs/ARCHITECTURE.md) — component structure and request flows.
- [API Reference](docs/API.md) — endpoint documentation.
- [Database and Migrations](docs/DATABASE.md) — schema and migration details.
- [Development Setup](docs/DEVELOPMENT.md) — local configuration and execution.

## Related Project

The Android client is maintained in a separate repository.

- Android Frontend : [local job finder frontend](https://github.com/ponugubatikarthikeya-del/local-job-finder-frontend)
