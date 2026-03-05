package ywcheong.sofia.domain.application.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class InvalidPhaseException(
    message: String,
) : BusinessException("INVALID_PHASE", message)
