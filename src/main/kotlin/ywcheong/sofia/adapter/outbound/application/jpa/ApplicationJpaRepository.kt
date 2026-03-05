package ywcheong.sofia.adapter.outbound.application.jpa

import org.springframework.data.jpa.repository.JpaRepository
import ywcheong.sofia.domain.application.enums.ApplicationStatus
import java.util.UUID

interface ApplicationJpaRepository : JpaRepository<ApplicationJpaEntity, UUID> {
    fun existsByStudentNumber(studentNumber: String): Boolean

    fun existsByStudentNumberAndStatusIn(
        studentNumber: String,
        statuses: List<ApplicationStatus>,
    ): Boolean

    fun findByStudentNumber(studentNumber: String): ApplicationJpaEntity?
}
