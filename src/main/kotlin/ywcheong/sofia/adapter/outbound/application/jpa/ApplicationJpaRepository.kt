package ywcheong.sofia.adapter.outbound.application.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ApplicationJpaRepository : JpaRepository<ApplicationJpaEntity, UUID> {
    fun existsByStudentNumber(studentNumber: String): Boolean

    fun findByStudentNumber(studentNumber: String): ApplicationJpaEntity?
}
