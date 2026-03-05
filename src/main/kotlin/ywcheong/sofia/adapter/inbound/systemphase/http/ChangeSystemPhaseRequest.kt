package ywcheong.sofia.adapter.inbound.systemphase.dto

import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType

data class ChangeSystemPhaseRequest(
    val systemPhaseType: SystemPhaseType,
)
