package ywcheong.sofia.application.service.task

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ywcheong.sofia.application.port.inbound.task.CreateTaskUseCase
import ywcheong.sofia.application.port.inbound.task.dto.AvailableAssigneeResult
import ywcheong.sofia.application.port.inbound.task.dto.CreateTaskCommand
import ywcheong.sofia.application.port.inbound.task.dto.CreateTaskResult
import ywcheong.sofia.application.port.inbound.task.dto.PreviewAutoAssignmentResult
import ywcheong.sofia.application.port.outbound.application.LoadApplicationPort
import ywcheong.sofia.application.port.outbound.application.SaveApplicationPort
import ywcheong.sofia.application.port.outbound.email.SendEmailPort
import ywcheong.sofia.application.port.outbound.systemphase.LoadSystemPhasePort
import ywcheong.sofia.application.port.outbound.task.LoadTaskPort
import ywcheong.sofia.application.port.outbound.task.SaveTaskPort
import ywcheong.sofia.domain.application.enums.ApplicationStatus
import ywcheong.sofia.domain.application.value.StudentNumber
import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import ywcheong.sofia.domain.task.entity.Task
import ywcheong.sofia.domain.task.enum.AssignmentMethod
import ywcheong.sofia.domain.task.enum.TaskStatus
import ywcheong.sofia.domain.task.exceptions.AssigneeIsRestingException
import ywcheong.sofia.domain.task.exceptions.AssigneeNotFoundException
import ywcheong.sofia.domain.task.exceptions.DuplicateWorkIdException
import ywcheong.sofia.domain.task.exceptions.InvalidTaskPhaseException
import ywcheong.sofia.domain.task.exceptions.NoAvailableAssigneeException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class TaskService(
    private val loadTaskPort: LoadTaskPort,
    private val saveTaskPort: SaveTaskPort,
    private val loadApplicationPort: LoadApplicationPort,
    private val saveApplicationPort: SaveApplicationPort,
    private val loadSystemPhasePort: LoadSystemPhasePort,
    private val sendEmailPort: SendEmailPort,
    private val clock: Clock,
) : CreateTaskUseCase {
    @Transactional
    override fun createTask(command: CreateTaskCommand): CreateTaskResult {
        // 1. 시스템 페이즈 검증 (BR-005: TRANSLATE 페이즈만 가능)
        validatePhase()

        // 2. Work ID 중복 검사 (BR-006)
        if (loadTaskPort.existsByWorkId(command.workId)) {
            throw DuplicateWorkIdException("이미 존재하는 작업 ID입니다: ${command.workId}")
        }

        // 3. 배정 방식에 따른 assignee 선정
        val now = Instant.now(clock)
        val (assignee, updatedAssignee) =
            when (command.assignmentMethod) {
                AssignmentMethod.AUTO -> selectAutoAssignee(now)
                AssignmentMethod.MANUAL -> selectManualAssignee(command.manualAssigneeStudentNumber)
            }

        // 4. Task 생성 (assigneeId 사용)
        val task =
            Task.create(
                taskType = command.taskType,
                workId = command.workId,
                assigneeId = assignee.id!!,
                assignedAt = now,
            )

        // 5. Task 저장
        val savedTask = saveTaskPort.save(task)

        // 6. assignee의 lastAssignedAt 업데이트
        saveApplicationPort.save(updatedAssignee)

        // 7. 이메일 발송 (수신 허용 시)
        val recipientEmail = "${assignee.studentNumber.value}@ksa.hs.kr"
        var emailMessage = "과제 배정 알림 이메일이 $recipientEmail 로 발송되지 않았습니다. (이메일 수신 거부 상태)"

        if (assignee.isEmailSubscribed) {
            sendEmailPort.sendTaskAssignmentNotification(
                recipientEmail = recipientEmail,
                studentNumber = assignee.studentNumber.value,
                name = assignee.name,
                workId = savedTask.workId,
            )
            emailMessage = "과제 배정 알림 이메일이 $recipientEmail 로 발송됩니다."
        }

        // 8. 결과 반환
        return CreateTaskResult(
            taskId = savedTask.id!!,
            taskType = savedTask.taskType,
            workId = savedTask.workId,
            assigneeStudentNumber = assignee.studentNumber.value,
            assigneeName = assignee.name,
            status = savedTask.status,
            assignedAt = savedTask.assignedAt,
            emailPreview =
                CreateTaskResult.EmailPreview(
                    recipientEmail = recipientEmail,
                    notificationType = "TASK_ASSIGNMENT",
                    message = emailMessage,
                ),
        )
    }

    @Transactional(readOnly = true)
    override fun previewAutoAssignment(): PreviewAutoAssignmentResult {
        // 시스템 페이즈 검증
        validatePhase()

        // 승인되고 휴식 중이 아닌 번역버디 목록 조회
        val availableAssignees = loadApplicationPort.findByStatusAndIsResting(ApplicationStatus.APPROVED, false)

        if (availableAssignees.isEmpty()) {
            return PreviewAutoAssignmentResult(
                nextAssigneeStudentNumber = null,
                nextAssigneeName = null,
                availableAssigneeCount = 0,
                message = "배정 가능한 번역버디가 없습니다.",
            )
        }

        // 라운드 로빈: lastAssignedAt 기준으로 가장 오래된/없는 번역버디 선택 (BR-048)
        val nextAssignee =
            availableAssignees
                .sortedWith(
                    nullsFirst(compareBy { it.lastAssignedAt }),
                ).first()

        return PreviewAutoAssignmentResult(
            nextAssigneeStudentNumber = nextAssignee.studentNumber.value,
            nextAssigneeName = nextAssignee.name,
            availableAssigneeCount = availableAssignees.size,
            message = "다음 자동 배정 대상: ${nextAssignee.name}(${nextAssignee.studentNumber.value})",
        )
    }

    @Transactional(readOnly = true)
    override fun getAvailableAssignees(): List<AvailableAssigneeResult> {
        // 승인된 모든 번역버디 조회 (휴식 중인 경우도 포함하여 수동 배정 시 확인 가능)
        val approvedApplications = loadApplicationPort.findByStatus(ApplicationStatus.APPROVED)

        return approvedApplications.map { app ->
            val assignedTaskCount =
                loadTaskPort.countByAssigneeIdAndStatusIn(
                    app.id!!,
                    listOf(TaskStatus.ASSIGNED),
                )

            AvailableAssigneeResult(
                studentNumber = app.studentNumber.value,
                name = app.name,
                isResting = app.isResting,
                lastAssignedAt = app.lastAssignedAt?.formatDateTime(),
                totalCharacterCount = app.totalCharacterCount,
                assignedTaskCount = assignedTaskCount,
            )
        }
    }

    private fun validatePhase() {
        val systemPhase = loadSystemPhasePort.load()
        if (systemPhase == null || systemPhase.systemPhaseType != SystemPhaseType.TRANSLATE) {
            throw InvalidTaskPhaseException(
                "과제 생성은 번역 페이즈에서만 가능합니다. 현재 페이즈: ${systemPhase?.systemPhaseType ?: "없음"}",
            )
        }
    }

    private fun selectAutoAssignee(
        now: Instant,
    ): Pair<ywcheong.sofia.domain.application.entity.Application, ywcheong.sofia.domain.application.entity.Application> {
        // 승인되고 휴식 중이 아닌 번역버디 목록 조회 (BR-048)
        val availableAssignees = loadApplicationPort.findByStatusAndIsResting(ApplicationStatus.APPROVED, false)

        if (availableAssignees.isEmpty()) {
            throw NoAvailableAssigneeException("배정 가능한 번역버디가 없습니다.")
        }

        // 라운드 로빈: lastAssignedAt 기준으로 가장 오래된/없는 번역버디 선택
        val selectedAssignee =
            availableAssignees
                .sortedWith(
                    nullsFirst(compareBy { it.lastAssignedAt }),
                ).first()

        val updatedAssignee = selectedAssignee.updateLastAssignedAt(now)
        return Pair(selectedAssignee, updatedAssignee)
    }

    private fun selectManualAssignee(
        manualAssigneeStudentNumber: String?,
    ): Pair<ywcheong.sofia.domain.application.entity.Application, ywcheong.sofia.domain.application.entity.Application> {
        if (manualAssigneeStudentNumber.isNullOrBlank()) {
            throw AssigneeNotFoundException("수동 배정할 번역버디 학번이 지정되지 않았습니다.")
        }

        val studentNumber = StudentNumber(manualAssigneeStudentNumber)
        val assignee =
            loadApplicationPort.findByStudentNumber(studentNumber)
                ?: throw AssigneeNotFoundException("해당 학번의 번역버디를 찾을 수 없습니다: $manualAssigneeStudentNumber")

        // 승인된 상태인지 확인
        if (assignee.status != ApplicationStatus.APPROVED) {
            throw AssigneeNotFoundException("승인되지 않은 번역버디입니다: $manualAssigneeStudentNumber")
        }

        // 휴식 중인지 확인 (BR-049)
        if (assignee.isResting) {
            throw AssigneeIsRestingException("휴식 중인 번역버디입니다: $manualAssigneeStudentNumber")
        }

        val now = Instant.now(clock)
        val updatedAssignee = assignee.updateLastAssignedAt(now)
        return Pair(assignee, updatedAssignee)
    }

    private fun Instant.formatDateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Seoul"))
        return formatter.format(this)
    }
}
