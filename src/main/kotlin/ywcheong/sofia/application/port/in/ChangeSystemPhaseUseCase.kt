@file:Suppress("ktlint:standard:package-name")

package ywcheong.sofia.application.port.`in`

import ywcheong.sofia.application.port.`in`.command.ChangeSystemPhaseCommand
import ywcheong.sofia.application.port.`in`.result.SystemPhaseResult

interface ChangeSystemPhaseUseCase {
    fun changeSystemPhase(command: ChangeSystemPhaseCommand): SystemPhaseResult
}
