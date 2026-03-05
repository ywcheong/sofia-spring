package ywcheong.sofia.application.port.inbound.systemphase

import ywcheong.sofia.application.port.inbound.systemphase.dto.ChangeSystemPhaseCommand
import ywcheong.sofia.application.port.inbound.systemphase.dto.SystemPhaseResult

interface ChangeSystemPhaseUseCase {
    fun changeSystemPhase(command: ChangeSystemPhaseCommand): SystemPhaseResult
}
