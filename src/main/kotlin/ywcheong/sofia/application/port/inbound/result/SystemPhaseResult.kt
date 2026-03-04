
package ywcheong.sofia.application.port.inbound.result

import ywcheong.sofia.domain.enums.PhaseType
import java.time.Instant

data class SystemPhaseResult(
    val phaseType: PhaseType,
    val startDate: Instant,
)
