# Sofia 시스템 아키텍처 문서

**버전**: 1.1
**기준 스택**: Kotlin · Spring Boot · Spring MVC · Spring Data JPA · Hibernate Envers · MySQL

***

## 1. 아키텍처 스타일

본 시스템은 **Hexagonal Architecture (Ports & Adapters)** 를 채택합니다. 애플리케이션 코어(비즈니스 로직)를 중심에 두고, 외부 세계(HTTP, DB, 이메일, 스케줄러 등)와의 연결을 모두 **어댑터(Adapter)** 를 통해서만 수행합니다.

핵심 목표는 두 가지입니다.

1. **비즈니스 로직은 프레임워크를 모른다.** `domain`과 `application` 레이어는 Spring, JPA, HTTP에 의존하지 않습니다.
2. **외부 채널이 여럿이어도 코어는 하나다.** 카카오톡 챗봇과 웹 인터페이스처럼 이질적인 입력 채널이 공존할 때, 코어를 수정하지 않고 어댑터만 추가합니다.

***

## 2. 레이어 구성

시스템은 4개 레이어로 구성됩니다.

```
adapter/in: 외부 → 앱 (HTTP, Scheduler)
---
application/port/in: UseCase 인터페이스 (Inbound Port)
application/service: UseCase 구현체
application/port/out: 영속성·외부서비스 인터페이스 (Outbound Port)
---
domain: 순수 도메인 모델 + Enum + Value Object
---
adapter/out: 앱 → 외부 (JPA, Email, ...)
```

### 의존성 방향 규칙

```
adapter/in  →  application/port/in → application/service → application/port/out (←  adapter/out) → domain  
```

- **모든 의존성은 코어(application, domain) 방향으로만 향합니다.**
- `domain`은 아무것도 의존하지 않습니다.
- `adapter`는 `application/port`에만 의존하며, 반대 방향은 불가합니다.
- `common`(유틸리티·설정·예외)은 모든 레이어에서 참조 가능한 예외 공간입니다.

***

## 3. 레이어별 책임

### 3.1 `domain/`

순수 Kotlin 데이터 구조입니다. Spring·JPA 어노테이션을 포함하지 않습니다.

- **Entity**: 시스템의 핵심 개념을 표현하는 클래스 (일반 `class`)
- **Value Object**: 불변이며 동일성이 값으로 결정되는 타입. Kotlin `data class`로 표현합니다.
- **Enum**: 도메인에서 사용하는 열거형 상수

```
domain/
├── entity/
│   ├── User.kt                  # class User(...)
│   └── Work.kt                  # class Work(...)
├── value/
│   ├── StudentId.kt             # data class StudentId(val value: String)
│   ├── CharacterCount.kt        # data class CharacterCount(val value: Int)
│   └── WarningCount.kt          # data class WarningCount(val value: Int)
└── enums/
    ├── WorkType.kt              # GAONNURI / EXTERNAL
    └── PhaseType.kt             # INACTIVE / RECRUIT / TRANSLATE / SETTLE
```

> **규칙**: `domain` 클래스에 `@Entity`, `@Column` 등 JPA 어노테이션을 붙이지 않습니다. JPA 매핑은 `adapter/out/persistence`의 `*JpaEntity`가 전담합니다.

***

### 3.2 Value Object의 레이어 횡단 사용

Hexagonal의 핵심 규칙은 **"도메인이 외부를 모른다"** 이지, **"외부가 도메인을 모른다"** 가 아닙니다. 의존성이 도메인을 향하는 방향은 규칙에 부합하므로, Value Object와 Enum은 어댑터 레이어에서 **적극적으로 직접 사용해야 합니다.** 불필요한 변환 코드를 줄이고, 타입 안전성을 시스템 전체에서 일관되게 유지하기 위해서입니다.

**어댑터에서 직접 사용 가능한 타입**:

| 타입 종류 | 예시 | 이유 |
|----------|------|------|
| Enum | `PhaseType`, `WorkType`, `EmailEventType` | 불변, 상태 없음 |
| Kotlin `value class` | `StudentId`, `CharacterCount` | 불변 래퍼, 유효성 검증 내장 |
| Command / Result 객체 | `IssueWarningCommand`, `StatsResult` | 어댑터-코어 경계를 위해 설계된 DTO |

```kotlin
// ✅ Value Object를 Command에 그대로 포함 — 변환 불필요
@PostMapping("/kakao/skill/apply")
fun apply(@RequestBody req: KakaoApplyRequest): KakaoApplyResponse {
    val command = ApplyCommand(
        studentId = StudentId(req.studentId),  // ex: value class 직접 사용
        name      = StudentName(req.name),
    )
    val result = applyUseCase.apply(command)
    return KakaoApplyResponse.from(result.message)
}
```

**어댑터에서 직접 사용하면 안 되는 타입**:

Entity·Aggregate(`User`, `Work` 등)는 가변 상태와 비즈니스 불변식을 보유합니다. 이를 어댑터에 노출하면 직렬화 시 내부 필드가 유출되거나, 컨트롤러에서 상태를 직접 변경하여 비즈니스 규칙을 우회할 수 있습니다. Entity는 항상 전용 Response DTO로 변환하여 반환합니다.

```kotlin
// ❌ Entity를 응답으로 직접 반환 — 내부 필드 유출, 불변식 우회 위험
fun getBuddy(@PathVariable id: Long): User? = loadUserPort.findByStudentId(id)

// ✅ 어댑터 전용 Response DTO로 변환 — 노출할 필드를 명시적으로 선택
fun getBuddy(@PathVariable id: Long): BuddyResponse =
    loadUserPort.findByStudentId(id)?.let { BuddyResponse.from(it) }
```

> **판단 기준**: 해당 타입을 어댑터에서 수정할 수 있는가? 수정 불가(불변, Enum, `value class`)이면 공유하고, 수정 가능(Entity, Aggregate)이면 DTO로 변환합니다.

***

### 3.3 `application/`

비즈니스 흐름의 조율자입니다.

#### 3.3.1 `application/port/in/` — Inbound Port

UseCase를 **인터페이스**로 선언합니다. 각 인터페이스는 정확히 하나의 유스케이스에 대응합니다.

```kotlin
// UC-006에 대응
interface IssueWarningUseCase {
    fun issue(command: IssueWarningCommand): WarningResult
}
```

- 파일명: `[동사][명사]UseCase.kt`
- 하나의 인터페이스에 메서드 하나를 권장합니다 (ISP 준수).

#### 3.3.2 `application/port/out/` — Outbound Port

서비스가 외부 시스템(DB, 이메일 등)에 **요청하는 인터페이스**입니다. 서비스는 이 인터페이스에만 의존하며, 실제 구현체(JPA, SMTP 등)를 알지 못합니다.

```kotlin
interface LoadUserPort {
    fun findByStudentId(studentId: StudentId): User?  // Value Object를 인자로 사용
}

interface SendEmailPort {
    fun send(event: EmailEvent)
}
```

- 파일명: `[Load|Save|Lock][명사]Port.kt` (단, Load|Save|Lock 이외에도 필요 시 사용 가능)

#### 3.3.3 `application/service/` — Service

`port/in`의 UseCase 인터페이스를 구현하고, `port/out`을 주입받아 비즈니스 흐름을 조율합니다. Spring의 `@Service`와 `@Transactional`을 사용합니다.

```kotlin
@Service
@Transactional
class WarningService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val sendEmailPort: SendEmailPort,
) : IssueWarningUseCase, CancelWarningUseCase {

    override fun issue(command: IssueWarningCommand): WarningResult { ... }
    override fun cancel(command: CancelWarningCommand): WarningResult { ... }
}
```

- 파일명: `[도메인명]Service.kt`
- 연관된 유스케이스 여럿을 한 서비스에 묶을 수 있습니다 (예: `WorkService`가 UC-003과 UC-004를 동시에 구현).

***

### 3.4 `adapter/in/` — Inbound Adapter

외부 요청을 UseCase로 변환하는 진입점입니다. UseCase 인터페이스(`port/in`)를 주입받아 호출합니다.

#### 채널이 여럿인 경우

채널마다 별도 하위 패키지를 둡니다. **같은 UseCase를 두 채널에서 호출하더라도 어댑터 코드는 분리됩니다.**

```
adapter/in/
├── kakao/          ← 카카오톡 스킬 서버 (webhook → JSON 응답)
│   ├── common/     ← 공통 DTO, 서명 검증 필터
│   └── work/
│       └── ReportCompletionSkillController.kt    # POST /kakao/skill/work/report-completion
├── api/            ← 관리자 웹 API (REST API)
│   ├── auth/
│   └── work/
│       └── WorkApiController.kt                  # METHOD /admin/api/works  (JSON)
├── email/          ← 이메일 링크 처리 (수신거부 등)
│   └── UnsubscribeController.kt
└── scheduler/      ← Spring @Scheduled 잡
    └── SubmissionReminderScheduler.kt
```

**컨트롤러 네이밍 규칙**:
- 카카오 스킬: `[동사][명사]SkillController.kt`
- 웹 API(mutation): `[명사]ApiController.kt`

**웹 라우팅 규칙**:
- 카카오 스킬: `METHOD /kakao/skill/{resource}`
- API: `METHOD /admin/api/{resource}`

***

### 3.5 `adapter/out/` — Outbound Adapter

`port/out` 인터페이스의 실제 구현체입니다.

#### 영속성 어댑터 구조

하나의 도메인 개념에 대해 세 파일이 한 쌍을 이룹니다.

```
adapter/out/persistence/user/
├── UserJpaEntity.kt              # @Entity: DB 컬럼 매핑 전담
├── UserJpaRepository.kt          # Spring Data JPA 인터페이스
└── UserPersistenceAdapter.kt     # implements LoadUserPort, SaveUserPort
                                  # JpaEntity ↔ Domain 모델 변환 담당
```

```kotlin
@Component
class UserPersistenceAdapter(
    private val repository: UserJpaRepository,
) : LoadUserPort, SaveUserPort {

    override fun findByStudentId(studentId: StudentId): User? =
        repository.findByStudentId(studentId.value)?.toDomain()

    override fun save(user: User): User =
        repository.save(UserJpaEntity.fromDomain(user)).toDomain()
}
```

**파일명 규칙**:
- `[명사]JpaEntity.kt`
- `[명사]JpaRepository.kt`
- `[명사]PersistenceAdapter.kt`

> **핵심**: `JpaEntity`는 `domain` 모델과 다른 별도 클래스입니다. `PersistenceAdapter`가 두 클래스 사이의 변환(`toDomain()`, `fromDomain()`)을 전담하여 JPA 관심사가 도메인을 오염시키지 않습니다.

***

### 3.6 `common/`

레이어 경계를 횡단하는 공유 코드입니다.

```
common/
├── config/
│   ├── SecurityConfig.kt
│   ├── DataSourceConfig.kt
│   ├── ClockConfig.kt
│   └── SchedulerConfig.kt
└── exception/
    └── SofiaException.kt # 이 서비스의 모든 Exception의 Base Exception 
```

> **범위 제한**: `common`에는 특정 도메인 로직을 넣지 않습니다. 둘 이상의 서비스에서 참조하는 **순수 유틸리티**만 위치합니다.

***

## 4. 전체 패키지 레이아웃 요약

```
{rootPackage}/
├── SofiaApplication.kt
│
├── domain/
│   ├── entity/
│   ├── value/
│   └── enums/
│
├── application/
│   ├── port/
│   │   ├── in/ # usecase
│   │   └── out/
│   └── service/
│
├── adapter/
│   ├── in/
│   │   ├── {channel-a}/
│   │   ├── {channel-b}/
│   │   ├── email/
│   │   └── scheduler/
│   └── out/
│       ├── persistence/
│       │   └── {domain}/
│       └── {external-service}/
│
└── common/
    ├── config/
    └── exception/
```

***

## 6. 부록: 주요 기술 결정 요약

| 항목 | 결정 | 근거 |
|------|------|------|
| 토폴로지 | 단일 모놀리스 | 팀 규모, 초기 단계 |
| DB 스키마 관리 | 초기 `ddl-auto=update` → 공유 환경부터 Flyway | 개발 속도 vs. 안전성 균형 |
| 감사 로그 | Hibernate Envers (`@Audited`) | 별도 로그 테이블 없이 자동 revision 관리 |
| 라운드로빈 동시성 | 단일 행 커서 테이블 + `SELECT ... FOR UPDATE` | 경합 최소화, 트랜잭션 경계 명확 |
| Soft delete | `deleted` boolean + `deleted_at` timestamp, `@SQLDelete` + `@SQLRestriction` | 데이터 보존, 감사 추적 |
| Enum 저장 | `STRING` (숫자 저장 금지) | 마이그레이션 안전성 |
| PK 전략 | `BIGINT` surrogate PK 전체 테이블 통일, 비즈니스 키는 `UNIQUE` 제약 | 조인 성능 + 비즈니스 키 변경 유연성 |
| DB 타임존 | UTC 저장, KST는 프레젠테이션 경계에서만 변환 | 시간대 버그 방지 |
| 테스트 DB | Testcontainers MySQL | 프로덕션 DB 엔진과 동일 환경 보장 |
