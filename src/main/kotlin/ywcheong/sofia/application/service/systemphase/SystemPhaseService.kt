package ywcheong.sofia.application.service.systemphase

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ywcheong.sofia.application.port.inbound.systemphase.ChangeSystemPhaseUseCase
import ywcheong.sofia.application.port.inbound.systemphase.GetSystemPhaseUseCase
import ywcheong.sofia.application.port.inbound.systemphase.dto.ChangeSystemPhaseCommand
import ywcheong.sofia.application.port.inbound.systemphase.dto.SystemPhaseResult
import ywcheong.sofia.application.port.outbound.systemphase.LoadSystemPhasePort
import ywcheong.sofia.application.port.outbound.systemphase.SaveSystemPhasePort
import ywcheong.sofia.domain.systemphase.entity.SystemPhase
import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
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
            systemPhaseType = systemPhase.systemPhaseType,
            startDate = systemPhase.startDate,
        )
    }

    @Transactional
    override fun changeSystemPhase(command: ChangeSystemPhaseCommand): SystemPhaseResult {
        val systemPhase =
            SystemPhase(
                systemPhaseType = command.systemPhaseType,
                startDate = command.startDate,
            )
        val saved = saveSystemPhasePort.save(systemPhase)
        return SystemPhaseResult(
            systemPhaseType = saved.systemPhaseType,
            startDate = saved.startDate,
        )
    }

    private fun createDefault(): SystemPhase =
        SystemPhase(
            systemPhaseType = SystemPhaseType.INACTIVE,
            startDate = Instant.EPOCH,
        )
}
