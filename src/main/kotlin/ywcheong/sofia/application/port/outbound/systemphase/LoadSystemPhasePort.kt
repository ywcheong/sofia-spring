package ywcheong.sofia.application.port.outbound.systemphase

import ywcheong.sofia.domain.systemphase.entity.SystemPhase

interface LoadSystemPhasePort {
    fun load(): SystemPhase?
}
