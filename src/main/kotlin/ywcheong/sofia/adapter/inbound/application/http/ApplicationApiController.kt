package ywcheong.sofia.adapter.inbound.application.http

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ywcheong.sofia.adapter.inbound.application.dto.ApplicationResponse
import ywcheong.sofia.adapter.inbound.application.dto.ApplyParticipationRequest
import ywcheong.sofia.application.port.inbound.application.ApplyParticipationUseCase
import ywcheong.sofia.application.port.inbound.application.dto.ApplyParticipationCommand

@RestController
@RequestMapping("/kakao/api/applications")
class ApplicationApiController(
    private val applyParticipationUseCase: ApplyParticipationUseCase,
) {
    @PostMapping
    fun applyParticipation(
        @RequestBody request: ApplyParticipationRequest,
    ): ApplicationResponse {
        val command =
            ApplyParticipationCommand(
                studentNumber = request.studentNumber,
                name = request.name,
            )
        val result = applyParticipationUseCase.applyParticipation(command)
        return ApplicationResponse(
            studentNumber = result.studentNumber,
            name = result.name,
            status = result.status,
            appliedAt = result.appliedAt,
        )
    }
}
