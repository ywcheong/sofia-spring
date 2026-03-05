package ywcheong.sofia.domain.application.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class InvalidStudentNumberFormatException(
    message: String,
) : BusinessException("INVALID_STUDENT_NUMBER_FORMAT", message)
