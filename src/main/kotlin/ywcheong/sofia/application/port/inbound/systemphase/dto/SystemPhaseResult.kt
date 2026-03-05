package ywcheong.sofia.application.port.inbound.systemphase.dto

import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import java.time.Instant

data class SystemPhaseResult(
    val systemPhaseType: SystemPhaseType,
    val startDate: Instant,
)
