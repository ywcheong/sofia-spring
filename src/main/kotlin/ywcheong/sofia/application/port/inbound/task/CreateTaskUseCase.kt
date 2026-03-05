package ywcheong.sofia.application.port.inbound.task

import ywcheong.sofia.application.port.inbound.task.dto.AvailableAssigneeResult
import ywcheong.sofia.application.port.inbound.task.dto.CreateTaskCommand
import ywcheong.sofia.application.port.inbound.task.dto.CreateTaskResult
import ywcheong.sofia.application.port.inbound.task.dto.PreviewAutoAssignmentResult

interface CreateTaskUseCase {
    fun createTask(command: CreateTaskCommand): CreateTaskResult

    fun previewAutoAssignment(): PreviewAutoAssignmentResult

    fun getAvailableAssignees(): List<AvailableAssigneeResult>
}
