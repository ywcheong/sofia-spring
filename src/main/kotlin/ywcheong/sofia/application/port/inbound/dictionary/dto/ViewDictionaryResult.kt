package ywcheong.sofia.application.port.inbound.dictionary.dto

import java.time.Instant
import java.util.UUID

data class ViewDictionaryResult(
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
