package ywcheong.sofia

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@Suppress("UtilityClassWithPublicConstructor")
abstract class IntegrationTestSupport {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val mysqlContainer: MySQLContainer<*> = MySQLContainer("mysql:8.4")
    }
}
