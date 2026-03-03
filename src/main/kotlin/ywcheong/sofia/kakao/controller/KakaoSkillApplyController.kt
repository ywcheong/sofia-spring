package ywcheong.sofia.kakao.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import ywcheong.sofia.kakao.dto.SkillPayload
import ywcheong.sofia.kakao.dto.SkillResponse
import ywcheong.sofia.kakao.filter.KakaoSkillSecretFilter

@RestController
@RequestMapping("/kakao/skill")
class KakaoSkillApplyController {
    @Value("\${KAKAO_SKILL_SECRET:}")
    private lateinit var kakaoSkillSecret: String

    @PostMapping("/apply")
    fun apply(
        @RequestHeader(KakaoSkillSecretFilter.HEADER_NAME, required = false) providedSecret: String?,
        @RequestBody payload: SkillPayload,
    ): SkillResponse {
        if (providedSecret != kakaoSkillSecret) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        val utterance = payload.userRequest?.utterance?.takeIf { it.isNotBlank() } ?: "요청이 접수되었습니다."
        return SkillResponse.simpleText("apply received: $utterance")
    }
}

@Configuration
class KakaoSkillFilterConfiguration {
    @Bean
    fun kakaoSkillSecretFilterRegistration(
        filter: KakaoSkillSecretFilter,
    ): FilterRegistrationBean<KakaoSkillSecretFilter> {
        val registration = FilterRegistrationBean(filter)
        registration.addUrlPatterns("/kakao/skill/*")
        registration.order = 0
        return registration
    }
}
