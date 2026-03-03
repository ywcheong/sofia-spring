package ywcheong.sofia.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ywcheong.sofia.application.port.`in`.ChangeSystemPhaseUseCase
import ywcheong.sofia.application.port.`in`.GetSystemPhaseUseCase
import ywcheong.sofia.application.port.`in`.command.ChangeSystemPhaseCommand
import ywcheong.sofia.application.port.`in`.result.SystemPhaseResult
import ywcheong.sofia.application.port.out.LoadSystemPhasePort
import ywcheong.sofia.application.port.out.SaveSystemPhasePort
import ywcheong.sofia.domain.entity.SystemPhase
import ywcheong.sofia.domain.enums.PhaseType
import java.time.Instant

@Service
class SystemPhaseService(
    private val loadSystemPhasePort: LoadSystemPhasePort,
    private val saveSystemPhasePort: SaveSystemPhasePort,
) : GetSystemPhaseUseCase,
    ChangeSystemPhaseUseCase {
    @Transactional(readOnly = true)
    override fun getSystemPhase(): SystemPhaseResult {
        val systemPhase = loadSystemPhasePort.load() ?: createDefault()
        return SystemPhaseResult(
            phaseType = systemPhase.phaseType,
            startDate = systemPhase.startDate,
        )
    }

    @Transactional
    override fun changeSystemPhase(command: ChangeSystemPhaseCommand): SystemPhaseResult {
        val systemPhase =
            SystemPhase(
                phaseType = command.phaseType,
                startDate = command.startDate,
            )
        val saved = saveSystemPhasePort.save(systemPhase)
        return SystemPhaseResult(
            phaseType = saved.phaseType,
            startDate = saved.startDate,
        )
    }

    private fun createDefault(): SystemPhase =
        SystemPhase(
            phaseType = PhaseType.INACTIVE,
            startDate = Instant.EPOCH,
        )
}
