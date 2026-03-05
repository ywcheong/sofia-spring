package ywcheong.sofia.domain.application.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class ApplicationNotFoundException(
    message: String,
) : BusinessException("APPLICATION_NOT_FOUND", message)
