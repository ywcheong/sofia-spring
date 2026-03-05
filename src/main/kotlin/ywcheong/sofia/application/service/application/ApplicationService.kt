package ywcheong.sofia.application.service.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ywcheong.sofia.application.port.inbound.application.ApplyParticipationUseCase
import ywcheong.sofia.application.port.inbound.application.ProcessApplicationUseCase
import ywcheong.sofia.application.port.inbound.application.dto.ApplicationResult
import ywcheong.sofia.application.port.inbound.application.dto.ApplyParticipationCommand
import ywcheong.sofia.application.port.inbound.application.dto.ApproveApplicationCommand
import ywcheong.sofia.application.port.inbound.application.dto.ProcessApplicationResult
import ywcheong.sofia.application.port.inbound.application.dto.RejectApplicationCommand
import ywcheong.sofia.application.port.outbound.application.LoadApplicationPort
import ywcheong.sofia.application.port.outbound.application.SaveApplicationPort
import ywcheong.sofia.application.port.outbound.email.SendEmailPort
import ywcheong.sofia.application.port.outbound.systemphase.LoadSystemPhasePort
import ywcheong.sofia.domain.application.entity.Application
import ywcheong.sofia.domain.application.enums.ApplicationStatus
import ywcheong.sofia.domain.application.exceptions.AlreadyProcessedException
import ywcheong.sofia.domain.application.exceptions.ApplicationNotFoundException
import ywcheong.sofia.domain.application.exceptions.DuplicateStudentNumberException
import ywcheong.sofia.domain.application.exceptions.InvalidPhaseException
import ywcheong.sofia.domain.application.value.StudentNumber
import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import java.time.Clock
import java.time.Instant

@Service
class ApplicationService(
    private val loadApplicationPort: LoadApplicationPort,
    private val saveApplicationPort: SaveApplicationPort,
    private val loadSystemPhasePort: LoadSystemPhasePort,
    private val sendEmailPort: SendEmailPort,
    private val clock: Clock,
) : ApplyParticipationUseCase,
    ProcessApplicationUseCase {
    @Transactional
    override fun applyParticipation(command: ApplyParticipationCommand): ApplicationResult {
        // 1. 시스템 페이즈 확인
        val systemPhase = loadSystemPhasePort.load()
        if (systemPhase == null || systemPhase.systemPhaseType !in ALLOWED_PHASES) {
            throw InvalidPhaseException(
                "현재 참가 신청을 받지 않습니다. 현재 페이즈: ${systemPhase?.systemPhaseType ?: "없음"}",
            )
        }

        // 2. 학번 값 객체 생성 및 검증
        val studentNumber = StudentNumber(command.studentNumber)

        // 3. 학번 중복 확인 (PENDING 또는 APPROVED 상태만 중복으로 간주)
        val duplicateStatuses = listOf(ApplicationStatus.PENDING, ApplicationStatus.APPROVED)
        if (loadApplicationPort.existsByStudentNumberAndStatusIn(studentNumber, duplicateStatuses)) {
            throw DuplicateStudentNumberException("이미 등록된 학번입니다: ${studentNumber.value}")
        }

        // 4. 신청 엔티티 생성 및 검증
        val now = Instant.now(clock)
        val application =
            Application.create(
                studentNumber = studentNumber,
                name = command.name,
                appliedAt = now,
            )

        // 5. 저장
        val saved = saveApplicationPort.save(application)

        // 6. 결과 반환
        return ApplicationResult(
            studentNumber = saved.studentNumber.value,
            name = saved.name,
            status = saved.status,
            appliedAt = saved.appliedAt,
        )
    }

    @Transactional
    override fun approveApplication(command: ApproveApplicationCommand): ProcessApplicationResult {
        // 1. 시스템 페이즈 확인
        validatePhase()

        // 2. 신청 조회
        val studentNumber = StudentNumber(command.studentNumber)
        val application = findApplication(studentNumber)

        // 3. 승인 처리
        val now = Instant.now(clock)
        val approved =
            try {
                application.approve(now)
            } catch (e: IllegalArgumentException) {
                throw AlreadyProcessedException(e.message ?: "이미 처리된 신청입니다")
            }

        // 4. 저장
        val saved = saveApplicationPort.save(approved)

        // 5. 이메일 발송
        val recipientEmail = "${saved.studentNumber.value}@ksa.hs.kr"
        sendEmailPort.sendApprovalNotification(
            recipientEmail = recipientEmail,
            studentNumber = saved.studentNumber.value,
            name = saved.name,
        )

        // 6. 결과 반환
        return ProcessApplicationResult(
            studentNumber = saved.studentNumber.value,
            name = saved.name,
            status = saved.status,
            appliedAt = saved.appliedAt,
            processedAt = saved.processedAt!!,
            emailPreview =
                ProcessApplicationResult.EmailPreview(
                    recipientEmail = recipientEmail,
                    notificationType = "APPROVAL",
                    message = "승인 알림 이메일이 $recipientEmail 로 발송됩니다.",
                ),
        )
    }

    @Transactional
    override fun rejectApplication(command: RejectApplicationCommand): ProcessApplicationResult {
        // 1. 시스템 페이즈 확인
        validatePhase()

        // 2. 신청 조회
        val studentNumber = StudentNumber(command.studentNumber)
        val application = findApplication(studentNumber)

        // 3. 거절 처리
        val now = Instant.now(clock)
        val rejected =
            try {
                application.reject(command.rejectionReason, now)
            } catch (e: IllegalArgumentException) {
                throw AlreadyProcessedException(e.message ?: "이미 처리된 신청입니다")
            }

        // 4. 저장
        val saved = saveApplicationPort.save(rejected)

        // 5. 이메일 발송
        val recipientEmail = "${saved.studentNumber.value}@ksa.hs.kr"
        sendEmailPort.sendRejectionNotification(
            recipientEmail = recipientEmail,
            studentNumber = saved.studentNumber.value,
            name = saved.name,
            rejectionReason = saved.rejectionReason,
        )

        // 6. 결과 반환
        return ProcessApplicationResult(
            studentNumber = saved.studentNumber.value,
            name = saved.name,
            status = saved.status,
            rejectionReason = saved.rejectionReason,
            appliedAt = saved.appliedAt,
            processedAt = saved.processedAt!!,
            emailPreview =
                ProcessApplicationResult.EmailPreview(
                    recipientEmail = recipientEmail,
                    notificationType = "REJECTION",
                    message = "거절 알림 이메일이 $recipientEmail 로 발송됩니다.",
                ),
        )
    }

    private fun validatePhase() {
        val systemPhase = loadSystemPhasePort.load()
        if (systemPhase == null || systemPhase.systemPhaseType !in ALLOWED_PHASES) {
            throw InvalidPhaseException(
                "현재 신청 처리를 할 수 없습니다. 현재 페이즈: ${systemPhase?.systemPhaseType ?: "없음"}",
            )
        }
    }

    private fun findApplication(studentNumber: StudentNumber): Application =
        loadApplicationPort.findByStudentNumber(studentNumber)
            ?: throw ApplicationNotFoundException("해당 학번의 신청을 찾을 수 없습니다: ${studentNumber.value}")

    companion object {
        private val ALLOWED_PHASES = listOf(SystemPhaseType.RECRUIT, SystemPhaseType.TRANSLATE)
    }
}
