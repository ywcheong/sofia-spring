@file:Suppress("ktlint:standard:package-name")

package ywcheong.sofia.adapter.`in`.api.systemphase.dto

import ywcheong.sofia.domain.enums.PhaseType
import java.time.Instant

data class SystemPhaseResponse(
    val phaseType: PhaseType,
    val startDate: Instant,
)
