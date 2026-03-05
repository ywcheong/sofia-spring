package ywcheong.sofia.adapter.inbound.common.http.handler

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import ywcheong.sofia.domain.application.exception.AlreadyProcessedException
import ywcheong.sofia.domain.application.exception.ApplicationNotFoundException
import ywcheong.sofia.domain.application.exception.DuplicateStudentNumberException
import ywcheong.sofia.domain.application.exception.InvalidPhaseException
import ywcheong.sofia.domain.exception.SofiaException
import java.net.URI
import java.time.Clock
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler(
    private val clock: Clock,
) : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(SofiaException::class)
    fun handleSofiaException(
        exception: SofiaException,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.error("Sofia exception occurred: ${exception.message}", exception)

        val status = determineHttpStatus(exception)
        val problemDetail =
            ProblemDetail
                .forStatusAndDetail(status, exception.message ?: "Bad Request")
        problemDetail.type = URI.create("https://sofia/errors/sofia-exception") // TODO 변경 필요
        problemDetail.title = "Sofia Business Error"
        problemDetail.instance = URI.create(request.requestURI)
        problemDetail.setProperty("timestamp", Instant.now(clock))

        return problemDetail
    }

    private fun determineHttpStatus(exception: SofiaException): HttpStatus =
        when (exception) {
            is DuplicateStudentNumberException -> HttpStatus.CONFLICT
            is AlreadyProcessedException -> HttpStatus.CONFLICT
            is ApplicationNotFoundException -> HttpStatus.NOT_FOUND
            is InvalidPhaseException -> HttpStatus.FORBIDDEN
            else -> HttpStatus.BAD_REQUEST
        }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.error("Unexpected exception occurred: ${exception.message}", exception)

        val problemDetail =
            ProblemDetail
                .forStatusAndDetail(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred",
                )
        problemDetail.type = URI.create("https://sofia/errors/unexpected") // TODO 변경 필요
        problemDetail.title = "Internal Server Error"
        problemDetail.instance = URI.create(request.requestURI)
        problemDetail.setProperty("timestamp", Instant.now(clock))

        return problemDetail
    }
}
