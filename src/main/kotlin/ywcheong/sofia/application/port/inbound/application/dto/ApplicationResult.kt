package ywcheong.sofia.application.port.inbound.application.dto

import ywcheong.sofia.domain.application.enums.ApplicationStatus
import java.time.Instant

data class ApplicationResult(
    val studentNumber: String,
    val name: String,
    val status: ApplicationStatus,
    val appliedAt: Instant,
)
