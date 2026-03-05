package ywcheong.sofia.adapter.inbound.task.http.dto

import ywcheong.sofia.domain.task.enum.TaskStatus
import ywcheong.sofia.domain.task.enum.TaskType
import java.time.Instant
import java.util.UUID

data class CreateTaskResponse(
    val taskId: UUID,
    val taskType: TaskType,
    val workId: String,
    val assigneeStudentNumber: String,
    val assigneeName: String,
    val status: TaskStatus,
    val assignedAt: Instant,
    val emailPreview: EmailPreview,
) {
    data class EmailPreview(
        val recipientEmail: String,
        val notificationType: String,
        val message: String,
    )
}
