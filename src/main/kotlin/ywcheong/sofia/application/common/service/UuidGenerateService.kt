package ywcheong.sofia.application.common.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UuidGenerateService {
    fun generate(): UUID = UUID.randomUUID()
}
