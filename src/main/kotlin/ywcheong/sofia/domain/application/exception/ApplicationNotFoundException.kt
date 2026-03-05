package ywcheong.sofia.domain.application.exception

import ywcheong.sofia.domain.exception.SofiaException

class ApplicationNotFoundException(
    message: String,
) : SofiaException(message)
