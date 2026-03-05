package ywcheong.sofia.adapter.inbound.systemphase.dto

import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import java.time.Instant

data class SystemPhaseResponse(
    val systemPhaseType: SystemPhaseType,
    val startDate: Instant,
)
