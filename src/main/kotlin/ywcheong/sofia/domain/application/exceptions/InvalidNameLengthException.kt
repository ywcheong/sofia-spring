package ywcheong.sofia.domain.application.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class InvalidNameLengthException(
    message: String,
) : BusinessException("INVALID_NAME_LENGTH", message)
