package ywcheong.sofia.domain.application.value

import ywcheong.sofia.domain.application.exception.InvalidStudentNumberFormatException

data class StudentNumber(
    val value: String,
) {
    init {
        validate()
    }

    fun validate() {
        val pattern = Regex("^\\d{2}-\\d{3}$")
        if (!pattern.matches(value)) {
            throw InvalidStudentNumberFormatException(
                "학번 형식이 올바르지 않습니다. XX-XXX 형식이어야 합니다. 입력값: $value",
            )
        }
    }

    override fun toString(): String = value
}
