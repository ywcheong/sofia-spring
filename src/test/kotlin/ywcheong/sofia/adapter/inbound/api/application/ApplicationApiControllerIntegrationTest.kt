package ywcheong.sofia.adapter.inbound.api.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ywcheong.sofia.IntegrationTestSupport
import ywcheong.sofia.adapter.outbound.application.jpa.ApplicationJpaRepository
import ywcheong.sofia.adapter.outbound.systemphase.jpa.SystemPhaseJpaRepository

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationApiControllerIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var applicationJpaRepository: ApplicationJpaRepository

    @Autowired
    private lateinit var systemPhaseJpaRepository: SystemPhaseJpaRepository

    @BeforeEach
    fun setUp() {
        applicationJpaRepository.deleteAll()
        systemPhaseJpaRepository.deleteAll()
    }

    @Test
    @DisplayName("참가 신청 - 정상 신청 시 PENDING 상태로 저장된다")
    fun `POST applications creates pending application`() {
        // given: RECRUIT 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)

        // when: 참가 신청
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-001","name":"홍길동"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.studentNumber").value("25-001"))
            .andExpect(jsonPath("$.name").value("홍길동"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.appliedAt").isString)

        // then: DB에 저장되었는지 확인
        assertThat(applicationJpaRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("참가 신청 - TRANSLATE 페이즈에서도 신청 가능하다")
    fun `POST applications allowed in TRANSLATE phase`() {
        // given: TRANSLATE 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"TRANSLATE"}"""),
            ).andExpect(status().isOk)

        // when: 참가 신청
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-002","name":"김철수"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PENDING"))

        assertThat(applicationJpaRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("참가 신청 - 중복 학번 시 409 Conflict를 반환한다")
    fun `POST applications rejects duplicate student number`() {
        // given: RECRUIT 페이즈로 설정하고 첫 번째 신청
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-003","name":"이영희"}"""),
            ).andExpect(status().isOk)

        // when: 같은 학번으로 다시 신청
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-003","name":"박영수"}"""),
            ).andExpect(status().isConflict)

        // then: 여전히 1개만 존재
        assertThat(applicationJpaRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("참가 신청 - 잘못된 학번 형식 시 400 Bad Request를 반환한다")
    fun `POST applications rejects invalid student number format`() {
        // given: RECRUIT 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)

        // when & then: 잘못된 형식의 학번으로 신청
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25001","name":"최민수"}"""),
            ).andExpect(status().isBadRequest)

        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-0001","name":"최민수"}"""),
            ).andExpect(status().isBadRequest)

        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"ABC-123","name":"최민수"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("참가 신청 - 이름 길이가 2~4글자가 아니면 400 Bad Request를 반환한다")
    fun `POST applications rejects invalid name length`() {
        // given: RECRUIT 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)

        // when & then: 1글자 이름
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-004","name":"김"}"""),
            ).andExpect(status().isBadRequest)

        // when & then: 5글자 이름
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-005","name":"가나다라마"}"""),
            ).andExpect(status().isBadRequest)

        // then: 정상 케이스 (2글자)
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-006","name":"김민"}"""),
            ).andExpect(status().isOk)

        // then: 정상 케이스 (4글자)
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-007","name":"가나다라"}"""),
            ).andExpect(status().isOk)
    }

    @Test
    @DisplayName("참가 신청 - INACTIVE 페이즈에서는 신청 불가하다")
    fun `POST applications rejects in INACTIVE phase`() {
        // given: INACTIVE 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"INACTIVE"}"""),
            ).andExpect(status().isOk)

        // when & then: 신청 시도
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-008","name":"홍길동"}"""),
            ).andExpect(status().isForbidden)

        assertThat(applicationJpaRepository.count()).isEqualTo(0)
    }

    @Test
    @DisplayName("참가 신청 - SETTLE 페이즈에서는 신청 불가하다")
    fun `POST applications rejects in SETTLE phase`() {
        // given: SETTLE 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"SETTLE"}"""),
            ).andExpect(status().isOk)

        // when & then: 신청 시도
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-009","name":"홍길동"}"""),
            ).andExpect(status().isForbidden)

        assertThat(applicationJpaRepository.count()).isEqualTo(0)
    }

    @Test
    @DisplayName("JPA Auditing - createdAt/updatedAt가 자동으로 채워진다")
    fun `timestamps are populated`() {
        // given
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)

        // when
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-010","name":"홍길동"}"""),
            ).andExpect(status().isOk)

        // then
        val entity = applicationJpaRepository.findAll().first()
        assertThat(entity.createdAt).isNotNull
        assertThat(entity.updatedAt).isNotNull
    }
}
