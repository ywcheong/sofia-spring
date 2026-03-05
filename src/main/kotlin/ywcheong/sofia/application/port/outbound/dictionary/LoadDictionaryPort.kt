package ywcheong.sofia.application.port.outbound.dictionary

import ywcheong.sofia.domain.dictionary.entity.DictionaryEntry

interface LoadDictionaryPort {
    fun loadAll(): List<DictionaryEntry>

    fun searchByKeyword(keyword: String): List<DictionaryEntry>
}
