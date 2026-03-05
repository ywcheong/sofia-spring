package ywcheong.sofia.adapter.outbound.application.jpa

import org.springframework.stereotype.Repository
import ywcheong.sofia.application.common.service.UuidGenerateService
import ywcheong.sofia.application.port.outbound.application.LoadApplicationPort
import ywcheong.sofia.application.port.outbound.application.SaveApplicationPort
import ywcheong.sofia.domain.application.entity.Application
import ywcheong.sofia.domain.application.enums.ApplicationStatus
import ywcheong.sofia.domain.application.value.StudentNumber
import java.util.UUID

@Repository
class ApplicationJpaAdapter(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val uuidGenerateService: UuidGenerateService,
) : LoadApplicationPort,
    SaveApplicationPort {
    override fun existsByStudentNumber(studentNumber: StudentNumber): Boolean =
        applicationJpaRepository.existsByStudentNumber(studentNumber.value)

    override fun existsByStudentNumberAndStatusIn(
        studentNumber: StudentNumber,
        statuses: List<ApplicationStatus>,
    ): Boolean = applicationJpaRepository.existsByStudentNumberAndStatusIn(studentNumber.value, statuses)

    override fun findByStudentNumber(studentNumber: StudentNumber): Application? =
        applicationJpaRepository.findByStudentNumber(studentNumber.value)?.toDomain()

    override fun findById(id: UUID): Application? = applicationJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByStatus(status: ApplicationStatus): List<Application> =
        applicationJpaRepository.findByStatus(status).map { it.toDomain() }

    override fun findByStatusAndIsResting(
        status: ApplicationStatus,
        isResting: Boolean,
    ): List<Application> = applicationJpaRepository.findByStatusAndIsResting(status, isResting).map { it.toDomain() }

    override fun save(application: Application): Application {
        val existingEntity = applicationJpaRepository.findByStudentNumber(application.studentNumber.value)

        val id = application.id ?: existingEntity?.id ?: uuidGenerateService.generate()
        val jpaEntity =
            ApplicationJpaEntity(
                id = id,
                studentNumber = application.studentNumber.value,
                name = application.name,
                status = application.status,
                appliedAt = application.appliedAt,
                rejectionReason = application.rejectionReason,
                processedAt = application.processedAt,
                isResting = application.isResting,
                isEmailSubscribed = application.isEmailSubscribed,
                totalCharacterCount = application.totalCharacterCount,
                adjustedCharacterCount = application.adjustedCharacterCount,
                warningCount = application.warningCount,
                lastAssignedAt = application.lastAssignedAt,
            )
        applicationJpaRepository.save(jpaEntity)
        return application.copy(id = id)
    }

    private fun ApplicationJpaEntity.toDomain(): Application =
        Application(
            id = id,
            studentNumber = StudentNumber(studentNumber),
            name = name,
            status = status,
            appliedAt = appliedAt,
            rejectionReason = rejectionReason,
            processedAt = processedAt,
            isResting = isResting,
            isEmailSubscribed = isEmailSubscribed,
            totalCharacterCount = totalCharacterCount,
            adjustedCharacterCount = adjustedCharacterCount,
            warningCount = warningCount,
            lastAssignedAt = lastAssignedAt,
        )
}
