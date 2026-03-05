package ywcheong.sofia.adapter.inbound.dictionary.http

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ywcheong.sofia.adapter.inbound.dictionary.http.dto.DictionaryResponse
import ywcheong.sofia.application.port.inbound.dictionary.ViewDictionaryUseCase
import ywcheong.sofia.application.port.inbound.dictionary.dto.ViewDictionaryCommand

@RestController
@RequestMapping("/kakao/api/dictionary")
class DictionaryApiController(
    private val viewDictionaryUseCase: ViewDictionaryUseCase,
) {
    @GetMapping
    fun queryDictionary(
        @RequestParam(required = false) keyword: String?,
    ): DictionaryResponse {
        val command = ViewDictionaryCommand(keyword = keyword)
        val result = viewDictionaryUseCase.query(command)
        return DictionaryResponse(
            entries =
                result.entries.map { entry ->
                    DictionaryResponse.Entry(
                        id = entry.id,
                        koreanTerm = entry.koreanTerm,
                        englishTerm = entry.englishTerm,
                        createdAt = entry.createdAt,
                        updatedAt = entry.updatedAt,
                    )
                },
        )
    }
}
