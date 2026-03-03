@file:Suppress("ktlint:standard:package-name")

package ywcheong.sofia.application.port.`in`

import ywcheong.sofia.application.port.`in`.result.SystemPhaseResult

interface GetSystemPhaseUseCase {
    fun getSystemPhase(): SystemPhaseResult
}
