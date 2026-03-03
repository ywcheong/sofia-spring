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
            .because("domain should stay framework-agnostic")
            .allowEmptyShould(true)

    @ArchTest
    val dependencyDirectionShouldFollowHexagonalFlow: ArchRule =
        CompositeArchRule
            .of(
                noClasses()
                    .that()
                    .resideInAPackage("ywcheong.sofia.adapter..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("ywcheong.sofia.domain..")
                    .allowEmptyShould(true),
            ).and(
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
            ).because("dependencies must flow adapter -> application -> domain without reverse coupling")

    @ArchTest
    val jpaEntitiesShouldHaveTimestamps: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.adapter.out.persistence..")
            .and()
            .haveSimpleNameEndingWith("JpaEntity")
            .should(haveFieldNamed("createdAt"))
            .andShould(haveFieldNamed("updatedAt"))
            .because("JPA entities must track creation and update timestamps")
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
            .because("application service implementations should be explicitly recognizable")
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
            .because("inbound ports represent use cases and should be named accordingly")
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
            .because("outbound ports should expose external dependencies via port interfaces")
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
                    .because("persistence repository interfaces should follow Spring Data naming")
                    .allowEmptyShould(true),
            ).and(
                classes()
                    .that()
                    .resideInAPackage("ywcheong.sofia.adapter.out.persistence..")
                    .and()
                    .areNotInterfaces()
                    .should(haveSimpleNameEndingWithAny("JpaEntity", "PersistenceAdapter"))
                    .because("persistence implementation classes should be entity or adapter types")
                    .allowEmptyShould(true),
            )

    @ArchTest
    val controllersShouldFollowNamingConventions: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.adapter.in..")
            .and()
            .areNotInterfaces()
            .should(haveSimpleNameEndingWithAny("Controller", "SkillController"))
            .because("inbound adapters should be easy to identify as controllers")
            .allowEmptyShould(true)

    @ArchTest
    val commonLayerShouldNotDependOnBusinessLayers: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("ywcheong.sofia.common..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("ywcheong.sofia.domain..", "ywcheong.sofia.application..")
            .because("common should remain a reusable shared layer without business dependencies")
            .allowEmptyShould(true)

    @ArchTest
    val servicesShouldImplementAtLeastOneUseCase: ArchRule =
        classes()
            .that()
            .resideInAPackage("ywcheong.sofia.application.service..")
            .and()
            .areNotInterfaces()
            .should(implementAtLeastOneUseCase())
            .because("service implementations should realize inbound use case contracts")
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
                            interfaceType.name.endsWith("UseCase")
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
