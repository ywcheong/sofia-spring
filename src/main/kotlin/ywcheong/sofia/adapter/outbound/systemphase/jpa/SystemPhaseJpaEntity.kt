package ywcheong.sofia.adapter.outbound.systemphase.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.AuditTable
import org.hibernate.envers.Audited
import ywcheong.sofia.adapter.common.jpa.BaseTimeJpaEntity
import ywcheong.sofia.adapter.common.jpa.BypassPrimaryKeyConvention
import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import java.time.Instant

@Entity
@Table(name = "system_phase")
@Audited
@AuditTable(value = "system_phase_aud")
@BypassPrimaryKeyConvention(reason = "싱글톤 엔티티: 시스템당 단일 레코드만 존재하며 고정된 PK 사용")
class SystemPhaseJpaEntity(
    @Id
    @Column(name = "id")
    val id: Long = 1L,
    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type", nullable = false)
    var systemPhaseType: SystemPhaseType,
    @Column(name = "start_date", nullable = false)
    var startDate: Instant,
) : BaseTimeJpaEntity()
