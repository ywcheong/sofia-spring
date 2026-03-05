package ywcheong.sofia.domain.systemphase.exception

import ywcheong.sofia.domain.exception.SofiaException

class InvalidPhaseTransitionException(
    currentPhase: String,
    targetPhase: String,
) : SofiaException("페이즈는 순차적으로만 전환할 수 있습니다. 현재: $currentPhase, 요청: $targetPhase")
