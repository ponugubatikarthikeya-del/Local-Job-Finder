# Interview preparation

## Project walkthrough

Local Job Finder is a Spring Boot REST backend that lets users create jobs, discover open listings near supplied coordinates, apply to jobs, and maintain profiles. A typical request enters through a controller, passes through stateless JWT authentication where required, executes business rules in a transactional service, and persists via Spring Data JPA to MySQL. Flyway manages schema changes. Redis caches selected reads, while async email and a ShedLock-coordinated scheduler handle OTP delivery and account cleanup.

## Design decisions visible in the code

- **Layered design:** controllers handle HTTP concerns, services own workflow rules, repositories encapsulate persistence, and DTOs separate API payloads from entities.
- **Stateless request authentication:** signed JWT bearer tokens are used with Spring Security's stateless session policy. Refresh tokens are rotated and matched against token rows in MySQL.
- **Database-managed schema:** Flyway migrations define MySQL tables, indexes, foreign keys, JSON, and spatial data. Job discovery applies a JPA specification using `ST_Distance_Sphere`.
- **Read caching:** Redis stores selected job, applicant, and profile responses. Cache invalidation is deferred until transaction commit; cache failures are logged and configured to fall back for reads.
- **Scheduled maintenance:** startup and midnight cleanup removes unverified users older than 24 hours; ShedLock coordinates a single execution across instances.
- **Email outside long database work:** mail dispatch uses an async executor with bounded pools and caller-runs backpressure.

## Questions to prepare for

1. How does the request flow from a controller to a persistent change, and where are validation and ownership checks enforced?
2. Why separate access and refresh tokens, and how does refresh-token rotation use the database?
3. How would you test geospatial filtering, coordinate boundary validation, and distance output?
4. What consistency problem does cache invalidation after commit avoid? What happens if Redis is unavailable?
5. Why use Flyway alongside Hibernate, and how would you handle a migration against an existing production database?
6. How does ShedLock prevent two application instances from running the cleanup at the same time?
7. What constraints prevent duplicate applications, and how would you handle concurrent attempts to apply?
8. Which parts should be covered by unit, repository, MVC/security, migration, and integration tests?

## Known gaps and improvement areas

These are findings from the current source rather than planned features:

- **Token handling:** token values are stored in columns named `hash_access_token` and `hash_refresh_token`, but login/refresh code writes the bearer-token strings directly. The JWT filter verifies signature and expiry but does not consult session revocation in the database. Logout therefore does not immediately invalidate already-issued access JWTs. Store token digests and define a deliberate access-token revocation strategy.
- **Job-search cache key:** its key includes user, text, category, salary, page, and size, but not latitude, longitude, or radius. Different locations can collide in the same cached result. Include all result-affecting criteria or avoid caching that query until its key is complete.
- **Schema alignment:** `ProfilesEntity.dateOfBirth` is a `String` while V2 declares `DATE`. `ShedLockEntity` maps `lockUntil` to `lock_type`, whereas ShedLock SQL uses `lock_until` and V12 adds `lock_type`. Compare entity mappings, migration history, and the provider's actual table usage.
- **Relationship constraints:** the JPA model uses one-to-one mappings for profiles and active listings, but their foreign-key columns are not unique in the SQL migrations. The database therefore does not enforce those cardinalities.
- **ShedLock table name:** the JDBC provider is created without an explicit table name, while migrations create `shed_lock`; verify the provider resolves to that same table.
- **Device uniqueness:** `tokens.device_id` is globally unique as well as unique with `user_id`; this prevents a device ID from being reused by another account and may not match the intended per-user session model.
- **API hardening:** `/user/auth/**` is fully permitted, including logout, although logout expects an authenticated identity. `/user/profiles/public-profile/{jobId}` is protected by the catch-all rule. Clarify and align route names, matchers, and semantics.
- **Cross-origin policy:** CORS allows all origins under `/user/**`; restrict before deployment.
- **Feature gaps:** OAuth/social login, push notifications/FCM, and review submission are commented out or absent. The ratings-summary table/entity alone does not implement a review feature.
- **Testing:** only a context-load smoke test and Redis serializer tests are present. There are no visible service/controller, authentication, migration, or MySQL spatial integration tests.
- **Operational settings:** SQL logging is enabled, required local connection values are empty by default, and Flyway repair-on-migrate is configured. Review logging, secret provisioning, schema behavior, observability, and deployment policy before production.

These issues should be resolved and tested before claiming production readiness.
