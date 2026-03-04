package ywcheong.sofia.adapter.outbound.persistence.systemphase

import org.springframework.stereotype.Repository
import ywcheong.sofia.application.port.outbound.LoadSystemPhasePort
import ywcheong.sofia.application.port.outbound.SaveSystemPhasePort
import ywcheong.sofia.domain.entity.SystemPhase

@Repository
class SystemPhasePersistenceAdapter(
    private val systemPhaseJpaRepository: SystemPhaseJpaRepository,
) : LoadSystemPhasePort,
    SaveSystemPhasePort {
    override fun load(): SystemPhase? =
        systemPhaseJpaRepository
            .findById(1L)
            .orElse(null)
            ?.toDomain()

    override fun save(systemPhase: SystemPhase): SystemPhase =
        systemPhaseJpaRepository
            .findById(1L)
            .orElseGet {
                SystemPhaseJpaEntity(
                    id = 1L,
                    phaseType = systemPhase.phaseType,
                    startDate = systemPhase.startDate,
                )
            }.apply {
                phaseType = systemPhase.phaseType
                startDate = systemPhase.startDate
            }.let { systemPhaseJpaRepository.save(it) }
            .toDomain()

    private fun SystemPhaseJpaEntity.toDomain(): SystemPhase =
        SystemPhase(
            phaseType = phaseType,
            startDate = startDate,
        )
}
