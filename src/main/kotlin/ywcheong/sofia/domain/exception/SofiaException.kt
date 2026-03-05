package ywcheong.sofia.domain.exception

import ywcheong.sofia.domain.common.exceptions.BusinessException

open class SofiaException(
    errorCode: String,
    message: String,
) : BusinessException(errorCode, message)
