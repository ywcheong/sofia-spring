_# Sofia
Sofia는 한국과학영재학교 국제부 자체 시스템임
번역버디에게 번역 과제를 자동 할당하고, 번역 성과를 추적하여 봉사시간을 산출하는 관리 시스템임

## 시스템 구조
기술 스택으로 Kotlin, Spring Boot 4.x, JPA + Hibernate, MySQL, Gradle 사용함
아키텍처는 Hexagonal Architecture 사용함
domain은 외부 의존성 없이 구현 (jpa 등 미사용)

입력 문제로 4xx 응답이 필요한 경우 `BusinessException`을 상속한 예외 사용.
기타 모든 예외는 서버 장애인 5xx로 매핑됨.

- feature = dictionary, systemphase, ... 
- tech = http, kakao, email, scheduler, ...
  
- adapter/
  - inbound/common/{tech}/*Config, *Handler, ...
  - inbound/{feature}/{tech}/(*Controller, dto/*Request,*Response)
  - outbound/common/{tech}/*Config, *Handler, ...
	- outbound/{feature}/jpa/(*JpaEntity, *JpaRepository, *JpaAdapter...)
	- outbound/{feature}/{tech}/(*Adapter...)
- application/
  - port/inbound/{feature}/(*Usecase, dto/*Command,*Result, exceptions/*Exception)
  - port/outbound/{feature}/(*Port, dto/*Command,*Result, exceptions/*Exception)
	- service/{feature}/*Service
	- service/common/{tech}/(*Config, *Handler, ...)
- domain/
  - common/
  - {feature}/entity/... 
  - {feature}/value/...
  - {feature}/enum/...
  - {feature}/exceptions/

### 지식 저장소
`.claude/ai-context/{}.md`에 구체적인 지식이 있음.
지식에 영향을 끼치는 행동을 한다면, 지식도 업데이트해야 함. 
단, CLAUDE.md는 짧게 핵심만 유지.

- business-summary.md: 비즈니스 기초지식 가이드 및 유스케이스 명세
- domain-entities.json:	현재 구현된 도메인 엔티티 카탈로그 (주의: JPA @Entity 아님)
- adapter-inbound-http-spec.json: 현재 구현된 Adapter Inbound, HTTP API 스펙
- adapter-outbound-jpa-spec.json: 현재 구현된 Adapter Outbound, JPA 스펙

`.claude/skills/{SKILLNAME}/SKILL.md`에 스킬이 있음
- develop: 개발 과정에서 필요한 스킬_
