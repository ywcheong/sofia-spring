package ywcheong.sofia.adapter.common.jpa

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@SQLDelete(sql = "UPDATE {h-table} SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
abstract class BaseSoftDeleteJpaEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null
        protected set

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant? = null
        protected set

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false
        protected set

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
        protected set
}
