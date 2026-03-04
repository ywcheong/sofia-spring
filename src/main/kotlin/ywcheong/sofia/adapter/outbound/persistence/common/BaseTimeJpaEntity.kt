package ywcheong.sofia.adapter.outbound.persistence.common

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeJpaEntity {
    @CreatedDate
    var createdAt: Instant? = null
        protected set

    @LastModifiedDate
    var updatedAt: Instant? = null
        protected set
}
