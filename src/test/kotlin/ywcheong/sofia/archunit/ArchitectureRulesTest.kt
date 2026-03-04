package ywcheong.sofia.archunit

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.CompositeArchRule
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController

@AnalyzeClasses(
    packages = ["ywcheong.sofia"],
    importOptions = [DoNotIncludeTests::class],
)
class ArchitectureRulesTest {
    @ArchTest
    val domainShouldNotDependOnFrameworks: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("ywcheong.sofia.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..")
            .because("헥사고날 아키텍처의 핵심 원칙: 도메인은 프레임워크에 독립적이어야 하며 비즈니스 로직만 포함해야 함")
            .allowEmptyShould(true)

    @ArchTest
    val dependencyDirectionShouldFollowHexagonalFlow: ArchRule =
        CompositeArchRule
            .of(
                noClasses()
                    .that()
                    .resideInAnyPackage("ywcheong.sofia.application..", "ywcheong.sofia.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("ywcheong.sofia.adapter..")
                    .allowEmptyShould(true),
            ).and(
                noClasses()
                    .that()
                    .resideInAPackage("ywcheong.sofia.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("ywcheong.sofia.application..", "ywcheong.sofia.adapter..")
                    .allowEmptyShould(true),
            ).because("헥사고날 아키텍처의 의존성 규칙: 외부(Adapter) → 내부(Application → Domain)로만 의존해야 하며 역방향 의존은 금지됨")

    @ArchTest
    val jpaEntitiesShouldHaveTimestamps: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.adapter.out.persistence..")
            .and()
            .haveSimpleNameEndingWith("JpaEntity")
            .should(haveFieldNamed("createdAt"))
            .andShould(haveFieldNamed("updatedAt"))
            .because("영속성 어댑터의 표준: JPA 엔티티는 데이터 추적을 위해 생성일시와 수정일시를 반드시 포함해야 함")
            .allowEmptyShould(true)

    @ArchTest
    val applicationServicesShouldBeNamedService: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.application.service..")
            .and()
            .areNotInterfaces()
            .should()
            .haveSimpleNameEndingWith("Service")
            .because("애플리케이션 계층의 명명 규칙: 서비스 구현체는 'Service' 접미사를 사용하여 명확히 식별 가능해야 함")
            .allowEmptyShould(true)

    @ArchTest
    val inboundPortsShouldBeNamedUseCase: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.application.port.in..")
            .and()
            .areInterfaces()
            .should()
            .haveSimpleNameEndingWith("UseCase")
            .because("헥사고날 아키텍처의 인바운드 포트: 유스케이스 인터페이스는 'UseCase' 접미사를 사용하여 비즈니스 의도를 명확히 표현해야 함")
            .allowEmptyShould(true)

    @ArchTest
    val outboundPortsShouldBeNamedPort: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.application.port.out..")
            .and()
            .areInterfaces()
            .should()
            .haveSimpleNameEndingWith("Port")
            .because("헥사고날 아키텍처의 아웃바운드 포트: 외부 의존성은 'Port' 접미사를 가진 인터페이스를 통해 추상화되어야 함")
            .allowEmptyShould(true)

    @ArchTest
    val persistenceAdaptersShouldFollowNamingConventions: ArchRule =
        CompositeArchRule
            .of(
                classes()
                    .that()
                    .resideInAPackage("ywcheong.sofia.adapter.out.persistence..")
                    .and()
                    .areInterfaces()
                    .should()
                    .haveSimpleNameEndingWith("JpaRepository")
                    .because("영속성 어댑터 명명 규칙: Spring Data JPA 인터페이스는 'JpaRepository' 접미사를 사용해야 함")
                    .allowEmptyShould(true),
            ).and(
                classes()
                    .that()
                    .resideInAPackage("ywcheong.sofia.adapter.out.persistence..")
                    .and()
                    .areNotInterfaces()
                    .should(haveSimpleNameEndingWithAny("JpaEntity", "PersistenceAdapter"))
                    .because("영속성 어댑터 명명 규칙: 구현체는 'JpaEntity'(엔티티) 또는 'PersistenceAdapter'(어댑터) 접미사를 사용해야 함")
                    .allowEmptyShould(true),
            )

    @ArchTest
    val controllersShouldFollowNamingConventions: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.adapter.in..")
            .and()
            .areAnnotatedWith(RestController::class.java)
            .or()
            .areAnnotatedWith(Controller::class.java)
            .should(haveSimpleNameEndingWithAny("Controller", "SkillController"))
            .because("인바운드 어댑터 명명 규칙: 컨트롤러는 'Controller' 접미사를 사용하여 진입점을 명확히 식별할 수 있어야 함")
            .allowEmptyShould(true)

    @ArchTest
    val commonLayerShouldNotDependOnBusinessLayers: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("ywcheong.sofia.common..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("ywcheong.sofia.domain..", "ywcheong.sofia.application..")
            .because("공통 계층의 독립성: common 패키지는 비즈니스 로직에 의존하지 않고 재사용 가능한 유틸리티로 유지되어야 함")
            .allowEmptyShould(true)

    @ArchTest
    val servicesShouldImplementAtLeastOneUseCase: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.application.service..")
            .and()
            .areNotInterfaces()
            .should(implementAtLeastOneUseCase())
            .because("서비스 계층의 책임: 서비스 구현체는 최소 하나 이상의 유스케이스 인터페이스를 구현하여 비즈니스 계약을 이행해야 함")
            .allowEmptyShould(true)

    @ArchTest
    val transactionalShouldOnlyBeUsedAtMethodLevel: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("ywcheong.sofia..")
            .should()
            .beAnnotatedWith(Transactional::class.java)
            .because("@Transactional 가시성 원칙: 클래스 레벨 선언은 메서드와 거리가 멀어 트랜잭션 적용 여부를 놓치기 쉬우므로 반드시 개별 메서드에만 선언해야 함")
            .allowEmptyShould(true)

    private fun haveSimpleNameEndingWithAny(vararg suffixes: String): ArchCondition<JavaClass> =
        object : ArchCondition<JavaClass>(
            "have simple name ending with one of ${suffixes.joinToString()}",
        ) {
            override fun check(
                item: JavaClass,
                events: ConditionEvents,
            ) {
                val matches = suffixes.any { suffix -> item.simpleName.endsWith(suffix) }
                events.add(
                    SimpleConditionEvent(
                        item,
                        matches,
                        "${item.name} should end with one of ${suffixes.joinToString()}",
                    ),
                )
            }
        }

    private fun haveFieldNamed(fieldName: String): ArchCondition<JavaClass> =
        object : ArchCondition<JavaClass>("have field '$fieldName'") {
            override fun check(
                item: JavaClass,
                events: ConditionEvents,
            ) {
                val hasField = item.allFields.any { it.name == fieldName }
                events.add(SimpleConditionEvent(item, hasField, "${item.name} should have field '$fieldName'"))
            }
        }

    private fun implementAtLeastOneUseCase(): ArchCondition<JavaClass> =
        object : ArchCondition<JavaClass>("implement at least one UseCase interface") {
            override fun check(
                item: JavaClass,
                events: ConditionEvents,
            ) {
                val implementsUseCase =
                    item.interfaces.any { interfaceType ->
                        interfaceType.name.startsWith("ywcheong.sofia.application.port.in.") &&
                            interfaceType.name.endsWith(
                                "UseCase",
                            )
                    }
                events.add(
                    SimpleConditionEvent(
                        item,
                        implementsUseCase,
                        "${item.name} should implement at least one inbound UseCase interface",
                    ),
                )
            }
        }
}
