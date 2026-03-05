package ywcheong.sofia.adapter.outbound.dictionary.jpa

import org.springframework.stereotype.Repository
import ywcheong.sofia.application.port.outbound.dictionary.LoadDictionaryPort
import ywcheong.sofia.domain.dictionary.entity.DictionaryEntry

@Repository
class DictionaryJpaAdapter(
    private val dictionaryJpaRepository: DictionaryJpaRepository,
) : LoadDictionaryPort {
    override fun loadAll(): List<DictionaryEntry> = dictionaryJpaRepository.findAll().map { it.toDomain() }

    override fun searchByKeyword(keyword: String): List<DictionaryEntry> =
        dictionaryJpaRepository.searchByKeywordLower(keyword.lowercase()).map { it.toDomain() }

    private fun DictionaryJpaEntity.toDomain(): DictionaryEntry =
        DictionaryEntry(
            id = id,
            koreanTerm = koreanTerm,
            englishTerm = englishTerm,
            createdAt = createdAt!!,
            updatedAt = updatedAt!!,
        )
}
