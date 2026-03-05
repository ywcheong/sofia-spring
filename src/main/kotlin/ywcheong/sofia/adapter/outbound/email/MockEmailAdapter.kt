package ywcheong.sofia.adapter.outbound.email

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ywcheong.sofia.application.port.outbound.email.SendEmailPort

@Component
class MockEmailAdapter : SendEmailPort {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendApprovalNotification(
        recipientEmail: String,
        studentNumber: String,
        name: String,
    ) {
        log.info("[MOCK EMAIL] 승인 알림 발송: to=$recipientEmail, studentNumber=$studentNumber, name=$name")
    }

    override fun sendRejectionNotification(
        recipientEmail: String,
        studentNumber: String,
        name: String,
        rejectionReason: String?,
    ) {
        log.info(
            "[MOCK EMAIL] 거절 알림 발송: to=$recipientEmail, studentNumber=$studentNumber, name=$name, reason=$rejectionReason",
        )
    }
}
