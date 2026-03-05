package ywcheong.sofia.adapter.outbound.task.jpa

import org.springframework.stereotype.Repository
import ywcheong.sofia.application.common.service.UuidGenerateService
import ywcheong.sofia.application.port.outbound.task.LoadTaskPort
import ywcheong.sofia.application.port.outbound.task.SaveTaskPort
import ywcheong.sofia.domain.task.entity.Task
import ywcheong.sofia.domain.task.enum.TaskStatus
import java.util.UUID

@Repository
class TaskJpaAdapter(
    private val taskJpaRepository: TaskJpaRepository,
    private val uuidGenerateService: UuidGenerateService,
) : LoadTaskPort,
    SaveTaskPort {
    override fun existsByWorkId(workId: String): Boolean = taskJpaRepository.existsByWorkId(workId)

    override fun findById(taskId: UUID): Task? = taskJpaRepository.findById(taskId).orElse(null)?.toDomain()

    override fun findByAssigneeId(assigneeId: UUID): List<Task> = taskJpaRepository.findByAssigneeId(assigneeId).map { it.toDomain() }

    override fun findByAssigneeIdAndStatusIn(
        assigneeId: UUID,
        statuses: List<TaskStatus>,
    ): List<Task> =
        taskJpaRepository
            .findByAssigneeIdAndStatusIn(assigneeId, statuses)
            .map { it.toDomain() }

    override fun countByAssigneeIdAndStatusIn(
        assigneeId: UUID,
        statuses: List<TaskStatus>,
    ): Long =
        taskJpaRepository.countByAssigneeIdAndStatusIn(
            assigneeId,
            statuses,
        )

    override fun save(task: Task): Task {
        val id = task.id ?: uuidGenerateService.generate()
        val jpaEntity =
            TaskJpaEntity(
                id = id,
                taskType = task.taskType,
                workId = task.workId,
                assigneeId = task.assigneeId,
                status = task.status,
                assignedAt = task.assignedAt,
                completedAt = task.completedAt,
                characterCount = task.characterCount,
            )
        taskJpaRepository.save(jpaEntity)
        return task.copy(id = id)
    }

    private fun TaskJpaEntity.toDomain(): Task =
        Task(
            id = id,
            taskType = taskType,
            workId = workId,
            assigneeId = assigneeId,
            status = status,
            assignedAt = assignedAt,
            completedAt = completedAt,
            characterCount = characterCount,
        )
}
