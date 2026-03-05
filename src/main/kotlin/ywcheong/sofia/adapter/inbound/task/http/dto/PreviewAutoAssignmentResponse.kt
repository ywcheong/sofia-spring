package ywcheong.sofia.adapter.inbound.task.http.dto

data class PreviewAutoAssignmentResponse(
    val nextAssigneeStudentNumber: String?,
    val nextAssigneeName: String?,
    val availableAssigneeCount: Int,
    val message: String,
)
