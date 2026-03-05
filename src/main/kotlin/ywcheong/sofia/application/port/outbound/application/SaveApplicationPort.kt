package ywcheong.sofia.application.port.outbound.application

import ywcheong.sofia.domain.application.entity.Application

interface SaveApplicationPort {
    fun save(application: Application): Application
}
