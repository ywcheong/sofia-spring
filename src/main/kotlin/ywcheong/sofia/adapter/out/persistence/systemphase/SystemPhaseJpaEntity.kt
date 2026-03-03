package ywcheong.sofia.adapter.out.persistence.systemphase

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.AuditTable
import org.hibernate.envers.Audited
import ywcheong.sofia.adapter.out.persistence.common.BaseTimeJpaEntity
import ywcheong.sofia.domain.enums.PhaseType
import java.time.Instant

@Entity
@Table(name = "system_phase")
@Audited
@AuditTable(value = "system_phase_aud")
class SystemPhaseJpaEntity(
    @Id
    @Column(name = "id")
    val id: Long = 1L,
    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type", nullable = false)
    var phaseType: PhaseType,
    @Column(name = "start_date", nullable = false)
    var startDate: Instant,
) : BaseTimeJpaEntity()
