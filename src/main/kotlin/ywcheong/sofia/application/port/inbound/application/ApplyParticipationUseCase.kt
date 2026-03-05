package ywcheong.sofia.application.port.inbound.application

import ywcheong.sofia.application.port.inbound.application.dto.ApplicationResult
import ywcheong.sofia.application.port.inbound.application.dto.ApplyParticipationCommand

interface ApplyParticipationUseCase {
    fun applyParticipation(command: ApplyParticipationCommand): ApplicationResult
}
