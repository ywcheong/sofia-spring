package ywcheong.sofia.domain.dictionary.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class InvalidPhaseForDictionaryException(
    message: String,
    cause: Throwable? = null,
) : BusinessException(
        errorCode = "DICTIONARY_INVALID_PHASE",
        message = message,
        cause = cause,
    )
