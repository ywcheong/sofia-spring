package ywcheong.sofia.application.port.inbound.task.dto

data class AvailableAssigneeResult(
    val studentNumber: String,
    val name: String,
    val isResting: Boolean,
    val lastAssignedAt: String?,
    val totalCharacterCount: Long,
    val assignedTaskCount: Long,
)
