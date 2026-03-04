package ywcheong.sofia.application.port.inbound

import ywcheong.sofia.application.port.inbound.command.ChangeSystemPhaseCommand
import ywcheong.sofia.application.port.inbound.result.SystemPhaseResult

interface ChangeSystemPhaseUseCase {
    fun changeSystemPhase(command: ChangeSystemPhaseCommand): SystemPhaseResult
}
