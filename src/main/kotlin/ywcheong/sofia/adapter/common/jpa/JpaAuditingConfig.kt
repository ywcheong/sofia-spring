package ywcheong.sofia.adapter.common.jpa

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.Clock
import java.time.Instant
import java.util.Optional

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "sofiaDateTimeProvider")
class JpaAuditingConfig {
    @Bean
    fun sofiaDateTimeProvider(clock: Clock): DateTimeProvider = DateTimeProvider { Optional.of(Instant.now(clock)) }
}
