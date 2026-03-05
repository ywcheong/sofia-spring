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

    // ==================== 승인/거절 테스트 ====================

    @Test
    @DisplayName("신청 승인 - PENDING 신청을 승인하면 APPROVED 상태가 된다")
    fun `POST approve changes status to APPROVED`() {
        // given: RECRUIT 페이즈로 설정하고 신청 생성
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
                    .content("""{"studentNumber":"25-100","name":"홍길동"}"""),
            ).andExpect(status().isOk)

        // when: 승인
        mockMvc
            .perform(
                post("/admin/api/applications/25-100/approve")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.studentNumber").value("25-100"))
            .andExpect(jsonPath("$.name").value("홍길동"))
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.appliedAt").isString)
            .andExpect(jsonPath("$.processedAt").isString)
            .andExpect(jsonPath("$.emailPreview.recipientEmail").value("25-100@ksa.hs.kr"))
            .andExpect(jsonPath("$.emailPreview.notificationType").value("APPROVAL"))

        // then: DB 상태 확인
        val entity = applicationJpaRepository.findByStudentNumber("25-100")!!
        assertThat(entity.status.name).isEqualTo("APPROVED")
        assertThat(entity.processedAt).isNotNull
    }

    @Test
    @DisplayName("신청 거절 - PENDING 신청을 거절하면 REJECTED 상태가 된다")
    fun `POST reject changes status to REJECTED`() {
        // given: RECRUIT 페이즈로 설정하고 신청 생성
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
                    .content("""{"studentNumber":"25-101","name":"김철수"}"""),
            ).andExpect(status().isOk)

        // when: 거절 (사유 포함)
        mockMvc
            .perform(
                post("/admin/api/applications/25-101/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"rejectionReason":"정원 초과"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.studentNumber").value("25-101"))
            .andExpect(jsonPath("$.name").value("김철수"))
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.rejectionReason").value("정원 초과"))
            .andExpect(jsonPath("$.emailPreview.notificationType").value("REJECTION"))

        // then: DB 상태 확인
        val entity = applicationJpaRepository.findByStudentNumber("25-101")!!
        assertThat(entity.status.name).isEqualTo("REJECTED")
        assertThat(entity.rejectionReason).isEqualTo("정원 초과")
        assertThat(entity.processedAt).isNotNull
    }

    @Test
    @DisplayName("신청 거절 - 사유 없이 거절할 수 있다")
    fun `POST reject without reason`() {
        // given: RECRUIT 페이즈로 설정하고 신청 생성
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
                    .content("""{"studentNumber":"25-102","name":"이영희"}"""),
            ).andExpect(status().isOk)

        // when: 거절 (사유 없음)
        mockMvc
            .perform(
                post("/admin/api/applications/25-102/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.rejectionReason").doesNotExist())
    }

    @Test
    @DisplayName("신청 승인 - 이미 처리된 신청은 다시 승인할 수 없다")
    fun `POST approve rejects already processed application`() {
        // given: RECRUIT 페이즈로 설정하고 신청 생성 후 승인
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
                    .content("""{"studentNumber":"25-103","name":"박민수"}"""),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/admin/api/applications/25-103/approve")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)

        // when: 다시 승인 시도
        mockMvc
            .perform(
                post("/admin/api/applications/25-103/approve")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isConflict)
    }

    @Test
    @DisplayName("신청 거절 - 이미 처리된 신청은 다시 거절할 수 없다")
    fun `POST reject rejects already processed application`() {
        // given: RECRUIT 페이즈로 설정하고 신청 생성 후 거절
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
                    .content("""{"studentNumber":"25-104","name":"최영희"}"""),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/admin/api/applications/25-104/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"rejectionReason":"테스트"}"""),
            ).andExpect(status().isOk)

        // when: 다시 거절 시도
        mockMvc
            .perform(
                post("/admin/api/applications/25-104/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"rejectionReason":"다시 시도"}"""),
            ).andExpect(status().isConflict)
    }

    @Test
    @DisplayName("신청 승인 - 존재하지 않는 신청은 404 Not Found")
    fun `POST approve returns 404 for non-existent application`() {
        // given: RECRUIT 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)

        // when & then: 존재하지 않는 학번 승인 시도
        mockMvc
            .perform(
                post("/admin/api/applications/99-999/approve")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("신청 승인 - INACTIVE 페이즈에서는 승인 불가하다")
    fun `POST approve rejects in INACTIVE phase`() {
        // given: INACTIVE 페이즈로 설정하고 신청 생성 후 페이즈 변경
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
                    .content("""{"studentNumber":"25-105","name":"홍길동"}"""),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"INACTIVE"}"""),
            ).andExpect(status().isOk)

        // when & then: 승인 시도
        mockMvc
            .perform(
                post("/admin/api/applications/25-105/approve")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("신청 승인 - TRANSLATE 페이즈에서도 승인 가능하다")
    fun `POST approve allowed in TRANSLATE phase`() {
        // given: TRANSLATE 페이즈로 설정
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"TRANSLATE"}"""),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"25-106","name":"홍길동"}"""),
            ).andExpect(status().isOk)

        // when: 승인
        mockMvc
            .perform(
                post("/admin/api/applications/25-106/approve")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APPROVED"))
    }
}
