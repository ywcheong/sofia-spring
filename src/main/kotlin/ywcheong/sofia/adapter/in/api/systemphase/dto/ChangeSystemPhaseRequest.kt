@file:Suppress("ktlint:standard:package-name")

package ywcheong.sofia.adapter.`in`.api.systemphase.dto

import ywcheong.sofia.domain.enums.PhaseType

data class ChangeSystemPhaseRequest(
    val phaseType: PhaseType,
)
