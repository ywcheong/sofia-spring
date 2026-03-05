package ywcheong.sofia.application.port.outbound.email

interface SendEmailPort {
    fun sendApprovalNotification(
        recipientEmail: String,
        studentNumber: String,
        name: String,
    )

    fun sendRejectionNotification(
        recipientEmail: String,
        studentNumber: String,
        name: String,
        rejectionReason: String?,
    )

    fun sendTaskAssignmentNotification(
        recipientEmail: String,
        studentNumber: String,
        name: String,
        workId: String,
    )
}
