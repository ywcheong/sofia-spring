package ywcheong.sofia.application.port.outbound

import ywcheong.sofia.domain.entity.SystemPhase

interface SaveSystemPhasePort {
    fun save(systemPhase: SystemPhase): SystemPhase
}
