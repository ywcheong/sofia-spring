package ywcheong.sofia.application.port.inbound.dictionary

import ywcheong.sofia.application.port.inbound.dictionary.dto.ViewDictionaryCommand
import ywcheong.sofia.application.port.inbound.dictionary.dto.ViewDictionaryResult

interface ViewDictionaryUseCase {
    fun query(command: ViewDictionaryCommand): ViewDictionaryResult
}
