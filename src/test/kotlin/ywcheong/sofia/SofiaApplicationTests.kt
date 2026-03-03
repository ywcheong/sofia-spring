package ywcheong.sofia

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Clock
import java.time.ZoneOffset

@SpringBootTest
class SofiaApplicationTests : IntegrationTestSupport() {
    @Autowired
    lateinit var clock: Clock

    @Test
    fun contextLoads() {
        // Spring Boot context loads successfully if this test passes
    }

    @Test
    fun clockBeanUsesUtc() {
        assertThat(clock.zone).isEqualTo(ZoneOffset.UTC)
    }
}
