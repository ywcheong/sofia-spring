package ywcheong.sofia.application.port.inbound.systemphase.dto

import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import java.time.Instant

data class ChangeSystemPhaseCommand(
    val systemPhaseType: SystemPhaseType,
    val startDate: Instant,
)
