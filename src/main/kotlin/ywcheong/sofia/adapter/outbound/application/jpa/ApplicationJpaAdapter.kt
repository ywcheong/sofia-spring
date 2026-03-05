package ywcheong.sofia.adapter.outbound.application.jpa

import org.springframework.stereotype.Repository
import ywcheong.sofia.application.common.service.UuidGenerateService
import ywcheong.sofia.application.port.outbound.application.LoadApplicationPort
import ywcheong.sofia.application.port.outbound.application.SaveApplicationPort
import ywcheong.sofia.domain.application.entity.Application
import ywcheong.sofia.domain.application.value.StudentNumber

@Repository
class ApplicationJpaAdapter(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val uuidGenerateService: UuidGenerateService,
) : LoadApplicationPort,
    SaveApplicationPort {
    override fun existsByStudentNumber(studentNumber: StudentNumber): Boolean =
        applicationJpaRepository.existsByStudentNumber(studentNumber.value)

    override fun findByStudentNumber(studentNumber: StudentNumber): Application? =
        applicationJpaRepository.findByStudentNumber(studentNumber.value)?.toDomain()

    override fun save(application: Application): Application {
        val existingEntity = applicationJpaRepository.findByStudentNumber(application.studentNumber.value)

        val jpaEntity =
            if (existingEntity != null) {
                // Update existing
                ApplicationJpaEntity(
                    id = existingEntity.id,
                    studentNumber = application.studentNumber.value,
                    name = application.name,
                    status = application.status,
                    appliedAt = application.appliedAt,
                    rejectionReason = application.rejectionReason,
                    processedAt = application.processedAt,
                )
            } else {
                // Create new
                ApplicationJpaEntity(
                    id = uuidGenerateService.generate(),
                    studentNumber = application.studentNumber.value,
                    name = application.name,
                    status = application.status,
                    appliedAt = application.appliedAt,
                    rejectionReason = application.rejectionReason,
                    processedAt = application.processedAt,
                )
            }
        applicationJpaRepository.save(jpaEntity)
        return application
    }

    private fun ApplicationJpaEntity.toDomain(): Application =
        Application(
            studentNumber = StudentNumber(studentNumber),
            name = name,
            status = status,
            appliedAt = appliedAt,
            rejectionReason = rejectionReason,
            processedAt = processedAt,
        )
}
