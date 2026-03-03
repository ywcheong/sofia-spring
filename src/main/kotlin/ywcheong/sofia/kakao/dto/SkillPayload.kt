package ywcheong.sofia.kakao.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillPayload(
    val userRequest: UserRequest? = null,
    val action: Action? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRequest(
    val utterance: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Action(
    val name: String? = null,
)
