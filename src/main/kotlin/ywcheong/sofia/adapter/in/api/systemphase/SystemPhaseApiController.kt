@file:Suppress("ktlint:standard:package-name")

package ywcheong.sofia.adapter.`in`.api.systemphase

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ywcheong.sofia.adapter.`in`.api.systemphase.dto.ChangeSystemPhaseRequest
import ywcheong.sofia.adapter.`in`.api.systemphase.dto.SystemPhaseResponse
import ywcheong.sofia.application.port.`in`.ChangeSystemPhaseUseCase
import ywcheong.sofia.application.port.`in`.GetSystemPhaseUseCase
import ywcheong.sofia.application.port.`in`.command.ChangeSystemPhaseCommand
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
            phaseType = result.phaseType,
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
                phaseType = request.phaseType,
                startDate = now,
            )
        val result = changeSystemPhaseUseCase.changeSystemPhase(command)
        return SystemPhaseResponse(
            phaseType = result.phaseType,
            startDate = result.startDate,
        )
    }
}
