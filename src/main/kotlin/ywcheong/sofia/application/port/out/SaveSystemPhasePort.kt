package ywcheong.sofia.application.port.out

import ywcheong.sofia.domain.entity.SystemPhase

interface SaveSystemPhasePort {
    fun save(systemPhase: SystemPhase): SystemPhase
}
