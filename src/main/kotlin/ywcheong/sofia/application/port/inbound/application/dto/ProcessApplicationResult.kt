package ywcheong.sofia.application.port.inbound.application.dto

import ywcheong.sofia.domain.application.enums.ApplicationStatus
import java.time.Instant

data class ProcessApplicationResult(
    val studentNumber: String,
    val name: String,
    val status: ApplicationStatus,
    val rejectionReason: String? = null,
    val appliedAt: Instant,
    val processedAt: Instant,
    val emailPreview: EmailPreview,
) {
    data class EmailPreview(
        val recipientEmail: String,
        val notificationType: String,
        val message: String,
    )
}
