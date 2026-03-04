package ywcheong.sofia.adapter.outbound.persistence.common

/**
 * JPA 엔티티의 기본키 규칙 검증을 우회하기 위한 어노테이션.
 *
 * 적용 대상: JPA 엔티티 클래스 (@Entity)
 *
 * 사용 사례:
 * - 싱글톤 패턴 엔티티 (시스템당 단일 레코드)
 * - 레거시 스키마 마이그레이션 중인 엔티티
 * - 외부 시스템에서 키를 제공받는 특수 케이스
 *
 * 주의: 이 어노테이션 사용 시 reason에 반드시 우회 사유를 명시해야 합니다.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class BypassPrimaryKeyConvention(
    val reason: String,
)
