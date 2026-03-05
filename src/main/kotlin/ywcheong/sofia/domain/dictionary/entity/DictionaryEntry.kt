package ywcheong.sofia.domain.dictionary.entity

import ywcheong.sofia.domain.dictionary.exceptions.InvalidTermLengthException
import java.time.Instant
import java.util.UUID

data class DictionaryEntry(
    val id: UUID,
    val koreanTerm: String,
    val englishTerm: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        validate()
    }

    fun validate() {
        if (koreanTerm.isEmpty() || koreanTerm.length > MAX_TERM_LENGTH) {
            throw InvalidTermLengthException(
                "한국어 용어는 1~${MAX_TERM_LENGTH}자여야 합니다. 입력값: $koreanTerm (${koreanTerm.length}자)",
            )
        }
        if (englishTerm.isEmpty() || englishTerm.length > MAX_TERM_LENGTH) {
            throw InvalidTermLengthException(
                "영어 용어는 1~${MAX_TERM_LENGTH}자여야 합니다. 입력값: $englishTerm (${englishTerm.length}자)",
            )
        }
    }

    fun update(
        koreanTerm: String,
        englishTerm: String,
        updatedAt: Instant,
    ): DictionaryEntry =
        copy(
            koreanTerm = koreanTerm,
            englishTerm = englishTerm,
            updatedAt = updatedAt,
        )

    companion object {
        private const val MAX_TERM_LENGTH = 200

        fun create(
            id: UUID,
            koreanTerm: String,
            englishTerm: String,
            createdAt: Instant,
        ): DictionaryEntry =
            DictionaryEntry(
                id = id,
                koreanTerm = koreanTerm,
                englishTerm = englishTerm,
                createdAt = createdAt,
                updatedAt = createdAt,
            )
    }
}
