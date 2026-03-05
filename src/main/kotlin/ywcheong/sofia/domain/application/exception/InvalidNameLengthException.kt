package ywcheong.sofia.domain.application.exception

import ywcheong.sofia.domain.exception.SofiaException

class InvalidNameLengthException(
    message: String,
) : SofiaException(message)
