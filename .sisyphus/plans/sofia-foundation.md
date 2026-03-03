# Sofia Foundation Setup Plan (Zero-Ground)

## TL;DR
> **Summary**: Implement feature work를 시작하기 전에, `docs/TECHNICAL_ARCHITECTURE.md` 기반의 Hexagonal 뼈대 + 로컬/프로덕션 설정 + MySQL 인프라 + 테스트/아키텍처 가드레일 + SystemPhase 실도메인 샘플(REST)까지 “빌드/테스트로 검증 가능한 기반”을 만든다.
> **Deliverables**:
> - Hexagonal 패키지 뼈대 + 공통 `Clock`/예외 베이스
> - `application.yaml`(base) + `application-local.yaml` + `application-prod.yaml` (default=local)
> - `docker-compose.yml` 로컬 MySQL 8.4 (UTC, utf8mb4, healthcheck)
> - Testcontainers(MySQL) + `@ServiceConnection` 기반 통합테스트 인프라
> - Envers 골격(+샘플 엔티티 `@Audited`)
> - ArchUnit: 레이어 규칙 + `created_at/updated_at` 강제
> - SystemPhase REST 샘플: `GET/PUT /admin/api/system-phase`
> - ktlint+detekt(엄격) `check` 포함
> **Effort**: Medium
> **Parallel**: NO (원자적 단계 + 사용자 검토 게이트로 순차 진행)
> **Critical Path**: Gradle(의존성/품질) → Testcontainers 인프라 → Hex 뼈대/공통설정 → Config/Compose → SystemPhase 샘플(+Envers/JPA Auditing) → ArchUnit 가드레일

## Context
### Original Request
- `docs/`(PRD/Technical docs) 기반으로 zero-ground 구현 예정.
- 현재: Spring Boot 프로젝트 생성 + `docs/PRD.md`, `docs/TECHNICAL_ARCHITECTURE.md`, `docs/TECHNICAL_DECISIONS.md`만 작성.
- 기능 구현 전에 기반(패키지 구조, 인프라, ArchUnitTest 등) 준비 계획 수립 필요.
- 사용자 제약: 명시되지 않은 결정은 임의로 하지 말 것(모든 결정은 사전에 질문/확정).
- 추가 제약: 단계는 원자적(atomic)으로 수행하고, 각 단계 완료 후 git staging → 사용자 검토 요청(승인/리뷰와 함께 보완) 프로토콜을 계획에 포함.

### Interview Summary (decisions locked)
- 아키텍처/패키지: `docs/TECHNICAL_ARCHITECTURE.md`의 레이어 구조가 정답(TECHNICAL_DECISIONS의 slice 언급은 오타).
- 관리자 인터페이스: MVC가 아니라 REST API(문서 오타 수정).
- 포함 범위(사용자 선택): 패키지 뼈대(샘플 흐름 1개 포함), 프로파일 설정(base+local+prod, default=local), Docker Compose MySQL, Testcontainers MySQL(@ServiceConnection), ArchUnit, Envers 골격(@Audited 샘플 포함), ktlint+detekt(엄격, check 포함).
- 제외 범위(명시적): CI workflow 추가, Flyway 골격, Security/auth, 스케줄러, 기타 유스케이스 구현.
- Testcontainers: `@ServiceConnection` 사용 + `org.springframework.boot:spring-boot-testcontainers` 추가.
- 버전 핀:
  - ktlint Gradle plugin `org.jlleitschuh.gradle.ktlint:14.0.1`
  - detekt Gradle plugin `dev.detekt:1.23.8`
  - ArchUnit `com.tngtech.archunit:archunit-junit5:1.4.1`
  - Testcontainers `org.testcontainers:*:1.21.4` (mysql 모듈 정합을 위해 1.21.4로 통일)
  - Envers는 Spring Boot BOM 버전 사용(별도 버전 고정 없음)
- Compose MySQL: image `mysql:8.4`, port `3306`, `TZ=UTC`, utf8mb4/collation 고정, healthcheck 포함.
- JPA/REST 기본: `spring.jpa.open-in-view=false`.
- JPA 네이밍: Boot 기본 naming strategy를 사용하되 설정에 명시해 환경차 제거.
- SystemPhase 샘플:
  - REST under `/admin/api`
  - `GET` + `PUT`
  - phase는 enum 문자열(`INACTIVE/RECRUIT/TRANSLATE/SETTLE`)
  - startDate는 자동(Clock 기반) 생성
  - startDate는 DB TIMESTAMP(UTC), API는 ISO-8601 instant(UTC0)
  - ClockConfig의 `Clock` 빈을 Controller가 주입받아 사용(테스트성 요구)
- created_at/updated_at:
  - 기반 단계에 포함
  - 강제 방식: ArchUnit
  - 자동 채움 방식: Spring Data JPA Auditing + Clock 기반 DateTimeProvider
- 커밋 메시지: Conventional Commits, 메시지는 한국어.

### Metis Review (gaps addressed)
- 버전/클래스명 불일치 리스크(특히 ktlint plugin, Boot naming strategy class): “초기 의존성 검증 단계”를 별도 원자 스텝으로 둬서 실패 시 즉시 중단 + 사용자에게 재승인 요청.
- Hexagonal drift 방지: domain에 JPA/Spring annotation이 들어가면 안 됨 → ArchUnit로 강제.

## Work Objectives
### Core Objective
- 기능 구현 전, “빌드/테스트로 검증 가능한 기반”을 만든다.

### Deliverables
- Hexagonal 패키지 골격 + 공통 베이스(`ClockConfig`, `SofiaException`)
- SystemPhase 실도메인 샘플(REST, JPA, Envers, auditing timestamps)
- 로컬/프로덕션 프로파일 구성 + 로컬 MySQL compose
- Testcontainers 기반 통합테스트 + ArchUnit 가드레일
- ktlint/detekt 품질 게이트(`./gradlew check`에 포함)

### Definition of Done (agent-verifiable)
- `./gradlew check` 성공(ktlint+detekt+테스트+ArchUnit 포함)
- 로컬 실행:
  - `docker compose up -d` 후 MySQL이 healthy
  - `./gradlew bootRun`(default profile=local)로 앱 부팅
  - `curl`로 SystemPhase API가 GET/PUT 정상 동작
- 테스트 실행:
  - `./gradlew test`가 Testcontainers MySQL을 사용해 통과
  - Envers audit table 생성/존재가 테스트로 검증됨
- 아키텍처 제약:
  - `domain/**`에 Spring/JPA/Hibernate 의존이 없음을 ArchUnit가 보장
  - `adapter/out/persistence/**`의 `*JpaEntity`는 `createdAt/updatedAt` 필드를 보유함을 ArchUnit가 보장

### Must Have
- 모든 단계는 원자적(atomic)이며, 각 단계 종료 시점에 컴파일/테스트 가능한 상태
- 각 단계마다 staging → 사용자 검토(승인/리뷰와 함께 보완) → 승인 시 커밋

### Must NOT Have (guardrails)
- Flyway 도입/마이그레이션 파일 생성
- Security/auth(세션/토큰 검증), 관리자 권한 시스템
- 스케줄러/백그라운드 잡
- CI workflow 추가/수정(기존 `.github/workflows/opencode.yml` 유지)
- SystemPhase 외의 도메인/유스케이스 구현(샘플은 SystemPhase만)

## Verification Strategy
> ZERO HUMAN INTERVENTION — 모든 검증은 커맨드/테스트로 수행.
- Test decision: tests-after, JUnit5 + SpringBootTest + Testcontainers(MySQL) 통합 테스트
- QA policy: 각 TODO는 최소 2개 시나리오(성공/실패 또는 경계)
- Evidence: 각 TODO 완료 시 `.sisyphus/evidence/task-{N}-{slug}.txt`에 실행 로그(명령어/핵심 출력) 저장

## Execution Strategy
### Review Gate Protocol (MANDATORY)
각 TODO는 아래 프로토콜을 따른다.
1) TODO 구현 완료
2) 검증 커맨드 실행(해당 TODO의 Acceptance Criteria)
3) 해당 TODO 변경분만 `git add`로 stage
4) 사용자에게 아래 질문을 던진다(옵션 2개 고정):
   - 승인
   - 리뷰와 함께 보완
5) 사용자 응답 처리
   - 승인: stage된 스냅샷 그대로 커밋(계획된 메시지 사용)
   - 리뷰와 함께 보완: 커밋 금지. 사용자가 남긴 unstaged 리뷰(주석/메모 등)를 반영해 수정 → 재검증 → 재-stage → 같은 질문을 다시 던짐

### Parallel Execution Waves
NO — 단계별 사용자 검토 게이트로 인해 순차 진행.

### Dependency Matrix (high level)
- T1(의존성/품질 게이트) → 나머지 전부
- T2(Testcontainers 기반 테스트 베이스) → SystemPhase 통합테스트(T7)
- T3(헥사 패키지/공통 Config) → ArchUnit(T4) + SystemPhase 구현(T7)
- T4(ArchUnit 기본 규칙) → 이후 리팩터링 시 가드
- T5(Config 프로파일) + T6(Compose) → 로컬 부팅 검증(T6/T7)
- T6(SystemPhase 구현+영속성+Envers+Auditing+테스트) → 완료

## TODOs
> Implementation + Test = ONE task.
> 각 TODO는 Review Gate Protocol을 반드시 수행.

- [x] 1. Gradle 품질 게이트(ktlint/detekt) 추가 + check 연결

  **What to do**:
  - `build.gradle.kts`에 ktlint/detekt 플러그인 버전 고정 추가
    - ktlint: `org.jlleitschuh.gradle.ktlint` = `14.0.1`
    - detekt: `dev.detekt` = `1.23.8`
  - ktlint/detekt를 `check`에 포함(엄격)되도록 Gradle task dependency를 구성
  - 설정 파일은 추가하지 않음(기본 설정 사용)
  - 버전/플러그인 해석 실패 시(리졸브 불가) 즉시 중단하고 사용자에게 대체 버전 후보를 제시해 재승인 받는다

  **Must NOT do**:
  - CI workflow 추가
  - 규칙 파일(detekt.yml/.editorconfig 등) 생성

  **Recommended Agent Profile**:
  - Category: `quick` — Reason: 단일 Gradle 파일 중심 변경
  - Skills: []
  - Omitted: [`git-master`] — 이유: 커밋은 Review Gate에 따라 수행

  **Parallelization**: Can Parallel: NO | Wave 1 | Blocks: [2-7] | Blocked By: []

  **References**:
  - Build: `build.gradle.kts`

  **Acceptance Criteria**:
  - [ ] `./gradlew check` 성공(최소: 기존 테스트 포함)
  - [ ] `./gradlew tasks`에서 ktlint/detekt 관련 태스크가 확인됨

  **QA Scenarios**:
  ```
  Scenario: check가 ktlint+detekt를 포함
    Tool: Bash
    Steps: ./gradlew check
    Expected: SUCCESS, ktlint/detekt가 실행 로그에 나타남
    Evidence: .sisyphus/evidence/task-1-quality-gate.txt

  Scenario: 플러그인 리졸브 실패 시 중단
    Tool: Bash
    Steps: (리졸브 실패가 발생하면) gradle 에러 로그 캡처 후 작업 중단
    Expected: 사용자 재승인 없이는 버전/플러그인 임의 변경 금지
    Evidence: .sisyphus/evidence/task-1-quality-gate-error.txt
  ```

  **Commit**: YES | Message: `chore(기반): ktlint/detekt 품질 게이트 추가` | Files: [`build.gradle.kts`]

- [x] 2. 테스트 기반: Testcontainers(MySQL) + @ServiceConnection 스캐폴딩

  **What to do**:
  - `build.gradle.kts`에 테스트 의존성 추가(버전 고정: 1.21.4)
    - `org.springframework.boot:spring-boot-testcontainers`
    - `org.testcontainers:junit-jupiter:1.21.4`
    - `org.testcontainers:mysql:1.21.4`
  - `src/test/kotlin/...`에 공통 통합테스트 베이스(예: `IntegrationTestSupport`)를 추가
    - MySQLContainer(image `mysql:8.4`)를 `@ServiceConnection`으로 제공
    - 테스트에서 명시적으로 `spring.jpa.hibernate.ddl-auto=create-drop`(또는 `create`)를 지정해 스키마 자동 생성
  - 기존 `SofiaApplicationTests.kt`가 해당 베이스를 사용하도록 변경하여 “Testcontainers 없이도 통과” 같은 우발을 방지

  **Must NOT do**:
  - `application-test.yaml` 같은 추가 프로파일 파일 생성(범위 밖)

  **Recommended Agent Profile**:
  - Category: `unspecified-low` — Reason: 테스트 인프라 + Gradle 의존성
  - Skills: []

  **Parallelization**: Can Parallel: NO | Wave 1 | Blocks: [3-7] | Blocked By: [1]

  **References**:
  - Existing test: `src/test/kotlin/ywcheong/sofia/SofiaApplicationTests.kt`

  **Acceptance Criteria**:
  - [ ] `./gradlew test` 성공
  - [ ] 테스트 실행 로그에 MySQL Testcontainers 컨테이너 기동이 확인됨

  **QA Scenarios**:
  ```
  Scenario: contextLoads가 Testcontainers 기반으로 통과
    Tool: Bash
    Steps: ./gradlew test
    Expected: SUCCESS, mysql:8.4 컨테이너가 시작됨
    Evidence: .sisyphus/evidence/task-2-testcontainers.txt

  Scenario: 컨테이너 이미지 pull 실패
    Tool: Bash
    Steps: 네트워크 차단/이미지 pull 실패 상황에서 ./gradlew test
    Expected: 테스트 실패가 명확한 에러로 종료(모호한 datasource 에러가 아님)
    Evidence: .sisyphus/evidence/task-2-testcontainers-error.txt
  ```

  **Commit**: YES | Message: `test(기반): Testcontainers MySQL 통합테스트 베이스 추가` | Files: [`build.gradle.kts`, `src/test/kotlin/**`]

- [x] 3. Hexagonal 패키지 뼈대 + 공통 Clock/Exception 베이스 추가

  **What to do**:
  - `docs/TECHNICAL_ARCHITECTURE.md`의 레이아웃을 그대로 패키지로 생성
    - `domain/`, `application/port/in`, `application/port/out`, `application/service`, `adapter/in`, `adapter/out/persistence`, `common/config`, `common/exception`
  - 공통 베이스 구현
    - `common/config/ClockConfig.kt`: `Clock` Bean 제공(UTC)
    - `common/exception/SofiaException.kt`: 서비스 공통 예외 베이스
  - (아직 기능 구현 전이므로) 비즈니스 로직은 추가하지 않되, 컴파일 가능한 최소 코드로 둔다

  **Must NOT do**:
  - domain에 Spring/JPA/Hibernate 어노테이션 추가

  **Recommended Agent Profile**:
  - Category: `unspecified-low`
  - Skills: []

  **Parallelization**: Can Parallel: NO | Wave 2 | Blocks: [4-7] | Blocked By: [2]

  **References**:
  - Architecture: `docs/TECHNICAL_ARCHITECTURE.md`
  - Root package: `src/main/kotlin/ywcheong/sofia/SofiaApplication.kt`

  **Acceptance Criteria**:
  - [ ] `./gradlew test` 성공
  - [ ] `Clock` Bean이 Spring context에 등록됨(간단한 스모크 테스트로 확인)

  **QA Scenarios**:
  ```
  Scenario: 애플리케이션 컨텍스트가 ClockConfig를 로드
    Tool: Bash
    Steps: ./gradlew test
    Expected: SUCCESS
    Evidence: .sisyphus/evidence/task-3-hex-skeleton.txt

  Scenario: domain 오염 방지
    Tool: Bash
    Steps: (실수로 domain에 spring/jpa import를 넣으면) 이후 ArchUnit에서 잡히도록 준비
    Expected: 다음 단계(ArchUnit)에서 위반이 실패로 표출됨
    Evidence: .sisyphus/evidence/task-3-hex-skeleton-guard.txt
  ```

  **Commit**: YES | Message: `chore(기반): 헥사고날 패키지 뼈대와 ClockConfig 추가` | Files: [`src/main/kotlin/**`]

- [x] 4. ArchUnit 기반 아키텍처 가드레일 추가(레이어 규칙 + timestamps 규칙)

  **What to do**:
  - `build.gradle.kts`에 ArchUnit 의존성 추가: `com.tngtech.archunit:archunit-junit5:1.4.1`
  - ArchUnit 테스트 추가(예: `src/test/kotlin/.../archunit/ArchitectureRulesTest.kt`)
    - domain 패키지(`..domain..`)는 `org.springframework..`, `jakarta.persistence..`, `org.hibernate..`에 의존하면 실패
    - `adapter..`는 `application..`/`domain..`에만 의존 가능(역방향 금지)
    - `..adapter.out.persistence..`의 `*JpaEntity`는 `createdAt`/`updatedAt` 필드를 반드시 보유(필드명 고정)
  - Kotlin 특성(컴패니언/합성 클래스)으로 인한 false-positive를 피하기 위해, 규칙 대상 패키지를 `ywcheong.sofia..`로 제한

  **Must NOT do**:
  - 기능 구현(SystemPhase) 시작

  **Recommended Agent Profile**:
  - Category: `unspecified-high` — Reason: ArchUnit 규칙은 한번에 잘 잡아야 이후 개발이 매끄러움
  - Skills: []

  **Parallelization**: Can Parallel: NO | Wave 2 | Blocks: [5-7] | Blocked By: [3]

  **References**:
  - Architecture rules: `docs/TECHNICAL_ARCHITECTURE.md`

  **Acceptance Criteria**:
  - [ ] `./gradlew test` 성공
  - [ ] ArchUnit 테스트가 실행되며(로그/결과), 규칙이 최소 3개 이상 존재

  **QA Scenarios**:
  ```
  Scenario: 정상 상태에서 ArchUnit 통과
    Tool: Bash
    Steps: ./gradlew test
    Expected: SUCCESS
    Evidence: .sisyphus/evidence/task-4-archunit.txt

  Scenario: domain에 JPA import가 생기면 실패
    Tool: Bash
    Steps: (고의 위반은 만들지 말고) 규칙이 해당 위반을 잡도록 패키지/조건이 정확히 설정됨을 코드로 확인
    Expected: 규칙이 'domain depends on jakarta.persistence'를 금지하도록 구현돼 있음
    Evidence: .sisyphus/evidence/task-4-archunit-guard.txt
  ```

  **Commit**: YES | Message: `test(기반): ArchUnit로 헥사고날 규칙과 timestamps 규칙 강제` | Files: [`build.gradle.kts`, `src/test/kotlin/**`]

- [x] 5. application 설정: base/local/prod 프로파일 + UTC + naming + OIV off

  **What to do**:
  - 아래 파일을 생성/수정
    - `src/main/resources/application.yaml` (base)
    - `src/main/resources/application-local.yaml`
    - `src/main/resources/application-prod.yaml`
  - base 정책
    - `spring.profiles.default: local`
    - `spring.jpa.open-in-view: false`
    - UTC 고정: `spring.jpa.properties.hibernate.jdbc.time_zone: UTC`
    - 네이밍 전략 명시(Boot 기본과 동일한 클래스)
      - physical: `org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy`
      - implicit: `org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy`
    - (주의) Boot 4에서 클래스 경로가 변경되어 컴파일 실패 시, classpath에서 동일 역할 클래스를 찾아 치환(치환은 “동일 기능”만 허용)
  - local
    - datasource를 docker-compose MySQL로 연결(UTC0): `jdbc:mysql://localhost:3306/sofia?...serverTimezone=UTC...`
    - `spring.jpa.hibernate.ddl-auto: update`
  - prod
    - datasource는 설정 파일에 두지 않음(표준 Spring 환경변수/시스템프로퍼티로 주입: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`)
    - `spring.jpa.hibernate.ddl-auto: validate` (Flyway 미도입 상태에서 prod 실행은 불가하다는 가드 문구/주석을 포함)

  **Must NOT do**:
  - Flyway 의존성/폴더 추가

  **Recommended Agent Profile**:
  - Category: `unspecified-low`
  - Skills: []

  **Parallelization**: Can Parallel: NO | Wave 3 | Blocks: [6-7] | Blocked By: [4]

  **References**:
  - Existing config: `src/main/resources/application.yaml`
  - UTC decision: `docs/TECHNICAL_DECISIONS.md`

  **Acceptance Criteria**:
  - [ ] `./gradlew test` 성공
  - [ ] `./gradlew bootRun`(옵션 없이, default=local)에서 datasource 관련 설정 누락 에러가 발생하지 않음(단, DB가 없으면 연결 실패는 가능)

  **QA Scenarios**:
  ```
  Scenario: 설정 파일 로딩/컴파일
    Tool: Bash
    Steps: ./gradlew test
    Expected: SUCCESS
    Evidence: .sisyphus/evidence/task-5-profiles.txt

  Scenario: naming strategy 클래스 경로 불일치
    Tool: Bash
    Steps: (만약 컴파일 실패 시) classpath에서 후보 클래스를 검색해 동일 역할로 교체
    Expected: 최종적으로 build 통과, 기능은 'snake_case' 유지
    Evidence: .sisyphus/evidence/task-5-profiles-error.txt
  ```

  **Commit**: YES | Message: `chore(기반): base/local/prod 설정과 UTC/OIV/naming 고정` | Files: [`src/main/resources/application*.yaml`]

- [x] 6. 로컬 MySQL 8.4 인프라: docker-compose.yml 추가(UTC/utf8mb4/healthcheck)

  **What to do**:
  - 루트에 `docker-compose.yml` 생성
    - service: mysql
    - image: `mysql:8.4`
    - ports: `3306:3306`
    - env: `MYSQL_DATABASE=sofia`, `MYSQL_USER=sofia`, `MYSQL_PASSWORD=sofia`, `MYSQL_ROOT_PASSWORD=sofia`, `TZ=UTC`
    - command로 charset/collation/UTC0 고정
    - healthcheck 추가
    - volume(데이터 지속) 포함

  **Must NOT do**:
  - `.env` 파일 커밋(비밀값이 들어갈 수 있음)

  **Recommended Agent Profile**:
  - Category: `quick`
  - Skills: []

  **Parallelization**: Can Parallel: NO | Wave 3 | Blocks: [7] | Blocked By: [5]

  **Acceptance Criteria**:
  - [ ] `docker compose up -d` 성공
  - [ ] `docker compose ps`에서 mysql이 `healthy`
  - [ ] `docker compose down -v`로 정상 정리

  **QA Scenarios**:
  ```
  Scenario: 로컬 MySQL 기동/헬스체크
    Tool: Bash
    Steps: docker compose up -d; docker compose ps
    Expected: mysql 서비스가 healthy
    Evidence: .sisyphus/evidence/task-6-compose.txt

  Scenario: 포트 충돌
    Tool: Bash
    Steps: 이미 3306 사용 중인 환경에서 docker compose up -d
    Expected: 명확한 포트 충돌 에러. (해결은 사용자 지시 없이는 임의 포트 변경 금지)
    Evidence: .sisyphus/evidence/task-6-compose-error.txt
  ```

  **Commit**: YES | Message: `chore(기반): 로컬 MySQL docker-compose 추가(UTC/utf8mb4/healthcheck)` | Files: [`docker-compose.yml`]

- [x] 7. SystemPhase 실도메인 샘플(E2E): REST + 영속성 + Auditing + Envers + 통합테스트

  **What to do**:
  - Domain
    - `domain/enums/PhaseType.kt`: `INACTIVE/RECRUIT/TRANSLATE/SETTLE`
    - `domain/entity/SystemPhase.kt`: 현재 phase + startDate(Instant) 표현(도메인에는 JPA 없음)
  - Application
    - Inbound usecase: `application/port/in/GetSystemPhaseUseCase.kt`, `application/port/in/ChangeSystemPhaseUseCase.kt`
    - Command/Result DTO(코어 경계 DTO): `ChangeSystemPhaseCommand(phaseType, startDate)` 등
    - Outbound port: `LoadSystemPhasePort`, `SaveSystemPhasePort`
    - Service: `application/service/SystemPhaseService.kt`
  - Adapter(in)
    - `adapter/in/api/systemphase/SystemPhaseApiController.kt`
      - `GET /admin/api/system-phase`
      - `PUT /admin/api/system-phase`
      - Clock bean 주입, `val now = Instant.now(clock)` 생성 후 command에 포함
    - Request/Response DTO는 어댑터 전용 클래스로 분리

  - Persistence (adapter/out)
    - Envers 의존성 추가: `org.hibernate.orm:hibernate-envers` (버전은 BOM)
    - JPA Auditing 구성
      - `common/config/JpaAuditingConfig.kt`
        - `@EnableJpaAuditing(dateTimeProviderRef = "sofiaDateTimeProvider")`
        - `DateTimeProvider`가 `Clock` bean을 사용해 UTC Instant 제공
      - `adapter/out/persistence/common/BaseTimeJpaEntity.kt` (`@MappedSuperclass`)
        - `@CreatedDate` `createdAt: Instant`
        - `@LastModifiedDate` `updatedAt: Instant`
    - SystemPhase JPA
      - `adapter/out/persistence/systemphase/SystemPhaseJpaEntity.kt`
        - `@Entity` + `@Table(name="system_phase")`
        - Envers: `@Audited` + `@AuditTable(name="system_phase_aud")`
        - 필드: `phaseType`(STRING), `startDate`(Instant UTC), timestamps(상속)
      - `SystemPhaseJpaRepository.kt`
      - `SystemPhasePersistenceAdapter.kt` implements `LoadSystemPhasePort`/`SaveSystemPhasePort`
    - 단일 row 정책
      - row가 없으면 생성, 있으면 업데이트
      - phase 변경 시 startDate는 항상 `Instant.now(clock)`로 재설정(Clock은 controller가 주입/생성한 값을 전달)

  - Integration tests (Testcontainers)
    - `@ServiceConnection` MySQL 8.4 컨테이너 기반으로 다음을 검증
      - Clock 고정(예: `Clock.fixed(...)`)을 테스트 컨텍스트에서 override하여, PUT 이후 응답 startDate가 고정값과 동일
      - createdAt/updatedAt이 null이 아님
      - Envers audit table `system_phase_aud`가 존재(정보 스키마 쿼리)

  **Must NOT do**:
  - 인증/권한 적용(범위 밖)
  - UC-012의 사전조건 검증(Work/User가 필요해져 범위 초과)
  - Flyway 도입
  - 다른 엔티티(User/Work 등) 생성

  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  - Skills: []

  **Parallelization**: Can Parallel: NO | Wave 4 | Blocks: [] | Blocked By: [6]

  **References**:
  - Ports & Adapters layout: `docs/TECHNICAL_ARCHITECTURE.md`
  - PRD SystemPhase: `docs/PRD.md`

  **Acceptance Criteria**:
  - [ ] `./gradlew check` 성공
  - [ ] `./gradlew test` 로그에서 Testcontainers MySQL 컨테이너 기동 확인
  - [ ] SystemPhase 통합테스트가 Clock 고정, timestamps, Envers audit table 존재를 검증

  **QA Scenarios**:
  ```
  Scenario: SystemPhase E2E 통과(Testcontainers)
    Tool: Bash
    Steps: ./gradlew test
    Expected: SUCCESS, mysql:8.4 컨테이너가 시작됨
    Evidence: .sisyphus/evidence/task-7-systemphase-e2e.txt

  Scenario: Clock 고정이 startDate에 반영
    Tool: Bash
    Steps: ./gradlew test (Clock.fixed를 주입하는 테스트 포함)
    Expected: PUT 응답의 startDate가 고정된 Instant와 정확히 일치
    Evidence: .sisyphus/evidence/task-7-systemphase-clock.txt
  ```

  **Commit**: YES | Message: `feat(기반): SystemPhase E2E(REST+JPA+Auditing+Envers) 및 통합테스트` | Files: [`build.gradle.kts`, `src/main/kotlin/**`, `src/test/kotlin/**`]

## Final Verification Wave (4 parallel agents, ALL must APPROVE)
- [ ] F1. Plan Compliance Audit — oracle
- [ ] F2. Code Quality Review — unspecified-high
- [ ] F3. Real Manual QA (API smoke) — unspecified-high
- [ ] F4. Scope Fidelity Check — deep

## Commit Strategy
- 커밋은 TODO 단위(원자적)로만 발생
- 모든 TODO는 staging 후 사용자 검토(승인/리뷰와 함께 보완)를 거친 뒤에만 커밋
- 커밋 메시지: Conventional Commits + 한국어(예: `chore(기반): ...`, `feat(기반): ...`, `test(기반): ...`)

## Success Criteria
- 위 Definition of Done 충족
- 기반 단계의 모든 커밋이 원자적이며, 각 커밋 단위로 `./gradlew check`가 통과
