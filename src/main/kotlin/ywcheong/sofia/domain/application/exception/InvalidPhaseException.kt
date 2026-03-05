package ywcheong.sofia.domain.application.exception

import ywcheong.sofia.domain.exception.SofiaException

class InvalidPhaseException(
    message: String,
) : SofiaException("INVALID_PHASE", message)
