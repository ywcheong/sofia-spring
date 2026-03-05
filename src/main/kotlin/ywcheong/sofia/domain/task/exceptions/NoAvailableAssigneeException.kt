package ywcheong.sofia.domain.task.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class NoAvailableAssigneeException(
    message: String,
) : BusinessException("NO_AVAILABLE_ASSIGNEE", message)
