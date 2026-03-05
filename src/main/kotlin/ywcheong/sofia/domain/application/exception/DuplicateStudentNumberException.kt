package ywcheong.sofia.domain.application.exception

import ywcheong.sofia.domain.exception.SofiaException

class DuplicateStudentNumberException(
    message: String,
) : SofiaException(message)
