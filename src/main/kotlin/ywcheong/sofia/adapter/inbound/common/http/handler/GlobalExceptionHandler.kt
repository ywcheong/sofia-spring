package ywcheong.sofia.adapter.inbound.common.http.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import ywcheong.sofia.domain.common.exceptions.BusinessException
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = KotlinLogging.logger { }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        exception: BusinessException,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.info { "Business exception occurred: $exception" }

        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message).apply {
            type = URI.create(SWAGGER_URL)
            title = exception.errorCode
            instance = URI.create(request.requestURI)
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.error { "!!! INTERNAL SERVER ERROR OCCURED !!! : $exception" }

        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR).apply {
            type = URI.create(SWAGGER_URL)
            instance = URI.create(request.requestURI)
        }
    }

    companion object {
        private const val SWAGGER_URL = "todo" // todo change as property injection
    }
}
