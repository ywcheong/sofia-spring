package ywcheong.sofia.domain.application.exception

import ywcheong.sofia.domain.exception.SofiaException

class InvalidStudentNumberFormatException(
    message: String,
) : SofiaException("INVALID_STUDENT_NUMBER_FORMAT", message)
