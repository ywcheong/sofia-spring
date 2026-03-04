
package ywcheong.sofia.adapter.inbound.api.systemphase.dto

import ywcheong.sofia.domain.enums.PhaseType

data class ChangeSystemPhaseRequest(
    val phaseType: PhaseType,
)
