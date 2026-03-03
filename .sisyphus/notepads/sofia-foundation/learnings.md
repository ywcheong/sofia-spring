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
