package ywcheong.sofia.kakao.dto

data class SkillResponse(
    val version: String = "2.0",
    val template: SkillTemplate,
) {
    companion object {
        fun simpleText(text: String): SkillResponse {
            return SkillResponse(
                template = SkillTemplate(
                    outputs = listOf(SkillOutput(simpleText = SimpleText(text = text))),
                ),
            )
        }
    }
}

data class SkillTemplate(
    val outputs: List<SkillOutput>,
)

data class SkillOutput(
    val simpleText: SimpleText,
)

data class SimpleText(
    val text: String,
)
