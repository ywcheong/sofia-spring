# Learnings - Sofia Foundation

## 2026-03-03 Task 1: Gradle Quality Gates (ktlint/detekt)

### Version Compatibility Findings
- **detekt 1.23.8** is compiled with Kotlin 2.0.21 - NOT compatible with Kotlin 2.2.x
- **Spring Boot 4.0.3** requires Kotlin 2.2.x - cannot downgrade Kotlin
- **detekt 2.0.0-alpha.1** is compiled with Kotlin 2.2.20 - COMPATIBLE

### Plugin ID Change in detekt 2.x
- Old ID (detekt 1.x): `io.gitlab.arturbosch.detekt`
- New ID (detekt 2.x): `dev.detekt`

### Final Version Configuration
- Kotlin: 2.2.20
- ktlint: 14.0.1 (plugin: `org.jlleitschuh.gradle.ktlint`)
- detekt: 2.0.0-alpha.1 (plugin: `dev.detekt`)

### ktlint Code Style Notes
- Empty function blocks need a comment to satisfy detekt's `EmptyFunctionBlock` rule
- Class bodies should not start with blank line (ktlint rule)
- Use `// comment` inside empty test methods like `contextLoads()`

### Task Dependencies
- `check` task depends on `ktlintCheck` and `detekt`
- Test failures due to missing datasource are EXPECTED until Task 2 (Testcontainers)

## 2026-03-03 Task 2: Testcontainers MySQL + @ServiceConnection

### Integration Test Pattern
- Use an abstract base test class (`IntegrationTestSupport`) with `@Testcontainers`
- Define a static `@Container` + `@ServiceConnection` `MySQLContainer("mysql:8.4")` in a companion object
- Let concrete `@SpringBootTest` classes extend the support class to inherit container lifecycle

### Test Configuration for JPA Schema
- Set `spring.jpa.hibernate.ddl-auto=create-drop` via `@TestPropertySource` on the support class
- This avoids creating `application-test.yaml` and keeps test-only DB schema lifecycle local to test scaffolding

### Verification Signals
- Test result XML `build/test-results/test/TEST-ywcheong.sofia.SofiaApplicationTests.xml` includes `tc.mysql:8.4` startup logs
- Context boot uses Testcontainers JDBC URL and detects MySQL 8.4 in Hibernate database info

## 2026-03-03 Task 3: Hexagonal Skeleton + Clock/Exception Base

### Package Skeleton Approach
- Create only top-level package directories first (`domain`, `application/port/in`, `application/port/out`, `application/service`, `adapter/in`, `adapter/out/persistence`, `common/config`, `common/exception`) to establish dependency boundaries before implementation files.

### Shared Baseline Components
- Add `common/config/ClockConfig.kt` with a singleton `Clock.systemUTC()` bean so time-source injection is consistent and testable across layers.
- Add `common/exception/SofiaException.kt` as an open runtime base to centralize future domain/application exception hierarchy under one root type.

### Verification Notes
- ktlint enforces non-intuitive import ordering (framework imports before `java.*` in this project rule set); run formatter when adding new Kotlin files to avoid manual ordering drift.
- Clock bean validation can be kept lightweight by asserting `Clock` zone equals `ZoneOffset.UTC` in `SofiaApplicationTests`.

## 2026-03-03 Task 4: ArchUnit Guardrails (Hexagonal + Timestamps)

### ArchUnit Rules for Early-Stage Modules
- When target layers/packages are not implemented yet, use `allowEmptyShould(true)` on package-scoped rules to keep architecture tests executable without disabling the rule intent.
- For "adapter -> application/domain only + no reverse dependency" in one `@ArchTest`, compose two rules with `CompositeArchRule.of(...).and(...)`.

### Kotlin/JUnit5 ArchUnit Setup
- Use `@AnalyzeClasses(packages = ["ywcheong.sofia"], importOptions = [DoNotIncludeTests::class])` to constrain scanning to production classes and avoid Kotlin test artifacts.
- Import `DoNotIncludeTests` from `com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests` (not from `com.tngtech.archunit.junit`).

## 2026-03-03 Task 5: Application Profiles (base/local/prod)

### Spring Boot 4.x / Hibernate 7 Naming Strategy Keys
- Use Hibernate naming strategy classes under `org.hibernate.boot.model.naming.*` in config:
  `CamelCaseToUnderscoresNamingStrategy` and `ImplicitNamingStrategyJpaCompliantImpl`.
- Legacy Spring Boot 3.x strategy classes under `org.springframework.boot.orm.jpa.hibernate.*` are not used here.

### Profile Split and JPA Defaults
- Keep global `spring.jpa.open-in-view=false` and `hibernate.jdbc.time_zone=UTC` in base `application.yaml`.
- Set local datasource in `application-local.yaml` with explicit MySQL UTC URL and `ddl-auto=update`.
- Keep prod datasource out of `application-prod.yaml` and set `ddl-auto=validate` with guard note about schema migration dependency.


## 2026-03-03 Task 6: Local MySQL 8.4 Docker Compose Infrastructure

### Docker Compose Configuration for MySQL 8.4
- Use `mysql:8.4` official image with explicit timezone and charset configuration
- Set `TZ=UTC` environment variable plus `--default-time-zone=+00:00` command flag to enforce UTC throughout
- Configure utf8mb4 charset and utf8mb4_unicode_ci collation via command flags: `--character-set-server=utf8mb4`, `--collation-server=utf8mb4_unicode_ci`
- Healthcheck uses `mysqladmin ping -h localhost` with 5s interval, 5s timeout, 10 retries, and 30s start period
- Named volume `mysql_data` persists database data across container restarts
- Expose port 3306 for local development (matches Spring Boot `application-local.yaml` datasource config)

### Verification Workflow
- `docker compose up -d`: Creates network, volume, and starts MySQL container in background
- `docker compose ps`: Shows container transitions from `health: starting` to `healthy` (~25-30s startup)
- `docker compose down -v`: Cleanly stops container and removes volume for reset (verified works)
- Final `docker compose up -d` leaves container running for ongoing development

### Credential Strategy for Local Development
- Hardcoded credentials (database/username/password all set to `sofia`) in docker-compose.yml
- No `.env` file needed for local development - simpler setup, acceptable for dev environment
- These credentials match `application-local.yaml` datasource configuration from Task 5

## 2026-03-03 Task 7: SystemPhase E2E (REST + Persistence + Auditing + Envers)

### Kotlin package naming + ktlint constraint
- Package paths that include Kotlin keywords (e.g., `application.port.in`, `adapter.in`) must use escaped package declarations (``package ... .`in` ...``) and require file-level suppression `@file:Suppress("ktlint:standard:package-name")` to satisfy ktlint.

### Spring Boot 4 test annotation package move
- `AutoConfigureMockMvc` is in `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` (not the Spring Boot 3.x package).

### Envers + Auditing specifics
- Envers `@AuditTable` in this stack uses `value = "..."`.
- JPA auditing timestamps in a mapped superclass require `@EntityListeners(AuditingEntityListener::class)` to populate `createdAt` / `updatedAt`.

### Docker Compose vs Testcontainers Port Conflict
- When running `docker compose up -d` with MySQL on port 3306, Testcontainers tests may fail due to port/network conflicts.
- **Workaround**: Stop docker-compose containers before running tests (`docker compose down`), or change docker-compose MySQL to a different port.

### @Transactional Placement Convention
- Apply `@Transactional` at method level (not class level) for explicit transaction boundary visibility.
- This makes transaction boundaries obvious to developers and prevents unexpected transaction behavior.

### Test Naming Convention
- Test function names: English, describes the technical behavior (e.g., `GET system-phase returns default when not exists`)
- `@DisplayName`: Korean, describes the business meaning (e.g., `"시스템 단계 조회 - 데이터가 없으면 기본값(INACTIVE)을 반환한다"`)
- This separation helps both technical readability and business understanding.
