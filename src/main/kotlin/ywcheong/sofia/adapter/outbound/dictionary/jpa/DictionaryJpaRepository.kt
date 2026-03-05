package ywcheong.sofia.adapter.outbound.dictionary.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DictionaryJpaRepository : JpaRepository<DictionaryJpaEntity, UUID> {
    @Query(
        """
        SELECT d FROM DictionaryJpaEntity d
        WHERE d.koreanTermLower LIKE CONCAT('%', :keywordLower, '%')
           OR d.englishTermLower LIKE CONCAT('%', :keywordLower, '%')
        """,
    )
    fun searchByKeywordLower(
        @Param("keywordLower") keywordLower: String,
    ): List<DictionaryJpaEntity>
}
