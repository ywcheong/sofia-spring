package ywcheong.sofia.adapter.outbound.dictionary.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import ywcheong.sofia.adapter.common.jpa.BaseTimeJpaEntity
import java.util.UUID

@Entity
@Table(name = "dictionary_entries")
class DictionaryJpaEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Column(name = "korean_term", nullable = false, length = 200)
    val koreanTerm: String,
    @Column(name = "english_term", nullable = false, length = 200)
    val englishTerm: String,
    @Column(name = "korean_term_lower", nullable = false, length = 200)
    val koreanTermLower: String,
    @Column(name = "english_term_lower", nullable = false, length = 200)
    val englishTermLower: String,
) : BaseTimeJpaEntity()
