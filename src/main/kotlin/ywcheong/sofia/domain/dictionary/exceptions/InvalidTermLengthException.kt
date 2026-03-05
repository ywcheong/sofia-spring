package ywcheong.sofia.domain.dictionary.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class InvalidTermLengthException(
    message: String,
    cause: Throwable? = null,
) : BusinessException(
        errorCode = "DICTIONARY_INVALID_TERM_LENGTH",
        message = message,
        cause = cause,
    )
