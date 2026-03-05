package ywcheong.sofia.application.port.outbound.application

import ywcheong.sofia.domain.application.entity.Application
import ywcheong.sofia.domain.application.enums.ApplicationStatus
import ywcheong.sofia.domain.application.value.StudentNumber
import java.util.UUID

interface LoadApplicationPort {
    fun existsByStudentNumber(studentNumber: StudentNumber): Boolean

    fun existsByStudentNumberAndStatusIn(
        studentNumber: StudentNumber,
        statuses: List<ApplicationStatus>,
    ): Boolean

    fun findByStudentNumber(studentNumber: StudentNumber): Application?

    fun findById(id: UUID): Application?

    fun findByStatus(status: ApplicationStatus): List<Application>

    fun findByStatusAndIsResting(
        status: ApplicationStatus,
        isResting: Boolean,
    ): List<Application>
}
