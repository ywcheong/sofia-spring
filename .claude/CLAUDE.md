# Sofia
Sofia는 한국과학영재학교 국제부 자체 시스템임
번역버디에게 번역 과제를 자동 할당하고, 번역 성과를 추적하여 봉사시간을 산출하는 관리 시스템임

## 시스템 구조
기술 스택으로 Kotlin, Spring Boot 4.x, JPA + Hibernate, MySQL, Gradle 사용함
아키텍처는 Hexagonal Architecture 사용함
domain은 외부 의존성 없이 구현 (jpa 등 미사용)

- adapter/
  - inbound/{feature}/
    - kakao/, http/, email/, scheduler/, ...
	- outbound/{feature}/
    - jpa/, email/, ...
	- common/{tech}/*Config, *Handler...
- application/
  - port/inbound/{feature}/*Usecase
  - port/inbound/{feature}/dto/*Command,*Result
  - port/outbound/{feature}/*Port
	- service/{feature}/*Service
- domain/
  - common,{feature}/
    - entity/, value/, enum/

### 지식 저장소
`.claude/ai-context/{}.md`에 구체적인 지식이 있음

- business-summary.md: 비즈니스 기초 및 심화지식 가이드
- domain-entities.json:	도메인 엔티티 카탈로그 (주의: JPA @Entity 아님)
- adapter-inbound-http-spec.json: Adapter Inbound, HTTP API 스펙
- adapter-outbound-jpa-spec.json: Adapter Outbound, JPA 스펙

`.claude/skills/{SKILLNAME}/SKILL.md`에 스킬이 있음
- develop: 개발 과정에서 필요한 스킬
