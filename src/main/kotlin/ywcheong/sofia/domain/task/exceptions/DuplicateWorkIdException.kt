package ywcheong.sofia.domain.task.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class DuplicateWorkIdException(
    message: String,
) : BusinessException("DUPLICATE_WORK_ID", message)
