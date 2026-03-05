package ywcheong.sofia.adapter.inbound.task.http

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ywcheong.sofia.adapter.inbound.task.http.dto.AvailableAssigneesResponse
import ywcheong.sofia.adapter.inbound.task.http.dto.CreateTaskRequest
import ywcheong.sofia.adapter.inbound.task.http.dto.CreateTaskResponse
import ywcheong.sofia.adapter.inbound.task.http.dto.PreviewAutoAssignmentResponse
import ywcheong.sofia.application.port.inbound.task.CreateTaskUseCase
import ywcheong.sofia.application.port.inbound.task.dto.CreateTaskCommand

@RestController
@RequestMapping("/admin/api/tasks")
class TaskAdminController(
    private val createTaskUseCase: CreateTaskUseCase,
) {
    @PostMapping
    fun createTask(
        @RequestBody request: CreateTaskRequest,
    ): CreateTaskResponse {
        val command =
            CreateTaskCommand(
                taskType = request.taskType,
                workId = request.workId,
                assignmentMethod = request.assignmentMethod,
                manualAssigneeStudentNumber = request.manualAssigneeStudentNumber,
            )
        val result = createTaskUseCase.createTask(command)
        return CreateTaskResponse(
            taskId = result.taskId,
            taskType = result.taskType,
            workId = result.workId,
            assigneeStudentNumber = result.assigneeStudentNumber,
            assigneeName = result.assigneeName,
            status = result.status,
            assignedAt = result.assignedAt,
            emailPreview =
                CreateTaskResponse.EmailPreview(
                    recipientEmail = result.emailPreview.recipientEmail,
                    notificationType = result.emailPreview.notificationType,
                    message = result.emailPreview.message,
                ),
        )
    }

    @GetMapping("/auto-assignment/preview")
    fun previewAutoAssignment(): PreviewAutoAssignmentResponse {
        val result = createTaskUseCase.previewAutoAssignment()
        return PreviewAutoAssignmentResponse(
            nextAssigneeStudentNumber = result.nextAssigneeStudentNumber,
            nextAssigneeName = result.nextAssigneeName,
            availableAssigneeCount = result.availableAssigneeCount,
            message = result.message,
        )
    }

    @GetMapping("/available-assignees")
    fun getAvailableAssignees(): AvailableAssigneesResponse {
        val assignees = createTaskUseCase.getAvailableAssignees()
        return AvailableAssigneesResponse(
            assignees =
                assignees.map {
                    AvailableAssigneesResponse.AvailableAssigneeItem(
                        studentNumber = it.studentNumber,
                        name = it.name,
                        isResting = it.isResting,
                        lastAssignedAt = it.lastAssignedAt,
                        totalCharacterCount = it.totalCharacterCount,
                        assignedTaskCount = it.assignedTaskCount,
                    )
                },
        )
    }
}
