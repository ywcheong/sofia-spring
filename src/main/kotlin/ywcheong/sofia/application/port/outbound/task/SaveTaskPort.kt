package ywcheong.sofia.application.port.outbound.task

import ywcheong.sofia.domain.task.entity.Task

interface SaveTaskPort {
    fun save(task: Task): Task
}
