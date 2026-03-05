package ywcheong.sofia.application.port.inbound.application

import ywcheong.sofia.application.port.inbound.application.dto.ApproveApplicationCommand
import ywcheong.sofia.application.port.inbound.application.dto.ProcessApplicationResult
import ywcheong.sofia.application.port.inbound.application.dto.RejectApplicationCommand

interface ProcessApplicationUseCase {
    fun approveApplication(command: ApproveApplicationCommand): ProcessApplicationResult

    fun rejectApplication(command: RejectApplicationCommand): ProcessApplicationResult
}
