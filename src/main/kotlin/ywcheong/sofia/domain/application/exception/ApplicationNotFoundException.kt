package ywcheong.sofia.domain.application.exception

import ywcheong.sofia.domain.exception.SofiaException

class ApplicationNotFoundException(
    message: String,
) : SofiaException("APPLICATION_NOT_FOUND", message)
