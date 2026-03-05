package ywcheong.sofia.adapter.inbound.task.http.dto

data class AvailableAssigneesResponse(
    val assignees: List<AvailableAssigneeItem>,
) {
    data class AvailableAssigneeItem(
        val studentNumber: String,
        val name: String,
        val isResting: Boolean,
        val lastAssignedAt: String?,
        val totalCharacterCount: Long,
        val assignedTaskCount: Long,
    )
}
