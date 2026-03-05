package ywcheong.sofia.adapter.outbound.task.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import ywcheong.sofia.adapter.common.jpa.BaseTimeJpaEntity
import ywcheong.sofia.domain.task.enum.TaskStatus
import ywcheong.sofia.domain.task.enum.TaskType
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "tasks",
    indexes = [
        Index(name = "idx_tasks_work_id", columnList = "work_id", unique = true),
        Index(name = "idx_tasks_assignee_status", columnList = "assignee_id, status"),
    ],
)
class TaskJpaEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    val taskType: TaskType,
    @Column(name = "work_id", nullable = false, length = 50, unique = true)
    val workId: String,
    @Column(name = "assignee_id", nullable = false)
    val assigneeId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: TaskStatus,
    @Column(name = "assigned_at", nullable = false)
    val assignedAt: Instant,
    @Column(name = "completed_at")
    val completedAt: Instant? = null,
    @Column(name = "character_count")
    val characterCount: Long? = null,
) : BaseTimeJpaEntity()
