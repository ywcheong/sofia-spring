package ywcheong.sofia.application.port.outbound.application

import ywcheong.sofia.domain.application.entity.Application
import ywcheong.sofia.domain.application.value.StudentNumber

interface LoadApplicationPort {
    fun existsByStudentNumber(studentNumber: StudentNumber): Boolean

    fun findByStudentNumber(studentNumber: StudentNumber): Application?
}
