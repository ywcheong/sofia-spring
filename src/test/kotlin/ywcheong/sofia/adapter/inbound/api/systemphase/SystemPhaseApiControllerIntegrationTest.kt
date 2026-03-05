package ywcheong.sofia.adapter.inbound.api.systemphase

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ywcheong.sofia.IntegrationTestSupport
import ywcheong.sofia.adapter.outbound.systemphase.jpa.SystemPhaseJpaRepository

@SpringBootTest
@AutoConfigureMockMvc
class SystemPhaseApiControllerIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var systemPhaseJpaRepository: SystemPhaseJpaRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        systemPhaseJpaRepository.deleteAll()
    }

    @Test
    @DisplayName("시스템 페이즈 조회 - 데이터가 없으면 기본값(INACTIVE)을 반환한다")
    fun `GET system-phase returns default when not exists`() {
        mockMvc
            .perform(get("/admin/api/system-phase"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.systemPhaseType").value("INACTIVE"))
            .andExpect(jsonPath("$.startDate").value("1970-01-01T00:00:00Z"))
    }

    @Test
    @DisplayName("시스템 페이즈 변경 - 생성 및 업데이트가 정상 동작한다")
    fun `PUT system-phase creates and updates phase`() {
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.systemPhaseType").value("RECRUIT"))

        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"TRANSLATE"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.systemPhaseType").value("TRANSLATE"))

        assertThat(systemPhaseJpaRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("시스템 페이즈 변경 - startDate가 현재 시각으로 설정된다")
    fun `PUT system-phase populates startDate`() {
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"SETTLE"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.startDate").isString)
    }

    @Test
    @DisplayName("JPA Auditing - createdAt/updatedAt가 자동으로 채워진다")
    fun `timestamps are populated`() {
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)

        val entity = systemPhaseJpaRepository.findById(1L).orElseThrow()
        assertThat(entity.createdAt).isNotNull
        assertThat(entity.updatedAt).isNotNull
    }

    @Test
    @DisplayName("Envers - 감사 테이블이 존재한다")
    fun `Envers audit table exists`() {
        val count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_phase_aud", Long::class.java)
        assertThat(count).isNotNull
    }
}
