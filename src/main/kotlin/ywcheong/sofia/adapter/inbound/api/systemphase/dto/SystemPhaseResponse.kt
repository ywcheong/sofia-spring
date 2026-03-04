
package ywcheong.sofia.adapter.inbound.api.systemphase.dto

import ywcheong.sofia.domain.enums.PhaseType
import java.time.Instant

data class SystemPhaseResponse(
    val phaseType: PhaseType,
    val startDate: Instant,
)
