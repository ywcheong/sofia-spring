package ywcheong.sofia.adapter.inbound.systemphase.http

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ywcheong.sofia.adapter.inbound.systemphase.dto.ChangeSystemPhaseRequest
import ywcheong.sofia.adapter.inbound.systemphase.dto.SystemPhaseResponse
import ywcheong.sofia.application.port.inbound.systemphase.ChangeSystemPhaseUseCase
import ywcheong.sofia.application.port.inbound.systemphase.GetSystemPhaseUseCase
import ywcheong.sofia.application.port.inbound.systemphase.dto.ChangeSystemPhaseCommand
import java.time.Clock
import java.time.Instant

@RestController
@RequestMapping("/admin/api/system-phase")
class SystemPhaseApiController(
    private val getSystemPhaseUseCase: GetSystemPhaseUseCase,
    private val changeSystemPhaseUseCase: ChangeSystemPhaseUseCase,
    private val clock: Clock,
) {
    @GetMapping
    fun getSystemPhase(): SystemPhaseResponse {
        val result = getSystemPhaseUseCase.getSystemPhase()
        return SystemPhaseResponse(
            systemPhaseType = result.systemPhaseType,
            startDate = result.startDate,
        )
    }

    @PutMapping
    fun changeSystemPhase(
        @RequestBody request: ChangeSystemPhaseRequest,
    ): SystemPhaseResponse {
        val now = Instant.now(clock)
        val command =
            ChangeSystemPhaseCommand(
                systemPhaseType = request.systemPhaseType,
                startDate = now,
            )
        val result = changeSystemPhaseUseCase.changeSystemPhase(command)
        return SystemPhaseResponse(
            systemPhaseType = result.systemPhaseType,
            startDate = result.startDate,
        )
    }
}
