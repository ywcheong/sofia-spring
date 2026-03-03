package ywcheong.sofia.kakao

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.StreamUtils
import org.springframework.web.context.WebApplicationContext
import ywcheong.sofia.kakao.filter.KakaoSkillSecretFilter
import java.nio.charset.StandardCharsets

@SpringBootTest(
    properties = [
        "KAKAO_SKILL_SECRET=test",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.orm.jpa.autoconfigure.HibernateJpaAutoConfiguration",
    ],
)
class KakaoSkillSecretFilterTest(
    private val webApplicationContext: WebApplicationContext,
) {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun `missing secret header returns forbidden`() {
        mockMvc.perform(
            post("/kakao/skill/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loadFixture()),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `valid secret header returns skill response`() {
        mockMvc.perform(
            post("/kakao/skill/apply")
                .header(KakaoSkillSecretFilter.HEADER_NAME, "test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loadFixture()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.version").value("2.0"))
            .andExpect(jsonPath("$.template.outputs[0].simpleText.text").isNotEmpty)
    }

    private fun loadFixture(): String {
        val resource = ClassPathResource("fixtures/kakao/skill-payload-apply.json")
        resource.inputStream.use { return StreamUtils.copyToString(it, StandardCharsets.UTF_8) }
    }
}
