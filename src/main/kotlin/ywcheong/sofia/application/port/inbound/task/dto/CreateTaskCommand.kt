package ywcheong.sofia.application.port.inbound.task.dto

import ywcheong.sofia.domain.task.enum.AssignmentMethod
import ywcheong.sofia.domain.task.enum.TaskType

data class CreateTaskCommand(
    val taskType: TaskType,
    val workId: String,
    val assignmentMethod: AssignmentMethod,
    val manualAssigneeStudentNumber: String? = null,
)
