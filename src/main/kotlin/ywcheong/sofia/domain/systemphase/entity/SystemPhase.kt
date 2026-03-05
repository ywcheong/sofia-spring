package ywcheong.sofia.domain.systemphase.entity

import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType
import java.time.Instant

data class SystemPhase(
    val systemPhaseType: SystemPhaseType,
    val startDate: Instant,
) {
    fun canTransitionTo(target: SystemPhaseType): Boolean = systemPhaseType.next == target
}
