package ywcheong.sofia.adapter.inbound.api.task

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
import ywcheong.sofia.adapter.outbound.task.jpa.TaskJpaRepository

@SpringBootTest
@AutoConfigureMockMvc
class TaskAdminControllerIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var taskJpaRepository: TaskJpaRepository

    @Autowired
    private lateinit var applicationJpaRepository: ApplicationJpaRepository

    @Autowired
    private lateinit var systemPhaseJpaRepository: SystemPhaseJpaRepository

    @BeforeEach
    fun setUp() {
        taskJpaRepository.deleteAll()
        applicationJpaRepository.deleteAll()
        systemPhaseJpaRepository.deleteAll()
    }

    // ==================== 페이즈 설정 헬퍼 ====================

    private fun setRecruitPhase() {
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"RECRUIT"}"""),
            ).andExpect(status().isOk)
    }

    private fun setTranslatePhase() {
        // RECRUIT -> TRANSLATE
        mockMvc
            .perform(
                put("/admin/api/system-phase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"systemPhaseType":"TRANSLATE"}"""),
            ).andExpect(status().isOk)
    }

    private fun createApplication(
        studentNumber: String,
        name: String,
    ) {
        mockMvc
            .perform(
                post("/kakao/api/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"studentNumber":"$studentNumber","name":"$name"}"""),
            ).andExpect(status().isOk)
    }

    private fun approveApplication(studentNumber: String) {
        mockMvc
            .perform(
                post("/admin/api/applications/$studentNumber/approve")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
    }

    private fun setResting(
        studentNumber: String,
        isResting: Boolean,
    ) {
        val entity = applicationJpaRepository.findByStudentNumber(studentNumber)!!
        entity.isResting = isResting
        applicationJpaRepository.save(entity)
    }

    private fun setEmailSubscribed(
        studentNumber: String,
        isEmailSubscribed: Boolean,
    ) {
        val entity = applicationJpaRepository.findByStudentNumber(studentNumber)!!
        entity.isEmailSubscribed = isEmailSubscribed
        applicationJpaRepository.save(entity)
    }

    // ==================== 과제 생성 테스트 ====================

    @Test
    @DisplayName("과제 생성 - 번역 페이즈에서 자동 배정으로 과제를 생성할 수 있다")
    fun `POST tasks creates task with auto assignment in TRANSLATE phase`() {
        // given: TRANSLATE 페이즈로 설정하고 승인된 번역버디 생성
        setRecruitPhase()
        createApplication("25-001", "홍길동")
        approveApplication("25-001")
        setTranslatePhase()

        // when: 자동 배정으로 과제 생성
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"12345","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.taskId").isString)
            .andExpect(jsonPath("$.taskType").value("GAONNURI"))
            .andExpect(jsonPath("$.workId").value("12345"))
            .andExpect(jsonPath("$.assigneeStudentNumber").value("25-001"))
            .andExpect(jsonPath("$.assigneeName").value("홍길동"))
            .andExpect(jsonPath("$.status").value("ASSIGNED"))
            .andExpect(jsonPath("$.assignedAt").isString)
            .andExpect(jsonPath("$.emailPreview.recipientEmail").value("25-001@ksa.hs.kr"))
            .andExpect(jsonPath("$.emailPreview.notificationType").value("TASK_ASSIGNMENT"))

        // then: DB에 저장되었는지 확인
        assertThat(taskJpaRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("과제 생성 - 번역 페이즈에서 수동 배정으로 과제를 생성할 수 있다")
    fun `POST tasks creates task with manual assignment in TRANSLATE phase`() {
        // given: TRANSLATE 페이즈로 설정하고 승인된 번역버디 생성
        setRecruitPhase()
        createApplication("25-002", "김철수")
        approveApplication("25-002")
        setTranslatePhase()

        // when: 수동 배정으로 과제 생성
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{"taskType":"EXTERNAL","workId":"과제-001","assignmentMethod":"MANUAL","manualAssigneeStudentNumber":"25-002"}""",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.taskId").isString)
            .andExpect(jsonPath("$.taskType").value("EXTERNAL"))
            .andExpect(jsonPath("$.workId").value("과제-001"))
            .andExpect(jsonPath("$.assigneeStudentNumber").value("25-002"))
            .andExpect(jsonPath("$.assigneeName").value("김철수"))
            .andExpect(jsonPath("$.status").value("ASSIGNED"))

        // then: DB에 저장되었는지 확인
        assertThat(taskJpaRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("과제 생성 - 번역 페이즈가 아닐 때 INVALID_TASK_PHASE 에러를 반환한다")
    fun `POST tasks returns INVALID_TASK_PHASE when not in TRANSLATE phase`() {
        // given: RECRUIT 페이즈로 설정 (TRANSLATE가 아님)
        setRecruitPhase()
        createApplication("25-003", "이영희")
        approveApplication("25-003")

        // when & then: 과제 생성 시도
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"99999","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("INVALID_TASK_PHASE"))

        // then: 과제가 생성되지 않음
        assertThat(taskJpaRepository.count()).isEqualTo(0)
    }

    @Test
    @DisplayName("과제 생성 - 중복된 Work ID로 과제 생성 시 DUPLICATE_WORK_ID 에러를 반환한다")
    fun `POST tasks returns DUPLICATE_WORK_ID for duplicate work ID`() {
        // given: TRANSLATE 페이즈로 설정하고 승인된 번역버디 생성
        setRecruitPhase()
        createApplication("25-004", "박민수")
        approveApplication("25-004")
        setTranslatePhase()

        // 첫 번째 과제 생성
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"WORK-001","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isOk)

        // when & then: 같은 Work ID로 다시 과제 생성 시도
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"WORK-001","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("DUPLICATE_WORK_ID"))

        // then: 여전히 1개만 존재
        assertThat(taskJpaRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("과제 생성 - 휴식 중인 번역버디에게 수동 배정 시 ASSIGNEE_IS_RESTING 에러를 반환한다")
    fun `POST tasks returns ASSIGNEE_IS_RESTING when assigning to resting buddy`() {
        // given: TRANSLATE 페이즈로 설정하고 휴식 중인 번역버디 생성
        setRecruitPhase()
        createApplication("25-005", "최영희")
        approveApplication("25-005")
        setResting("25-005", true)
        setTranslatePhase()

        // when & then: 휴식 중인 번역버디에게 수동 배정 시도
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{"taskType":"GAONNURI","workId":"WORK-002","assignmentMethod":"MANUAL","manualAssigneeStudentNumber":"25-005"}""",
                    ),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("ASSIGNEE_IS_RESTING"))

        // then: 과제가 생성되지 않음
        assertThat(taskJpaRepository.count()).isEqualTo(0)
    }

    @Test
    @DisplayName("과제 생성 - 자동 배정 가능한 번역버디가 없을 때 NO_AVAILABLE_ASSIGNEE 에러를 반환한다")
    fun `POST tasks returns NO_AVAILABLE_ASSIGNEE when no available buddies`() {
        // given: TRANSLATE 페이즈로 설정하고 승인된 번역버디 없음
        setRecruitPhase()
        setTranslatePhase()

        // when & then: 자동 배정으로 과제 생성 시도
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"WORK-003","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("NO_AVAILABLE_ASSIGNEE"))

        // then: 과제가 생성되지 않음
        assertThat(taskJpaRepository.count()).isEqualTo(0)
    }

    @Test
    @DisplayName("과제 생성 - 모든 번역버디가 휴식 중일 때 자동 배정 실패")
    fun `POST tasks returns NO_AVAILABLE_ASSIGNEE when all buddies are resting`() {
        // given: TRANSLATE 페이즈로 설정하고 모든 번역버디가 휴식 중
        setRecruitPhase()
        createApplication("25-006", "홍길동")
        approveApplication("25-006")
        setResting("25-006", true)
        createApplication("25-007", "김철수")
        approveApplication("25-007")
        setResting("25-007", true)
        setTranslatePhase()

        // when & then: 자동 배정으로 과제 생성 시도
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"WORK-004","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("NO_AVAILABLE_ASSIGNEE"))
    }

    @Test
    @DisplayName("과제 생성 - 존재하지 않는 번역버디에게 수동 배정 시 ASSIGNEE_NOT_FOUND 에러를 반환한다")
    fun `POST tasks returns ASSIGNEE_NOT_FOUND for non-existent buddy`() {
        // given: TRANSLATE 페이즈로 설정
        setRecruitPhase()
        setTranslatePhase()

        // when & then: 존재하지 않는 번역버디에게 수동 배정 시도
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{"taskType":"GAONNURI","workId":"WORK-005","assignmentMethod":"MANUAL","manualAssigneeStudentNumber":"99-999"}""",
                    ),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("ASSIGNEE_NOT_FOUND"))
    }

    @Test
    @DisplayName("과제 생성 - 승인되지 않은 번역버디에게 수동 배정 시 ASSIGNEE_NOT_FOUND 에러를 반환한다")
    fun `POST tasks returns ASSIGNEE_NOT_FOUND for unapproved buddy`() {
        // given: TRANSLATE 페이즈로 설정하고 PENDING 상태의 번역버디 생성
        setRecruitPhase()
        createApplication("25-008", "이민수")
        setTranslatePhase()

        // when & then: 승인되지 않은 번역버디에게 수동 배정 시도
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{"taskType":"GAONNURI","workId":"WORK-006","assignmentMethod":"MANUAL","manualAssigneeStudentNumber":"25-008"}""",
                    ),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("ASSIGNEE_NOT_FOUND"))
    }

    // ==================== 라운드 로빈 테스트 ====================

    @Test
    @DisplayName("자동 배정 - 라운드 로빈 방식으로 순차 할당된다")
    fun `auto assignment uses round robin`() {
        // given: TRANSLATE 페이즈로 설정하고 여러 번역버디 생성
        setRecruitPhase()
        createApplication("25-010", "홍길동")
        approveApplication("25-010")
        createApplication("25-011", "김철수")
        approveApplication("25-011")
        createApplication("25-012", "이영희")
        approveApplication("25-012")
        setTranslatePhase()

        // when & then: 세 번의 과제 생성이 각각 다른 번역버디에게 할당되는지 확인
        // (순서는 UUID 순서이므로 학번 순서와 다를 수 있음)
        val assignedStudentNumbers = mutableSetOf<String>()

        // 첫 번째 과제
        val result1 =
            mockMvc
                .perform(
                    post("/admin/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"taskType":"GAONNURI","workId":"RR-001","assignmentMethod":"AUTO"}"""),
                ).andExpect(status().isOk)
                .andReturn()
        val studentNumber1 = result1.response.contentAsString
        assignedStudentNumbers.add(extractStudentNumber(studentNumber1))

        // 두 번째 과제는 그 다음 번역버디에게
        val result2 =
            mockMvc
                .perform(
                    post("/admin/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"taskType":"GAONNURI","workId":"RR-002","assignmentMethod":"AUTO"}"""),
                ).andExpect(status().isOk)
                .andReturn()
        val studentNumber2 = result2.response.contentAsString
        assignedStudentNumbers.add(extractStudentNumber(studentNumber2))

        // 세 번째 과제는 마지막 번역버디에게
        val result3 =
            mockMvc
                .perform(
                    post("/admin/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"taskType":"GAONNURI","workId":"RR-003","assignmentMethod":"AUTO"}"""),
                ).andExpect(status().isOk)
                .andReturn()
        val studentNumber3 = result3.response.contentAsString
        assignedStudentNumbers.add(extractStudentNumber(studentNumber3))

        // then: 3개의 과제가 모두 다른 번역버디에게 할당됨
        assertThat(assignedStudentNumbers).hasSize(3)
    }

    private fun extractStudentNumber(json: String): String {
        val regex = """"assigneeStudentNumber":"(\d{2}-\d{3})"""".toRegex()
        return regex.find(json)?.groupValues?.get(1) ?: ""
    }

    @Test
    @DisplayName("자동 배정 - 휴식 중인 번역버디는 건너뛴다")
    fun `auto assignment skips resting buddies`() {
        // given: TRANSLATE 페이즈로 설정하고 일부 번역버디는 휴식 중
        setRecruitPhase()
        createApplication("25-020", "홍길동")
        approveApplication("25-020")
        setResting("25-020", true)
        createApplication("25-021", "김철수")
        approveApplication("25-021")
        setTranslatePhase()

        // when & then: 휴식 중인 번역버디를 건너뛰고 할당
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"SKIP-001","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.assigneeStudentNumber").value("25-021"))
    }

    // ==================== 자동 배정 미리보기 테스트 ====================

    @Test
    @DisplayName("자동 배정 미리보기 - 다음 배정 대상을 확인할 수 있다")
    fun `GET auto-assignment preview shows next assignee`() {
        // given: TRANSLATE 페이즈로 설정하고 승인된 번역버디 생성
        setRecruitPhase()
        createApplication("25-030", "홍길동")
        approveApplication("25-030")
        createApplication("25-031", "김철수")
        approveApplication("25-031")
        setTranslatePhase()

        // when & then: 미리보기 조회 (순서는 UUID 순서이므로 학번 순서와 다를 수 있음)
        mockMvc
            .perform(get("/admin/api/tasks/auto-assignment/preview"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nextAssigneeStudentNumber").isString)
            .andExpect(jsonPath("$.nextAssigneeName").isString)
            .andExpect(jsonPath("$.availableAssigneeCount").value(2))
    }

    @Test
    @DisplayName("자동 배정 미리보기 - 번역 페이즈가 아닐 때 INVALID_TASK_PHASE 에러를 반환한다")
    fun `GET auto-assignment preview returns error when not in TRANSLATE phase`() {
        // given: RECRUIT 페이즈 (TRANSLATE가 아님)
        setRecruitPhase()

        // when & then: 미리보기 조회
        mockMvc
            .perform(get("/admin/api/tasks/auto-assignment/preview"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("INVALID_TASK_PHASE"))
    }

    @Test
    @DisplayName("자동 배정 미리보기 - 배정 가능한 번역버디가 없으면 적절한 메시지를 반환한다")
    fun `GET auto-assignment preview shows message when no available buddies`() {
        // given: TRANSLATE 페이즈로 설정하고 승인된 번역버디 없음
        setRecruitPhase()
        setTranslatePhase()

        // when & then: 미리보기 조회
        mockMvc
            .perform(get("/admin/api/tasks/auto-assignment/preview"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nextAssigneeStudentNumber").doesNotExist())
            .andExpect(jsonPath("$.nextAssigneeName").doesNotExist())
            .andExpect(jsonPath("$.availableAssigneeCount").value(0))
            .andExpect(jsonPath("$.message").value("배정 가능한 번역버디가 없습니다."))
    }

    // ==================== 배정 가능한 번역버디 목록 테스트 ====================

    @Test
    @DisplayName("배정 가능한 번역버디 목록 - 승인된 번역버디 목록을 조회할 수 있다")
    fun `GET available-assignees returns list of approved buddies`() {
        // given: 승인된 번역버디 생성
        setRecruitPhase()
        createApplication("25-040", "홍길동")
        approveApplication("25-040")
        createApplication("25-041", "김철수")
        approveApplication("25-041")
        setResting("25-041", true)
        setTranslatePhase()

        // when & then: 목록 조회 (순서는 보장되지 않음)
        mockMvc
            .perform(get("/admin/api/tasks/available-assignees"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.assignees").isArray)
            .andExpect(jsonPath("$.assignees.length()").value(2))
    }

    @Test
    @DisplayName("배정 가능한 번역버디 목록 - 승인된 번역버디가 없으면 빈 목록을 반환한다")
    fun `GET available-assignees returns empty list when no approved buddies`() {
        // given: 승인된 번역버디 없음
        setRecruitPhase()
        setTranslatePhase()

        // when & then: 목록 조회
        mockMvc
            .perform(get("/admin/api/tasks/available-assignees"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.assignees").isArray)
            .andExpect(jsonPath("$.assignees.length()").value(0))
    }

    // ==================== 이메일 수신 설정 테스트 ====================

    @Test
    @DisplayName("과제 생성 - 이메일 수신 거부 시 이메일 미리보기에 거부 메시지를 표시한다")
    fun `POST tasks shows unsubscribe message when email is unsubscribed`() {
        // given: TRANSLATE 페이즈로 설정하고 이메일 수신 거부 번역버디 생성
        setRecruitPhase()
        createApplication("25-050", "홍길동")
        approveApplication("25-050")
        setEmailSubscribed("25-050", false)
        setTranslatePhase()

        // when & then: 과제 생성
        mockMvc
            .perform(
                post("/admin/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"taskType":"GAONNURI","workId":"EMAIL-001","assignmentMethod":"AUTO"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.emailPreview.recipientEmail").value("25-050@ksa.hs.kr"))
            .andExpect(jsonPath("$.emailPreview.message").value("과제 배정 알림 이메일이 25-050@ksa.hs.kr 로 발송되지 않았습니다. (이메일 수신 거부 상태)"))
    }
}
