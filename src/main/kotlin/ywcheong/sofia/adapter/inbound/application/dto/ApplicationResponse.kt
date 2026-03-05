package ywcheong.sofia.adapter.inbound.application.dto

import ywcheong.sofia.domain.application.enums.ApplicationStatus
import java.time.Instant

data class ApplicationResponse(
    val studentNumber: String,
    val name: String,
    val status: ApplicationStatus,
    val appliedAt: Instant,
)
