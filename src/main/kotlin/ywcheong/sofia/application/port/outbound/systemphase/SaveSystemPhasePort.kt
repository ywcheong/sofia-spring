package ywcheong.sofia.application.port.outbound.systemphase

import ywcheong.sofia.domain.systemphase.entity.SystemPhase

interface SaveSystemPhasePort {
    fun save(systemPhase: SystemPhase): SystemPhase
}
