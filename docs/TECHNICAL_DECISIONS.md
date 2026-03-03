# Sofia PRD 구현 결정 사항

## 저장소 정보 (확인된 내용)
- 기술 스택: Kotlin + Spring Boot (Gradle, Java 21)
- 현재 코드: `src/main/kotlin/ywcheong/sofia/SofiaApplication.kt` 하나만 존재 (controller/service/entity 없음)
- 포함된 의존성: WebMVC, RestClient, Actuator, Data JPA, MySQL driver
- ORM: Hibernate `hibernate-core:7.2.4.Final` (Spring Boot 4.0.3 BOM을 통해)
- Envers: 현재 runtimeClasspath에 없음 → `org.hibernate.orm:hibernate-envers` 추가 필요
- 테스트: JUnit5 + SpringBootTest 스켈레톤만 존재
- CI workflow 없음, Docker 없음, `src/main/resources/application.yaml` 외 환경 템플릿 없음

## 목표 (기술적)
- Kotlin Spring Boot를 사용하여 `docs/PRD.md`에 기술된 시스템(챗봇 인터페이스 + 관리자 웹 인터페이스 + 시스템 잡)을 구현한다.

## 확정된 결정 사항
- 토폴로지: 챗봇 + 관리자 웹 + 스케줄러/이메일 기능을 하나의 배포물로 포함하는 단일 모놀리식 Spring Boot 앱
- 카카오 연동: Kakao i Open Builder 스킬 서버(웹훅 → 즉시 JSON 응답)
- 관리자 웹 UI: 서버 렌더링 MVC (Spring MVC + 템플릿)
- 템플릿 엔진: 사용하지 않음
- 관리자 웹 인증: Spring Security + 세션 로그인 (토큰 1회 입력 → 세션 쿠키 유지)
- DB 스키마 관리:
	- 초기 개발 단계: Hibernate `ddl-auto=update`로 빠른 개발
	- 첫 공유 환경 배포 전: Flyway 베이스라인 + 마이그레이션 도입
	- 비개발 환경(CI/staging/prod): Flyway 마이그레이션 + `ddl-auto=validate`
- 라운드로빈 동시성 제어: 커서 테이블 단일 행 잠금(`SELECT ... FOR UPDATE`)
- 이메일: 실제 발송하지 않음; EmailLog만 기록(프로바이더 연동은 추후)
- 수신 거부 기능: 지금 구현(엔드포인트 + 검증 + 토글), 이메일 발송 연동은 추후
- 수신 거부 검증: 사용자별 비밀키(`emailAuthSecret`) 해시 저장; 링크에는 원문 비밀키 포함
- DB 엔진: MySQL 8.x
- 테스트 DB: Testcontainers MySQL
- 로컬 개발 DB: Docker Compose MySQL
- 관리자 인증 토큰 형식: 불투명 난수형 토큰; 해시를 DB에 저장
- PK 전략: 모든 테이블에서 BIGINT 대리 PK 사용, 비즈니스 키는 UNIQUE로 강제
- Enum 저장 방식: STRING
- 사전(Dictionary) 모델링: (한글, 영어) 쌍 당 한 행 (다대다 관계는 여러 행으로 표현)
- 카카오 Open Builder 상태: 슬롯 채우기/파라미터는 Open Builder에서 처리; 서버는 대부분 stateless
- 카카오 사용자 식별: Kakao `user_key`(또는 동등 필드)를 User에 바인딩 및 영속화; 챗봇 유스케이스는 이를 통해 사용자 식별
- 참여 신청(ParticipationApplication): `ParticipationApplication` 테이블 추가; 승인 시 User 생성/갱신
- 카카오 스킬 라우팅: 단일 디스패처 엔드포인트가 아닌, 스킬별 별도 엔드포인트
- 카카오 스킬 경로 명명: 도메인/행위 기반 (UC-ID 기반 아님)
- DB 명명 규칙: 테이블/컬럼 snake_case
- DB 타임스탬프: UTC로 저장, 표시 시점에 KST 변환
- 패키지 구성: 기능/도메인 단위로 구성(feature/domain-based slices)
- 관리자 웹: GET 페이지는 MVC, 변경(mutation)은 `/admin/api/**` 하의 JSON 요청
- 카카오 스킬: Kakao 타겟 유스케이스 1개당 1 엔드포인트
- 카카오 스킬 엔드포인트 (고정):
	- UC-001: `POST /kakao/skill/apply`
	- UC-004: `POST /kakao/skill/work/report-completion`
	- UC-005: `POST /kakao/skill/me/stats`
	- UC-008: `POST /kakao/skill/dictionary/search`
	- UC-010: `POST /kakao/skill/dictionary/auto-map`
	- UC-016: `POST /kakao/skill/admin/token/issue`
	- UC-017: `POST /kakao/skill/admin/token/reissue`
- 카카오 스킬 요청 검증: 모든 스킬에 공통 적용되는 전역 정적 비밀 헤더
- 스케줄 작업: 애플리케이션 내부 `@Scheduled` 사용
- 소프트 삭제: User/Work/DictionaryEntry/Adjustment/AuthToken에 `deleted_at` 적용 (EmailLog/cursor/state 테이블은 제외)
- 소프트 삭제 구현: `deleted` boolean + `deleted_at` timestamp를 활용한 명시적 `@SQLDelete` + `@SQLRestriction`
- 감사 로그 (UC-020): Hibernate Envers 사용; 수정 이력 저장만 우선 구현, 조회/리포트는 추후
	- Envers 적용 범위: User, Work, DictionaryEntry, Adjustment, SystemPhase, AuthToken
- 관리자 UI JS: `/admin/api/**` 호출을 위한 순수 `fetch` 사용
- 테스트 전략: 후행 테스트(tests-after), JUnit5 + SpringBootTest + Testcontainers 통합 테스트
- 공통 감사 타임스탬프: 모든 테이블에 `created_at` / `updated_at` (UTC) 추가

## 결정 순서 (상위 → 하위)
1. 시스템 토폴로지 (단일 앱 vs 분리 서비스; 스케줄러 위치)
2. 카카오 챗봇 연동 방식 (Kakao i Open Builder 스킬 서버 vs 기타 웹훅/API 방식; 요청 검증 방식)
3. 관리자 웹 인터페이스 스타일 (서버 렌더링 MVC vs JSON API + 별도 프론트엔드; 라우팅/레이아웃)
4. 영속성 기본 설정 (DB 엔진/버전, 스키마 마이그레이션 전략, 명명 규칙, 타임존, 문자셋/Collation)
5. 도메인 모델링 방식 (엔터티 경계, enum, ID, 고유 제약; PRD 테이블 매핑 방식)
6. 라운드로빈 배정 동시성 전략 (락 설계, 경합 동작, 테스트 접근법)
7. 관리자 웹 인증 모델 (토큰 발급/저장/해시, 검증, 세션 vs 헤더 토큰, 만료 처리)
8. 이메일 서브시스템 (프로바이더/라이브러리, 템플릿, 로그 테이블/이벤트, 수신 거부 링크/토큰 구조)
9. 스케줄러/백그라운드 잡 (48시간 리마인더 스캔 주기, 멱등성, 재시도 정책)
10. API 계약 및 에러 메시지 (카카오 응답, 관리자 엔드포인트, CSV 내보내기)
11. 검증 전략 (단위/통합 테스트, 테스트 DB 전략, 로컬 개발 환경, CI)

## 미결정 항목
- [결정 필요] (없음)

## 범위 경계
- 포함(INCLUDE): 아키텍처, 모듈 경계, 데이터 모델 매핑, 엔드포인트 구조, 동시성/트랜잭션, 보안/토큰 처리, 스케줄링, 이메일 처리 메커니즘
- 제외(EXCLUDE): 비즈니스 정책 논의 (지급 정책 등 PRD 변경 논의)
