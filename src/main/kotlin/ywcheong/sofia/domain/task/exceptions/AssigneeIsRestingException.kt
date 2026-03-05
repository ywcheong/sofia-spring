package ywcheong.sofia.domain.task.exceptions

import ywcheong.sofia.domain.common.exceptions.BusinessException

class AssigneeIsRestingException(
    message: String,
) : BusinessException("ASSIGNEE_IS_RESTING", message)
