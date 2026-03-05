package ywcheong.sofia.adapter.inbound.application.http

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ywcheong.sofia.adapter.inbound.application.dto.ProcessApplicationResponse
import ywcheong.sofia.adapter.inbound.application.dto.RejectApplicationRequest
import ywcheong.sofia.application.port.inbound.application.ProcessApplicationUseCase
import ywcheong.sofia.application.port.inbound.application.dto.ApproveApplicationCommand
import ywcheong.sofia.application.port.inbound.application.dto.RejectApplicationCommand

@RestController
@RequestMapping("/admin/api/applications")
class ApplicationAdminController(
    private val processApplicationUseCase: ProcessApplicationUseCase,
) {
    @PostMapping("/{studentNumber}/approve")
    fun approveApplication(
        @PathVariable studentNumber: String,
    ): ProcessApplicationResponse {
        val command = ApproveApplicationCommand(studentNumber = studentNumber)
        val result = processApplicationUseCase.approveApplication(command)
        return ProcessApplicationResponse(
            studentNumber = result.studentNumber,
            name = result.name,
            status = result.status,
            appliedAt = result.appliedAt,
            processedAt = result.processedAt,
            emailPreview =
                ProcessApplicationResponse.EmailPreview(
                    recipientEmail = result.emailPreview.recipientEmail,
                    notificationType = result.emailPreview.notificationType,
                    message = result.emailPreview.message,
                ),
        )
    }

    @PostMapping("/{studentNumber}/reject")
    fun rejectApplication(
        @PathVariable studentNumber: String,
        @RequestBody request: RejectApplicationRequest,
    ): ProcessApplicationResponse {
        val command =
            RejectApplicationCommand(
                studentNumber = studentNumber,
                rejectionReason = request.rejectionReason,
            )
        val result = processApplicationUseCase.rejectApplication(command)
        return ProcessApplicationResponse(
            studentNumber = result.studentNumber,
            name = result.name,
            status = result.status,
            rejectionReason = result.rejectionReason,
            appliedAt = result.appliedAt,
            processedAt = result.processedAt,
            emailPreview =
                ProcessApplicationResponse.EmailPreview(
                    recipientEmail = result.emailPreview.recipientEmail,
                    notificationType = result.emailPreview.notificationType,
                    message = result.emailPreview.message,
                ),
        )
    }
}
