package ywcheong.sofia.domain.task.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class InvalidTaskPhaseException(
    message: String,
) : BusinessException("INVALID_TASK_PHASE", message)
