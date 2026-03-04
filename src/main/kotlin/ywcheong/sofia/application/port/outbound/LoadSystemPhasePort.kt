package ywcheong.sofia.application.port.outbound

import ywcheong.sofia.domain.entity.SystemPhase

interface LoadSystemPhasePort {
    fun load(): SystemPhase?
}
