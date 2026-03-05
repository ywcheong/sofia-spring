package ywcheong.sofia.application.service.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ywcheong.sofia.application.port.inbound.application.ApplyParticipationUseCase
import ywcheong.sofia.application.port.inbound.application.dto.ApplicationResult
import ywcheong.sofia.application.port.inbound.application.dto.ApplyParticipationCommand
import ywcheong.sofia.application.port.outbound.application.LoadApplicationPort
import ywcheong.sofia.application.port.outbound.application.SaveApplicationPort
import ywcheong.sofia.application.port.outbound.systemphase.LoadSystemPhasePort
import ywcheong.sofia.domain.application.entity.Application
import ywcheong.sofia.domain.application.exception.DuplicateStudentNumberException
import ywcheong.sofia.domain.application.exception.InvalidPhaseException
import ywcheong.sofia.domain.application.value.StudentNumber
import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import java.time.Clock
import java.time.Instant

@Service
class ApplicationService(
    private val loadApplicationPort: LoadApplicationPort,
    private val saveApplicationPort: SaveApplicationPort,
    private val loadSystemPhasePort: LoadSystemPhasePort,
    private val clock: Clock,
) : ApplyParticipationUseCase {
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

        // 3. 학번 중복 확인
        if (loadApplicationPort.existsByStudentNumber(studentNumber)) {
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

    companion object {
        private val ALLOWED_PHASES = listOf(SystemPhaseType.RECRUIT, SystemPhaseType.TRANSLATE)
    }
}
