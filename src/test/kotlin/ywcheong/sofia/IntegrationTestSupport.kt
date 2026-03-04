package ywcheong.sofia

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.MySQLContainer

@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=create"])
@Suppress("UtilityClassWithPublicConstructor")
abstract class IntegrationTestSupport {
    companion object {
        @JvmStatic
        @ServiceConnection
        val mysqlContainer: MySQLContainer<*> =
            MySQLContainer("mysql:8.4").apply {
                start()
            }

        @JvmStatic
        @DynamicPropertySource
        fun registerDataSourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
            registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName)
        }
    }
}
