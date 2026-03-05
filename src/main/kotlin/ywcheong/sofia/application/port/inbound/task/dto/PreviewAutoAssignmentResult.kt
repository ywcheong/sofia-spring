package ywcheong.sofia.application.port.inbound.task.dto

data class PreviewAutoAssignmentResult(
    val nextAssigneeStudentNumber: String?,
    val nextAssigneeName: String?,
    val availableAssigneeCount: Int,
    val message: String,
)
