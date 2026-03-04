
package ywcheong.sofia.application.port.inbound

import ywcheong.sofia.application.port.inbound.result.SystemPhaseResult

interface GetSystemPhaseUseCase {
    fun getSystemPhase(): SystemPhaseResult
}
