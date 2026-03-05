# Sofia Develop Skill

Sofia 프로젝트의 유스케이스 개발 워크플로우를 정의합니다.

---

## 개요

이 Skill은 Sofia 프로젝트에서 새로운 유스케이스를 개발할 때 따라야 할 표준 워크플로우를 정의합니다. Hexagonal Architecture (Ports & Adapters)를 기반으로 하며, 작은 팀 규모에 맞게 단순화된 프로세스를 제공합니다.

---

## 개발 워크플로우

### 1단계: 유스케이스 선택

**목표**: 개발할 유스케이스를 PRD에서 선택하고 범위를 명확히 합니다.

**참고 문서**:
- `docs/PRD.md` - 전체 유스케이스 명세서 (UC-001 ~ UC-021)

**체크리스트**:
- [ ] PRD에서 해당 유스케이스 ID 확인 (예: UC-001, UC-003, UC-006 등)
- [ ] 유스케이스의 행위자(Actor) 확인 (번역버디 / 관리자 / 시스템)
- [ ] 유스케이스의 인터페이스 확인 (카카오톡 챗봇 / 웹 인터페이스 / 스케줄러)
- [ ] 전제 조건(Preconditions) 확인
- [ ] 종료 조건(Postconditions) 확인
- [ ] 관련 비즈니스 규칙(BR-XXX) 확인

**인터페이스별 라우팅 규칙**:
| 인터페이스 | 엔드포인트 패턴 | 컨트롤러 위치 |
|-----------|---------------|---------------|
| 카카오톡 챗봇 | `POST /kakao/skill/{resource}` | `adapter/in/kakao/` |
| 웹 API | `METHOD /admin/api/{resource}` | `adapter/in/api/` |
| 스케줄러 | `@Scheduled` | `adapter/in/scheduler/` |

---

### 2단계: 요구사항 분석

**목표**: 유스케이스의 상세 흐름과 비즈니스 규칙을 분석합니다.

**참고 문서**:
- `docs/PRD.md` - 유스케이스 명세서, 비즈니스 규칙, 데이터 모델
- `docs/TECHNICAL_ARCHITECTURE.md` - 아키텍처 가이드라인
- `docs/TECHNICAL_DECISIONS.md` - 기술 결정 사항

**체크리스트**:
- [ ] 기본 흐름(Main Flow) 단계별 분석
- [ ] 예외 흐름(Alternative Flow) 분석
- [ ] 관련 데이터 모델 확인
- [ ] 기간별 가용성 확인 (Phase 0~3)
- [ ] 이메일 발송 필요 여부 확인
- [ ] 동시성 이슈 여부 확인 (예: 라운드로빈 배정)

**분석 산출물**:
```
유스케이스: UC-XXX [유스케이스명]
행위자: [번역버디/관리자/시스템]
인터페이스: [카카오톡/웹/스케줄러]

입력:
- [입력 필드 목록]

출력:
- [출력 필드 목록]

비즈니스 규칙:
- BR-XXX: [규칙 내용]

의존성:
- [연관된 다른 유스케이스 또는 엔티티]
```

---

### 3단계: 설계 (Hexagonal Architecture)

**목표**: Hexagonal Architecture에 맞춰 컴포넌트를 설계합니다.

**참고 문서**:
- `docs/TECHNICAL_ARCHITECTURE.md` - 레이어 구성, 의존성 규칙

**체크리스트**:
- [ ] Domain 모델 설계 (Entity, Value Object, Enum)
- [ ] Inbound Port 설계 (UseCase 인터페이스)
- [ ] Outbound Port 설계 (영속성/외부 서비스 인터페이스)
- [ ] Service 설계 (UseCase 구현체)
- [ ] Inbound Adapter 설계 (Controller)
- [ ] Outbound Adapter 설계 (Persistence Adapter)

**설계 순서** (Inside-Out):
```
1. domain/          <- 핵심 비즈니스 로직, 프레임워크 독립
2. application/     <- 유스케이스 조율
   ├── port/in/     <- UseCase 인터페이스
   ├── port/out/    <- 영속성/외부 서비스 인터페이스
   └── service/     <- UseCase 구현체
3. adapter/         <- 외부 세계 연결
   ├── in/          <- HTTP, 스케줄러 등
   └── out/         <- JPA, 이메일 등
```

**명명 규칙**:
| 레이어 | 파일명 패턴 | 예시 |
|--------|------------|------|
| Domain Entity | `[명사].kt` | `User.kt`, `Work.kt` |
| Domain Value Object | `[명사].kt` | `StudentId.kt` |
| Domain Enum | `[명사].kt` | `WorkType.kt` |
| Inbound Port | `[동사][명사]UseCase.kt` | `IssueWarningUseCase.kt` |
| Outbound Port | `[Load/Save][명사]Port.kt` | `LoadUserPort.kt` |
| Service | `[도메인명]Service.kt` | `WarningService.kt` |
| Controller (Kakao) | `[동사][명사]SkillController.kt` | `ReportCompletionSkillController.kt` |
| Controller (API) | `[명사]ApiController.kt` | `WorkApiController.kt` |
| JPA Entity | `[명사]JpaEntity.kt` | `UserJpaEntity.kt` |
| JPA Repository | `[명사]JpaRepository.kt` | `UserJpaRepository.kt` |
| Persistence Adapter | `[명사]PersistenceAdapter.kt` | `UserPersistenceAdapter.kt` |

---

### 4단계: 구현

**목표**: 설계에 따라 코드를 구현합니다.

**구현 순서** (도메인 → 애플리케이션 → 어댑터):

#### 4.1 Domain 레이어 구현

**위치**: `src/main/kotlin/ywcheong/sofia/domain/`

```
domain/
├── entity/         # 핵심 엔티티 (Spring/JPA 어노테이션 금지)
├── value/          # 값 객체 (Kotlin data class 또는 value class)
└── enums/          # 열거형
```

**규칙**:
- `@Entity`, `@Column` 등 JPA 어노테이션 사용 금지
- 순수 Kotlin 데이터 구조로만 구성
- Value Object는 `data class` 또는 `value class` 사용

```kotlin
// 예: domain/value/StudentId.kt
@JvmInline
value class StudentId(val value: String) {
    init {
        require(value.matches(Regex("\\d{2}-\\d{3}"))) {
            "학번 형식이 올바르지 않습니다: $value"
        }
    }
}
```

#### 4.2 Application 레이어 구현

**위치**: `src/main/kotlin/ywcheong/sofia/application/`

**Inbound Port (UseCase)**:
```kotlin
// application/port/in/ApplyForParticipationUseCase.kt
interface ApplyForParticipationUseCase {
    fun apply(command: ApplyCommand): ApplicationResult
}
```

**Command/Result** (`application/port/inbound/command/`, `application/port/inbound/result/`):
```kotlin
// Command: UseCase 입력
data class ApplyCommand(
    val studentId: StudentId,
    val name: StudentName,
)

// Result: UseCase 출력
data class ApplicationResult(
    val message: String,
    val applicationId: Long,
)
```

**Outbound Port**:
```kotlin
// application/port/out/LoadUserPort.kt
interface LoadUserPort {
    fun findByStudentId(studentId: StudentId): User?
    fun existsByStudentId(studentId: StudentId): Boolean
}

// application/port/out/SaveUserPort.kt
interface SaveUserPort {
    fun save(user: User): User
}
```

**Service**:
```kotlin
// application/service/ApplicationService.kt
@Service
@Transactional
class ApplicationService(
    private val loadUserPort: LoadUserPort,
    private val saveApplicationPort: SaveApplicationPort,
) : ApplyForParticipationUseCase {

    override fun apply(command: ApplyCommand): ApplicationResult {
        // 비즈니스 로직 구현
    }
}
```

#### 4.3 Adapter 레이어 구현

**Inbound Adapter** (`adapter/in/`):

```kotlin
// adapter/in/kakao/KakaoSkillController.kt
@RestController
@RequestMapping("/kakao/skill")
class KakaoSkillController(
    private val applyForParticipationUseCase: ApplyForParticipationUseCase,
) {

    @PostMapping("/apply")
    fun apply(@RequestBody request: KakaoApplyRequest): KakaoApplyResponse {
        val command = ApplyCommand(
            studentId = StudentId(request.studentId),
            name = StudentName(request.name),
        )
        val result = applyForParticipationUseCase.apply(command)
        return KakaoApplyResponse.from(result)
    }
}
```

**Outbound Adapter** (`adapter/out/`):

```kotlin
// adapter/out/persistence/user/UserJpaEntity.kt
@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "student_id", unique = true, nullable = false)
    val studentId: String,

    // ... 기타 필드
) {
    fun toDomain(): User = User(...)

    companion object {
        fun fromDomain(user: User): UserJpaEntity = ...
    }
}

// adapter/out/persistence/user/UserPersistenceAdapter.kt
@Component
class UserPersistenceAdapter(
    private val userRepository: UserJpaRepository,
) : LoadUserPort, SaveUserPort {

    override fun findByStudentId(studentId: StudentId): User? =
        userRepository.findByStudentId(studentId.value)?.toDomain()

    override fun save(user: User): User =
        userRepository.save(UserJpaEntity.fromDomain(user)).toDomain()
}
```

**구현 체크리스트**:
- [ ] Domain Entity / Value Object 구현
- [ ] Command / Result DTO 구현
- [ ] UseCase 인터페이스 정의
- [ ] Outbound Port 인터페이스 정의
- [ ] Service 구현 (`@Service`, `@Transactional`)
- [ ] JpaEntity 구현 (`@Entity`, `@Audited`)
- [ ] JpaRepository 구현
- [ ] PersistenceAdapter 구현
- [ ] Controller 구현 (요청 검증 포함)

---

### 5단계: 테스트

**목표**: Testcontainers MySQL을 활용한 통합 테스트를 작성합니다.

**참고 문서**:
- `docs/TECHNICAL_DECISIONS.md` - 테스트 전략 (후행 테스트)

**테스트 구조**:
```kotlin
@SpringBootTest
@Testcontainers
class ApplicationServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        val mysql = MySQLContainer<Nothing>("mysql:8.0")
    }

    @Autowired
    private lateinit var applyForParticipationUseCase: ApplyForParticipationUseCase

    @Test
    fun `참가 신청이 성공해야 한다`() {
        // given
        val command = ApplyCommand(
            studentId = StudentId("24-001"),
            name = StudentName("홍길동"),
        )

        // when
        val result = applyForParticipationUseCase.apply(command)

        // then
        assertThat(result.message).contains("성공")
    }
}
```

**테스트 체크리스트**:
- [ ] 정상 흐름 테스트 (Happy Path)
- [ ] 예외 흐름 테스트 (Validation Error, Business Rule Violation)
- [ ] 경계값 테스트 (Edge Cases)
- [ ] 동시성 테스트 (필요한 경우)

---

### 6단계: PR 생성

**목표**: 변경 사항을 PR로 제출합니다.

**PR 제목 형식**:
```
[유스케이스ID] 유스케이스명

예: [UC-001] 참가 신청 기능 구현
```

**PR 설명 템플릿**:
```markdown
## 요약
- 유스케이스: UC-XXX [유스케이스명]
- 행위자: [번역버디/관리자/시스템]
- 인터페이스: [카카오톡/웹/스케줄러]

## 변경 사항
- [ ] Domain: [추가/수정된 Entity, Value Object]
- [ ] Application: [추가/수정된 UseCase, Service]
- [ ] Adapter In: [추가/수정된 Controller]
- [ ] Adapter Out: [추가/수정된 Persistence Adapter]

## 테스트
- [ ] 통합 테스트 작성 완료
- [ ] 로컬 테스트 통과

## 관련 문서
- PRD: docs/PRD.md#UC-XXX
- 아키텍처: docs/TECHNICAL_ARCHITECTURE.md
```

---

## Hexagonal Architecture 준수 가이드라인

### 의존성 방향 규칙

```
adapter/in  →  application/port/in → application/service → application/port/out ← adapter/out → domain
```

- 모든 의존성은 **코어(direction, application) 방향으로만** 향합니다.
- `domain`은 아무것도 의존하지 않습니다.
- `adapter`는 `application/port`에만 의존합니다.

### Value Object 사용 규칙

**어댑터에서 직접 사용 가능**:
- Enum (`PhaseType`, `WorkType` 등)
- Value class (`StudentId`, `CharacterCount` 등)
- Command / Result 객체

**어댑터에서 직접 사용 금지**:
- Entity / Aggregate (`User`, `Work` 등)
- 반드시 전용 Response DTO로 변환하여 반환

```kotlin
// X 금지: Entity 직접 반환
fun getBuddy(@PathVariable id: Long): User? = loadUserPort.findByStudentId(id)

// O 권장: DTO로 변환
fun getBuddy(@PathVariable id: Long): BuddyResponse =
    loadUserPort.findByStudentId(id)?.let { BuddyResponse.from(it) }
```

---

## 코드 작성 규칙

### 패키지 구조

```
src/main/kotlin/ywcheong/sofia/
├── SofiaApplication.kt
├── domain/
│   ├── entity/
│   ├── value/
│   └── enums/
├── application/
│   ├── port/
│   │   ├── inbound/
│   │   │   ├── command/
│   │   │   ├── result/
│   │   │   └── [동사][명사]UseCase.kt
│   │   └── outbound/
│   │       └── [Load/Save][명사]Port.kt
│   └── service/
│       └── [도메인명]Service.kt
├── adapter/
│   ├── in/
│   │   ├── kakao/
│   │   ├── api/
│   │   ├── email/
│   │   └── scheduler/
│   └── out/
│       ├── persistence/
│       │   └── {domain}/
│       │       ├── [명사]JpaEntity.kt
│       │       ├── [명사]JpaRepository.kt
│       │       └── [명사]PersistenceAdapter.kt
│       └── {external-service}/
└── common/
    ├── config/
    └── exception/
```

### DB 명명 규칙

- 테이블명: `snake_case` (예: `users`, `dictionary_entries`)
- 컬럼명: `snake_case` (예: `student_id`, `created_at`)
- Enum 저장: `STRING` (숫자 저장 금지)
- PK: `BIGINT` surrogate PK
- 타임스탬프: UTC로 저장

### Soft Delete 규칙

적용 대상: User, Work, DictionaryEntry, Adjustment, AuthToken

```kotlin
@SQLDelete(sql = "UPDATE users SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class UserJpaEntity(...) : BaseSoftDeleteJpaEntity() {
    // ...
}
```

### 감사 로그 (Hibernate Envers)

```kotlin
@Entity
@Audited
class UserJpaEntity(...) {
    // ...
}
```

---

## 테스트 작성 가이드라인

### 기본 원칙

- **후행 테스트(tests-after)**: 구현 완료 후 테스트 작성
- **통합 테스트 중심**: JUnit5 + SpringBootTest + Testcontainers MySQL
- **프로덕션 DB와 동일 환경**: MySQL 8.x 사용

### Testcontainers 설정

```kotlin
@SpringBootTest
@Testcontainers
class BaseServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        val mysql = MySQLContainer<Nothing>("mysql:8.0")
    }
}
```

### 테스트 명명 규칙

```kotlin
// 형식: `[상황] 때 [결과] 여야 한다`
@Test
fun `학번이 중복되면 예외가 발생해야 한다`() { ... }

@Test
fun `48시간 초과 제출 시 자동 경고가 발행되어야 한다`() { ... }
```

---

## 유스케이스별 엔드포인트 매핑

| 유스케이스 ID | 유스케이스명 | 인터페이스 | 엔드포인트 |
|--------------|-------------|-----------|-----------|
| UC-001 | 참가 신청 | 카카오톡 | `POST /kakao/skill/apply` |
| UC-002 | 신청 승인/거절 | 웹 | `POST /admin/api/applications/{id}/approve` |
| UC-003 | 번역 과제 생성 | 웹 | `POST /admin/api/works` |
| UC-004 | 과제 완료 보고 | 카카오톡 | `POST /kakao/skill/work/report-completion` |
| UC-005 | 내 통계 확인 | 카카오톡 | `POST /kakao/skill/me/stats` |
| UC-006 | 경고 발행/취소 | 웹 | `POST /admin/api/users/{id}/warnings` |
| UC-007 | 봉사시간 수동 조정 | 웹 | `POST /admin/api/users/{id}/adjustments` |
| UC-008 | 번역 사전 조회 | 카카오톡/웹 | `POST /kakao/skill/dictionary/search` |
| UC-009 | 번역 사전 수정 | 웹 | `PUT /admin/api/dictionary/{id}` |
| UC-010 | 사전 자동 매핑 | 카카오톡 | `POST /kakao/skill/dictionary/auto-map` |
| UC-011 | 성과 보고서 생성 | 웹 | `GET /admin/api/reports/performance.csv` |
| UC-012 | 기간 전환 | 웹 | `POST /admin/api/system/phase` |
| UC-013 | 개인 휴식 설정 | 웹 | `POST /admin/api/users/{id}/rest` |
| UC-014 | 관리자 승급 | 웹 | `POST /admin/api/users/{id}/promote` |
| UC-015 | 관리자 강등 | 웹 | `POST /admin/api/users/{id}/demote` |
| UC-016 | 인증 토큰 발급 | 카카오톡 | `POST /kakao/skill/admin/token/issue` |
| UC-017 | 인증 토큰 재발급 | 카카오톡 | `POST /kakao/skill/admin/token/reissue` |

---

## 참고 문서 요약

| 문서 | 용도 |
|------|------|
| `docs/PRD.md` | 유스케이스 명세서, 비즈니스 규칙, 데이터 모델 |
| `docs/TECHNICAL_ARCHITECTURE.md` | Hexagonal Architecture, 레이어 구성, 명명 규칙 |
| `docs/TECHNICAL_DECISIONS.md` | 기술 스택, DB 설정, 테스트 전략 |

---

## 빠른 참조

### 개발 프로세스 요약

1. **유스케이스 선택** → PRD에서 UC-XXX 확인
2. **요구사항 분석** → 흐름, 규칙, 데이터 모델 분석
3. **설계** → Domain → Application → Adapter (Inside-Out)
4. **구현** → Domain → UseCase → Service → Persistence → Controller
5. **테스트** → Testcontainers MySQL 통합 테스트
6. **PR 생성** → 체크리스트 확인 후 제출

### 핵심 원칙

- **도메인은 프레임워크를 모른다**: JPA 어노테이션 금지
- **의존성은 코어 방향으로만**: Adapter → Application → Domain
- **Entity는 DTO로 변환하여 반환**: 직접 노출 금지
- **테스트는 프로덕션과 동일 환경**: MySQL 8.x + Testcontainers
