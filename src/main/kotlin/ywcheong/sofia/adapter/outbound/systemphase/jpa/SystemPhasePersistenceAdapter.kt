package ywcheong.sofia.adapter.outbound.systemphase.jpa

import org.springframework.stereotype.Repository
import ywcheong.sofia.application.port.outbound.systemphase.LoadSystemPhasePort
import ywcheong.sofia.application.port.outbound.systemphase.SaveSystemPhasePort
import ywcheong.sofia.domain.systemphase.entity.SystemPhase

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
                    systemPhaseType = systemPhase.systemPhaseType,
                    startDate = systemPhase.startDate,
                )
            }.apply {
                systemPhaseType = systemPhase.systemPhaseType
                startDate = systemPhase.startDate
            }.let { systemPhaseJpaRepository.save(it) }
            .toDomain()

    private fun SystemPhaseJpaEntity.toDomain(): SystemPhase =
        SystemPhase(
            systemPhaseType = systemPhaseType,
            startDate = startDate,
        )
}
