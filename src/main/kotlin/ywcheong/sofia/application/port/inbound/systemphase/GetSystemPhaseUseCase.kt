package ywcheong.sofia.application.port.inbound.systemphase

import ywcheong.sofia.application.port.inbound.systemphase.dto.SystemPhaseResult

interface GetSystemPhaseUseCase {
    fun getSystemPhase(): SystemPhaseResult
}
