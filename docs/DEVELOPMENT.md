# Development

## Prerequisites

- Java 21 JDK.
- Git and PowerShell on Windows.
- MySQL with support for the spatial functions/types used by the application.
- Redis, configured as the Spring cache store.
- An SMTP account/server if testing signup or password-reset email delivery.

The checked-in Maven Wrapper downloads Maven 3.9.14; a separate Maven installation is not needed.

## Configuration

The application reads these environment variables through `application.properties`; do not put real credentials in documentation or commit local secrets.

| Variable | Purpose | Default in source |
|---|---|---|
| `SERVER_PORT` | HTTP server port | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL for MySQL | Empty; configure before starting |
| `SPRING_DATASOURCE_USERNAME` | MySQL username | Empty |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password | Empty |
| `JPA_DDL_AUTO` | Hibernate DDL mode | Empty; configure to match the local environment |
| `JWT_SECRET` | JWT signing secret; must resolve to at least 32 characters (or valid Base64 decoding to at least 32 bytes) | Empty |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD` | SMTP host, port, user, and password | Empty |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Redis connection settings | Empty |
| `REDIS_DATABASE` | Redis logical database | `0` |
| `REDIS_TIMEOUT` | Redis operation timeout | `2000ms` |
| `REDIS_POOL_MAX_ACTIVE` | Maximum pooled Redis connections | `16` |
| `REDIS_POOL_MAX_IDLE` | Maximum idle Redis connections | `8` |
| `REDIS_POOL_MIN_IDLE` | Minimum idle Redis connections | `2` |
| `REDIS_POOL_MAX_WAIT` | Maximum pool wait | `1000ms` |
| `REDIS_CACHE_PREFIX` | Environment/version namespace for cache keys | Application name, active profile, and `v1` |
| `REDIS_CACHE_CLEAR_SCAN_BATCH_SIZE` | Batch size used when scanning keys during cache clear | `1000` |
| `REDIS_CACHE_MONITORING_ENABLED` | Enables cache monitoring configuration | `true` |
| `REDIS_CACHE_MONITORING_INTERVAL` | Monitoring interval | `5m` |
| `REDIS_CACHE_MONITORING_INITIAL_DELAY` | Monitoring initial delay | `1m` |

Email SMTP authentication and STARTTLS are enabled in source. Use the actual host/provider values for your development account. No `.env` loading mechanism is configured by the application; provide variables through your shell or IDE run configuration.

## Local setup (PowerShell)

1. Start MySQL and Redis, then create an empty MySQL database, for example `local_job_finder`.
2. Set the necessary variables in the same PowerShell session. Example values below are placeholders; replace them locally and do not commit credentials:

   ```powershell
   $env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/local_job_finder"
   $env:SPRING_DATASOURCE_USERNAME = "<your-mysql-user>"
   $env:SPRING_DATASOURCE_PASSWORD = "<your-mysql-password>"
   $env:JPA_DDL_AUTO = "validate"
   $env:JWT_SECRET = "<at-least-32-random-characters>"
   $env:REDIS_HOST = "localhost"
   $env:REDIS_PORT = "6379"
   ```

3. From the repository root, run:

   ```powershell
   .\mvnw.cmd test
   .\mvnw.cmd spring-boot:run
   ```

Flyway runs migrations at application startup. Configure SMTP variables before exercising signup/password reset; set Redis password only when the local Redis instance requires authentication. `JPA_DDL_AUTO=validate` is a reasonable local value because Flyway owns schema changes, but confirm schema compatibility first.

## Useful commands

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd package
```

The current tests comprise a Spring context-load smoke test and Redis JSON serializer round-trip tests. The context test needs dependencies and required application configuration to be available; passing compilation alone does not prove end-to-end MySQL/Redis/SMTP behavior.

## Configuration cautions

- `spring.jpa.show-sql=true` is enabled in the current properties. Avoid enabling SQL output in environments where query values could be sensitive.
- The configured JWT secret must be strong, private, and kept outside source control.
- CORS currently allows any origin for `/user/**`; restrict it to trusted origins before deployment.
- The property for Flyway repair is enabled in source; evaluate whether this behavior matches deployment policy before production use.
