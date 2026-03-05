package ywcheong.sofia.adapter.inbound.task.http.dto

import ywcheong.sofia.domain.task.enum.AssignmentMethod
import ywcheong.sofia.domain.task.enum.TaskType

data class CreateTaskRequest(
    val taskType: TaskType,
    val workId: String,
    val assignmentMethod: AssignmentMethod,
    val manualAssigneeStudentNumber: String? = null,
)
