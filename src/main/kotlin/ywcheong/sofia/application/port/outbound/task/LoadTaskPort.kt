package ywcheong.sofia.application.port.outbound.task

import ywcheong.sofia.domain.task.entity.Task
import ywcheong.sofia.domain.task.enum.TaskStatus
import java.util.UUID

interface LoadTaskPort {
    fun existsByWorkId(workId: String): Boolean

    fun findById(taskId: UUID): Task?

    fun findByAssigneeId(assigneeId: UUID): List<Task>

    fun findByAssigneeIdAndStatusIn(
        assigneeId: UUID,
        statuses: List<TaskStatus>,
    ): List<Task>

    fun countByAssigneeIdAndStatusIn(
        assigneeId: UUID,
        statuses: List<TaskStatus>,
    ): Long
}
