package ywcheong.sofia.domain.application.entity

import ywcheong.sofia.domain.application.enums.ApplicationStatus
import ywcheong.sofia.domain.application.exceptions.InvalidNameLengthException
import ywcheong.sofia.domain.application.value.StudentNumber
import java.time.Instant

data class Application(
    val studentNumber: StudentNumber,
    val name: String,
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val appliedAt: Instant,
    val rejectionReason: String? = null,
    val processedAt: Instant? = null,
) {
    init {
        validate()
    }

    fun validate() {
        if (name.length < MIN_NAME_LENGTH || name.length > MAX_NAME_LENGTH) {
            throw InvalidNameLengthException(
                "이름은 ${MIN_NAME_LENGTH}~${MAX_NAME_LENGTH}글자여야 합니다. 입력값: $name (${name.length}글자)",
            )
        }
    }

    fun approve(processedAt: Instant): Application {
        require(status == ApplicationStatus.PENDING) { "이미 처리된 신청입니다" }
        return copy(
            status = ApplicationStatus.APPROVED,
            processedAt = processedAt,
        )
    }

    fun reject(
        rejectionReason: String?,
        processedAt: Instant,
    ): Application {
        require(status == ApplicationStatus.PENDING) { "이미 처리된 신청입니다" }
        return copy(
            status = ApplicationStatus.REJECTED,
            rejectionReason = rejectionReason,
            processedAt = processedAt,
        )
    }

    companion object {
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 4

        fun create(
            studentNumber: StudentNumber,
            name: String,
            appliedAt: Instant,
        ): Application =
            Application(
                studentNumber = studentNumber,
                name = name,
                status = ApplicationStatus.PENDING,
                appliedAt = appliedAt,
            )
    }
}
