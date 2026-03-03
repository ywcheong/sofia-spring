@file:Suppress("ktlint:standard:package-name")

package ywcheong.sofia.application.port.`in`.command

import ywcheong.sofia.domain.enums.PhaseType
import java.time.Instant

data class ChangeSystemPhaseCommand(
    val phaseType: PhaseType,
    val startDate: Instant,
)
