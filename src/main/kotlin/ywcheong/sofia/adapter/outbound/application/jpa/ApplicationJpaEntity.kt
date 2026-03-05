package ywcheong.sofia.adapter.outbound.application.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import ywcheong.sofia.adapter.common.jpa.BaseTimeJpaEntity
import ywcheong.sofia.domain.application.enums.ApplicationStatus
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "applications")
class ApplicationJpaEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Column(name = "student_number", nullable = false, unique = true, length = 6)
    val studentNumber: String,
    @Column(name = "name", nullable = false, length = 10)
    val name: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ApplicationStatus,
    @Column(name = "applied_at", nullable = false)
    val appliedAt: Instant,
    @Column(name = "rejection_reason", length = 500)
    val rejectionReason: String? = null,
    @Column(name = "processed_at")
    val processedAt: Instant? = null,
    @Column(name = "is_resting", nullable = false)
    var isResting: Boolean = false,
    @Column(name = "is_email_subscribed", nullable = false)
    var isEmailSubscribed: Boolean = true,
    @Column(name = "total_character_count", nullable = false)
    val totalCharacterCount: Long = 0L,
    @Column(name = "adjusted_character_count", nullable = false)
    val adjustedCharacterCount: Long = 0L,
    @Column(name = "warning_count", nullable = false)
    val warningCount: Int = 0,
    @Column(name = "last_assigned_at")
    val lastAssignedAt: Instant? = null,
) : BaseTimeJpaEntity()
