package ywcheong.sofia.domain.task.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class AssigneeNotFoundException(
    message: String,
) : BusinessException("ASSIGNEE_NOT_FOUND", message)
