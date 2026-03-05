package ywcheong.sofia.domain.systemphase.exception

import ywcheong.sofia.domain.common.exceptions.BusinessException

class InvalidPhaseTransitionException(
    currentPhase: String,
    targetPhase: String,
) : BusinessException(
        "INVALID_PHASE_TRANSITION",
        "페이즈는 순차적으로만 전환할 수 있습니다. 현재: $currentPhase, 요청: $targetPhase",
    )
