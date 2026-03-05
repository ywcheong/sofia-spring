package ywcheong.sofia.adapter.inbound.dictionary.http.dto

import java.time.Instant
import java.util.UUID

data class DictionaryResponse(
    val entries: List<Entry>,
) {
    data class Entry(
        val id: UUID,
        val koreanTerm: String,
        val englishTerm: String,
        val createdAt: Instant,
        val updatedAt: Instant,
    )
}
