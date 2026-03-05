package ywcheong.sofia.adapter.outbound.task.jpa

import org.springframework.data.jpa.repository.JpaRepository
import ywcheong.sofia.domain.task.enum.TaskStatus
import java.util.UUID

interface TaskJpaRepository : JpaRepository<TaskJpaEntity, UUID> {
    fun existsByWorkId(workId: String): Boolean

    fun findByAssigneeId(assigneeId: UUID): List<TaskJpaEntity>

    fun findByAssigneeIdAndStatusIn(
        assigneeId: UUID,
        statuses: List<TaskStatus>,
    ): List<TaskJpaEntity>

    fun countByAssigneeIdAndStatusIn(
        assigneeId: UUID,
        statuses: List<TaskStatus>,
    ): Long
}
