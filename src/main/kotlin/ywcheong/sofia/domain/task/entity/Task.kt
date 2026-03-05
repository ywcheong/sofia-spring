package ywcheong.sofia.domain.task.entity

import ywcheong.sofia.domain.task.enum.TaskStatus
import ywcheong.sofia.domain.task.enum.TaskType
import java.time.Instant
import java.util.UUID

data class Task(
    val id: UUID? = null,
    val taskType: TaskType,
    val workId: String,
    val assigneeId: UUID,
    val status: TaskStatus = TaskStatus.ASSIGNED,
    val assignedAt: Instant,
    val completedAt: Instant? = null,
    val characterCount: Long? = null,
) {
    init {
        validate()
    }

    fun validate() {
        require(workId.length <= MAX_WORK_ID_LENGTH) {
            "작업 ID는 ${MAX_WORK_ID_LENGTH}자 이하여야 합니다. 입력값: ${workId.length}자"
        }
        require(workId.isNotBlank()) {
            "작업 ID는 비어있을 수 없습니다."
        }
        if (characterCount != null) {
            require(characterCount >= 0) {
                "글자 수는 0 이상이어야 합니다. 입력값: $characterCount"
            }
        }
        if (completedAt != null) {
            require(!completedAt.isBefore(assignedAt)) {
                "완료 시각은 할당 시각 이후여야 합니다."
            }
        }
    }

    fun complete(
        completedAt: Instant,
        characterCount: Long? = null,
    ): Task {
        require(status == TaskStatus.ASSIGNED) { "이미 완료된 과제입니다" }
        return copy(
            status = TaskStatus.COMPLETED,
            completedAt = completedAt,
            characterCount = characterCount,
        )
    }

    companion object {
        private const val MAX_WORK_ID_LENGTH = 50

        fun create(
            taskType: TaskType,
            workId: String,
            assigneeId: UUID,
            assignedAt: Instant,
        ): Task =
            Task(
                id = null,
                taskType = taskType,
                workId = workId,
                assigneeId = assigneeId,
                status = TaskStatus.ASSIGNED,
                assignedAt = assignedAt,
                completedAt = null,
                characterCount = null,
            )
    }
}
