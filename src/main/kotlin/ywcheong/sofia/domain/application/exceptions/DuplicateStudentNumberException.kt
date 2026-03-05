package ywcheong.sofia.domain.application.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class DuplicateStudentNumberException(
    message: String,
) : BusinessException("DUPLICATE_STUDENT_NUMBER", message)
