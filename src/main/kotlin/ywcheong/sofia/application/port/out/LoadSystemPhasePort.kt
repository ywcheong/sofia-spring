package ywcheong.sofia.application.port.out

import ywcheong.sofia.domain.entity.SystemPhase

interface LoadSystemPhasePort {
    fun load(): SystemPhase?
}
