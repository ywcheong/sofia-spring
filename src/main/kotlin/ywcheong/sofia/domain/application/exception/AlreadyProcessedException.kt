package ywcheong.sofia.domain.application.exception

import ywcheong.sofia.domain.exception.SofiaException

class AlreadyProcessedException(
    message: String,
) : SofiaException(message)
