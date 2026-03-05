package ywcheong.sofia.domain.application.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class AlreadyProcessedException(
    message: String,
) : BusinessException("ALREADY_PROCESSED", message)
