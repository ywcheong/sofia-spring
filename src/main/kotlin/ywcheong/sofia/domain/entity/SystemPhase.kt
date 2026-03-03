package ywcheong.sofia.domain.entity

import ywcheong.sofia.domain.enums.PhaseType
import java.time.Instant

data class SystemPhase(
    val phaseType: PhaseType,
    val startDate: Instant,
)
