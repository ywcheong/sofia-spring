---
name: develop
description: 이 Skill은 Sofia 프로젝트에서 새로운 로직을 구현할 때 따라야 할 표준 워크플로우를 정의합니다.
---

## 개발 워크플로우

### 1단계: 유스케이스 확인

**확인 사항**:
- 유스케이스 ID, 행위자, 인터페이스 (카카오톡/웹/스케줄러)
- 관련 비즈니스 규칙 (BR-XXX)

---

### 2단계: 설계 (Inside-Out)

**설계 순서**:
```
1. domain/          <- 핵심 비즈니스 로직 (JPA 어노테이션 금지)
2. application/     <- 유스케이스 조율
   ├── port/inbound/   <- UseCase 인터페이스, Command, Result
   ├── port/outbound/  <- 영속성/외부 서비스 Port
   └── service/        <- UseCase 구현체
3. adapter/          <- 외부 세계 연결
   ├── inbound/        <- HTTP, 스케줄러 등
   └── outbound/       <- JPA, 이메일 등
```

---

### 3단계: 구현

**명명 규칙**:

| 레이어 | 패턴 | 예시 |
|--------|------|------|
| Inbound Port | `[동사][명사]UseCase` | `IssueWarningUseCase` |
| Outbound Port | `[Load/Save][명사]Port` | `LoadUserPort` |
| Service | `[도메인명]Service` | `WarningService` |
| Controller (Kakao) | `[명사]Controller` | `ApplicationController` |
| Controller (API) | `[명사]Controller` | `SystemPhaseController` |
| JPA Entity | `[명사]JpaEntity` | `UserJpaEntity` |
| Persistence Adapter | `[명사]PersistenceAdapter` | `UserPersistenceAdapter` |

**핵심 규칙**:
- Domain 레이어는 JPA 어노테이션 사용 금지
- 의존성은 코어(direction, application) 방향으로만 향함
- Entity는 Response DTO로 변환하여 반환 (직접 노출 금지)

---

### 4단계: 테스트 구현

- **통합 테스트 중심**: SpringBootTest + Testcontainers MySQL
- **테스트 명명**: `[상황] 때 [결과] 여야 한다`
