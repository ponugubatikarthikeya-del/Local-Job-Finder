# Local Job Finder

Local Job Finder is a Java REST API for creating local job listings, searching by location and criteria, managing applications, and maintaining user profiles. The current implementation is a Spring Boot application backed by MySQL; it is not a front-end application.

## Implemented capabilities

- Local account signup with email OTP verification, password login, refresh-token rotation, password reset, and device-session logout.
- Job creation, soft deletion, completion, and paginated filtering by location, text, category, and minimum pay.
- Job applications, owner-only applicant review, and accept/reject decisions.
- User profile reads and updates, including a geocoded city location.
- Redis-backed response caching with cache invalidation and a cache-error handler that logs Redis failures.
- Asynchronous verification/reset email delivery.
- Daily and startup cleanup of unverified accounts, coordinated across instances with ShedLock.

OAuth/social login, push notifications, and an operational review-submission API are not implemented in the current source. See [known gaps](docs/INTERVIEW_PREP.md#known-gaps-and-improvement-areas) before presenting those as features.

## Technology

Verified in the Maven build and source: Java 21, Spring Boot 4.0.4, Maven Wrapper (Maven 3.9.14), Spring MVC, Spring Security, JWT (JJWT), Spring Data JPA/Hibernate Spatial, MySQL, Flyway, Redis/Lettuce, Jakarta Bean Validation, Spring Mail, MapStruct, and ShedLock.

## Run locally

Install Java 21 and have MySQL and Redis available. Configure the environment variables described in [Development](docs/DEVELOPMENT.md), then from PowerShell run:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The application listens on port `8080` by default. Database schema changes are managed by Flyway. The signup and password-reset email flows also require valid SMTP configuration.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [API reference](docs/API.md)
- [Database and migrations](docs/DATABASE.md)
- [Development setup and configuration](docs/DEVELOPMENT.md)
- [Interview preparation and project gaps](docs/INTERVIEW_PREP.md)

## Project status

This is a working project repository, not a claim of production readiness. Test coverage is currently limited, and the docs call out implementation and schema concerns that should be addressed before production use.
