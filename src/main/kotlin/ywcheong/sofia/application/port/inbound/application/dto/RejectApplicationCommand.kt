package ywcheong.sofia.application.port.inbound.application.dto

data class RejectApplicationCommand(
    val studentNumber: String,
    val rejectionReason: String?,
)
