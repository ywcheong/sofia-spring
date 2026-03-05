package ywcheong.sofia.domain.common.exceptions

abstract class BusinessException(
    val errorCode: String,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
