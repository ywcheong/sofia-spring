package ywcheong.sofia.application.service.dictionary

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ywcheong.sofia.application.port.inbound.dictionary.ViewDictionaryUseCase
import ywcheong.sofia.application.port.inbound.dictionary.dto.ViewDictionaryCommand
import ywcheong.sofia.application.port.inbound.dictionary.dto.ViewDictionaryResult
import ywcheong.sofia.application.port.outbound.dictionary.LoadDictionaryPort
import ywcheong.sofia.application.port.outbound.systemphase.LoadSystemPhasePort
import ywcheong.sofia.domain.dictionary.exceptions.InvalidPhaseForDictionaryException
import ywcheong.sofia.domain.systemphase.enums.SystemPhaseType

@Service
class DictionaryService(
    private val loadDictionaryPort: LoadDictionaryPort,
    private val loadSystemPhasePort: LoadSystemPhasePort,
) : ViewDictionaryUseCase {
    @Transactional(readOnly = true)
    override fun query(command: ViewDictionaryCommand): ViewDictionaryResult {
        // 1. 시스템 페이즈 확인 (INACTIVE 제외)
        validatePhase()

        // 2. 사전 조회 (키워드 필터링 또는 전체 조회)
        val entries =
            if (command.keyword.isNullOrBlank()) {
                loadDictionaryPort.loadAll()
            } else {
                loadDictionaryPort.searchByKeyword(command.keyword)
            }

        // 3. 결과 반환
        return ViewDictionaryResult(
            entries =
                entries.map { entry ->
                    ViewDictionaryResult.Entry(
                        id = entry.id,
                        koreanTerm = entry.koreanTerm,
                        englishTerm = entry.englishTerm,
                        createdAt = entry.createdAt,
                        updatedAt = entry.updatedAt,
                    )
                },
        )
    }

    private fun validatePhase() {
        val systemPhase = loadSystemPhasePort.load()
        if (systemPhase != null && systemPhase.systemPhaseType == SystemPhaseType.INACTIVE) {
            throw InvalidPhaseForDictionaryException(
                "비활성 페이즈에서는 사전 조회가 불가능합니다.",
            )
        }
    }
}
