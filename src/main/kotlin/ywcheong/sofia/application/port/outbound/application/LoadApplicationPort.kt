package ywcheong.sofia.application.port.outbound.application

import ywcheong.sofia.domain.application.entity.Application
import ywcheong.sofia.domain.application.enums.ApplicationStatus
import ywcheong.sofia.domain.application.value.StudentNumber

interface LoadApplicationPort {
    fun existsByStudentNumber(studentNumber: StudentNumber): Boolean

    fun existsByStudentNumberAndStatusIn(
        studentNumber: StudentNumber,
        statuses: List<ApplicationStatus>,
    ): Boolean

    fun findByStudentNumber(studentNumber: StudentNumber): Application?
}
