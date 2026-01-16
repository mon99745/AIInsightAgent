package com.aiinsightagent.app.controller;

import com.aiinsightagent.app.TestApplication;
import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.repository.ActorRepository;
import com.aiinsightagent.app.repository.PreparedContextRepository;
import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.aiinsightagent.core.model.Context;
import com.aiinsightagent.core.queue.GeminiQueueManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.Models;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ContextController 통합 테스트")
class ContextControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ActorRepository actorRepository;

	@Autowired
	private PreparedContextRepository preparedContextRepository;

	@MockitoBean
	private Client geminiClient;

	@MockitoBean
	private Models geminiModels;

	@MockitoBean
	private GeminiQueueManager geminiQueueManager;

	@MockitoBean
	private GeminiChatAdapter geminiChatAdapter;

	private Context context;
	private Map<String, String> contextData;

	@BeforeEach
	void setUp() {
		// 테스트 데이터 초기화
		preparedContextRepository.deleteAll();
		actorRepository.deleteAll();

		// Context 데이터 생성
		contextData = new HashMap<>();
		contextData.put("averagePace", "6:00");
		contextData.put("totalDistance", "100km");
		contextData.put("runningDays", "30");

		// Context 생성
		context = Context.builder()
				.userId("test-user")
				.category("running_history")
				.data(contextData)
				.build();
	}

	@Nested
	@DisplayName("POST /api/v1/context/save - 전처리 데이터 저장")
	class SaveContextTest {

		@Test
		@DisplayName("성공: 새로운 사용자의 Context 저장")
		void saveContext_Success() throws Exception {
			// given
			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextId").exists())
					.andExpect(jsonPath("$.contextType").value("running_history"))
					.andExpect(jsonPath("$.contextScope").value("ACTOR"))
					.andExpect(jsonPath("$.active").value(true))
					.andExpect(jsonPath("$.confidenceLevel").value("MEDIUM"));

			// DB 검증 - Actor 생성 확인
			Actor savedActor = actorRepository.findByActorKey("test-user").orElseThrow();
			assertThat(savedActor).isNotNull();
			assertThat(savedActor.getActorKey()).isEqualTo("test-user");

			// DB 검증 - PreparedContext 저장 확인
			PreparedContext savedContext = preparedContextRepository.findByActor(savedActor).orElseThrow();
			assertThat(savedContext).isNotNull();
			assertThat(savedContext.getContextType()).isEqualTo("running_history");
			assertThat(savedContext.getActor().getActorKey()).isEqualTo("test-user");
		}

		@Test
		@DisplayName("성공: 기존 Actor에 새로운 Context 저장")
		void saveContext_ExistingActor_Success() throws Exception {
			// given - Actor 미리 생성
			Actor existingActor = Actor.create("existing-user");
			actorRepository.save(existingActor);

			Context newContext = Context.builder()
					.userId("existing-user")
					.category("health_metrics")
					.data(Map.of("heartRate", "170", "bloodPressure", "120/80"))
					.build();

			String requestBody = objectMapper.writeValueAsString(newContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("health_metrics"));

			// DB 검증 - Actor는 기존 것 사용
			assertThat(actorRepository.findAll()).hasSize(1);

			// DB 검증 - Context 새로 저장됨
			PreparedContext savedContext = preparedContextRepository
					.findByActor(existingActor)
					.orElseThrow();
			assertThat(savedContext.getContextType()).isEqualTo("health_metrics");
		}

		@Test
		@DisplayName("성공: 다양한 카테고리의 Context 저장")
		void saveContext_VariousCategories_Success() throws Exception {
			// given
			String[] categories = {"running_history", "health_metrics", "nutrition_log", "sleep_data"};

			for (int i = 0; i < categories.length; i++) {
				Context ctx = Context.builder()
						.userId("user-" + i)
						.category(categories[i])
						.data(Map.of("data", "value-" + i))
						.build();

				// when
				mockMvc.perform(post("/api/v1/context/save")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(ctx)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.contextType").value(categories[i]));
			}

			// then - DB 검증
			assertThat(actorRepository.findAll()).hasSize(categories.length);
			assertThat(preparedContextRepository.findAll()).hasSize(categories.length);
		}

		@Test
		@DisplayName("성공: 빈 데이터로 Context 저장")
		void saveContext_EmptyData_Success() throws Exception {
			// given
			Context emptyDataContext = Context.builder()
					.userId("empty-user")
					.category("empty_category")
					.data(new HashMap<>())
					.build();

			String requestBody = objectMapper.writeValueAsString(emptyDataContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("empty_category"));

			// DB 검증
			Actor actor = actorRepository.findByActorKey("empty-user").orElseThrow();
			PreparedContext savedContext = preparedContextRepository.findByActor(actor).orElseThrow();
			assertThat(savedContext).isNotNull();
		}

		@Test
		@DisplayName("성공: 대량의 데이터로 Context 저장")
		void saveContext_LargeData_Success() throws Exception {
			// given
			Map<String, String> largeData = new HashMap<>();
			for (int i = 0; i < 100; i++) {
				largeData.put("key" + i, "value" + i);
			}

			Context largeContext = Context.builder()
					.userId("large-data-user")
					.category("large_category")
					.data(largeData)
					.build();

			String requestBody = objectMapper.writeValueAsString(largeContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("large_category"));

			// DB 검증
			assertThat(actorRepository.findAll()).hasSize(1);
			assertThat(preparedContextRepository.findAll()).hasSize(1);
		}

		@Test
		@DisplayName("성공: 특수문자가 포함된 데이터로 Context 저장")
		void saveContext_SpecialCharacters_Success() throws Exception {
			// given
			Map<String, String> specialData = new HashMap<>();
			specialData.put("special", "!@#$%^&*()");
			specialData.put("korean", "한글테스트");
			specialData.put("emoji", "😀🎉");

			Context specialContext = Context.builder()
					.userId("special-user")
					.category("special_category")
					.data(specialData)
					.build();

			String requestBody = objectMapper.writeValueAsString(specialContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("special_category"));
		}

		@Test
		@DisplayName("성공: 긴 userId로 Context 저장")
		void saveContext_LongUserId_Success() throws Exception {
			// given
			String longUserId = "a".repeat(100);
			Context longUserIdContext = Context.builder()
					.userId(longUserId)
					.category("test_category")
					.data(Map.of("test", "data"))
					.build();

			String requestBody = objectMapper.writeValueAsString(longUserIdContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk());

			// DB 검증
			Actor actor = actorRepository.findByActorKey(longUserId).orElseThrow();
			assertThat(actor.getActorKey()).hasSize(100);
		}

		@Test
		@DisplayName("실패: 중복된 Context 저장 시도")
		void saveContext_DuplicateContext_ThrowsException() throws Exception {
			// given - Context 먼저 저장
			String requestBody = objectMapper.writeValueAsString(context);
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk());

			// when & then - 동일한 사용자의 Context 재저장 시도
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			// DB 검증 - PreparedContext는 1개만 존재
			assertThat(preparedContextRepository.findAll()).hasSize(1);
		}

		@Test
		@DisplayName("실패: 잘못된 JSON 형식")
		void saveContext_InvalidJson_Returns400() throws Exception {
			// given
			String invalidJson = "{invalid json}";

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(invalidJson))
					.andDo(print())
					.andExpect(status().isBadRequest());

			// DB 검증 - 아무것도 저장되지 않음
			assertThat(actorRepository.findAll()).isEmpty();
			assertThat(preparedContextRepository.findAll()).isEmpty();
		}

		@Test
		@DisplayName("실패: Content-Type 누락")
		void saveContext_NoContentType_Returns415() throws Exception {
			// given
			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isUnsupportedMediaType());
		}
	}

	@Nested
	@DisplayName("POST /api/v1/context/get - 전처리 데이터 조회")
	class GetContextTest {

		@Test
		@DisplayName("성공: 저장된 Context 조회")
		void getContext_Success() throws Exception {
			// given - Context 먼저 저장
			String saveRequestBody = objectMapper.writeValueAsString(context);
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(saveRequestBody))
					.andExpect(status().isOk());

			// when & then
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextId").exists())
					.andExpect(jsonPath("$.contextType").value("running_history"))
					.andExpect(jsonPath("$.contextScope").value("ACTOR"))
					.andExpect(jsonPath("$.active").value(true))
					.andExpect(jsonPath("$.actor.actorKey").value("test-user"));
		}

		@Test
		@DisplayName("성공: 여러 사용자의 Context 개별 조회")
		void getContext_MultipleUsers_Success() throws Exception {
			// given - 여러 사용자 Context 저장
			for (int i = 1; i <= 3; i++) {
				Context ctx = Context.builder()
						.userId("user-" + i)
						.category("category-" + i)
						.data(Map.of("key", "value-" + i))
						.build();

				mockMvc.perform(post("/api/v1/context/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(ctx)));
			}

			// when & then - 각 사용자별로 조회
			for (int i = 1; i <= 3; i++) {
				mockMvc.perform(post("/api/v1/context/get")
								.param("userId", "user-" + i))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.contextType").value("category-" + i))
						.andExpect(jsonPath("$.actor.actorKey").value("user-" + i));
			}
		}

		@Test
		@DisplayName("성공: 저장 직후 즉시 조회")
		void getContext_ImmediatelyAfterSave_Success() throws Exception {
			// given & when
			String requestBody = objectMapper.writeValueAsString(context);
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk());

			// then - 바로 조회
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("running_history"));
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 조회")
		void getContext_ActorNotFound_ThrowsException() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "non-existent-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("실패: Actor는 있지만 Context가 없는 경우")
		void getContext_ContextNotFound_ThrowsException() throws Exception {
			// given - Actor만 생성
			Actor actor = Actor.create("test-user");
			actorRepository.save(actor);

			// when & then
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			// DB 검증
			assertThat(actorRepository.findByActorKey("test-user")).isPresent();
			assertThat(preparedContextRepository.findByActor(actor)).isEmpty();
		}

		@Test
		@DisplayName("실패: userId 파라미터 누락")
		void getContext_MissingUserId_Returns400() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v1/context/get"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/v1/context/update - 전처리 데이터 수정")
	class UpdateContextTest {

		@Test
		@DisplayName("성공: Context 데이터 수정")
		void updateContext_Success() throws Exception {
			// given - Context 먼저 저장
			String saveRequestBody = objectMapper.writeValueAsString(context);
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(saveRequestBody))
					.andExpect(status().isOk());

			// 수정할 데이터 준비
			Map<String, String> updatedData = new HashMap<>();
			updatedData.put("averagePace", "5:30");
			updatedData.put("totalDistance", "150km");
			updatedData.put("runningDays", "45");

			Context updatedContext = Context.builder()
					.userId("test-user")
					.category("updated_running_history")
					.data(updatedData)
					.build();

			String updateRequestBody = objectMapper.writeValueAsString(updatedContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(updateRequestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextId").exists())
					.andExpect(jsonPath("$.contextType").value("updated_running_history"));

			// DB 검증 - Context가 업데이트됨
			Actor actor = actorRepository.findByActorKey("test-user").orElseThrow();
			PreparedContext updatedPreparedContext = preparedContextRepository
					.findByActor(actor)
					.orElseThrow();
			assertThat(updatedPreparedContext.getContextType()).isEqualTo("updated_running_history");

			// DB 검증 - Actor와 Context 개수는 그대로
			assertThat(actorRepository.findAll()).hasSize(1);
			assertThat(preparedContextRepository.findAll()).hasSize(1);
		}

		@Test
		@DisplayName("성공: 카테고리만 변경")
		void updateContext_CategoryOnly_Success() throws Exception {
			// given
			mockMvc.perform(post("/api/v1/context/save")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(context)));

			Context updatedContext = Context.builder()
					.userId("test-user")
					.category("new_category")
					.data(contextData) // 데이터는 동일
					.build();

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(updatedContext)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("new_category"));
		}

		@Test
		@DisplayName("성공: 데이터만 변경")
		void updateContext_DataOnly_Success() throws Exception {
			// given
			mockMvc.perform(post("/api/v1/context/save")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(context)));

			Map<String, String> newData = new HashMap<>();
			newData.put("newKey", "newValue");

			Context updatedContext = Context.builder()
					.userId("test-user")
					.category("running_history") // 카테고리 동일
					.data(newData)
					.build();

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(updatedContext)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("running_history"));
		}

		@Test
		@DisplayName("성공: 여러 번 연속 수정")
		void updateContext_MultipleUpdates_Success() throws Exception {
			// given - 초기 저장
			mockMvc.perform(post("/api/v1/context/save")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(context)));

			// when & then - 3번 연속 수정
			for (int i = 1; i <= 3; i++) {
				Context updateCtx = Context.builder()
						.userId("test-user")
						.category("version-" + i)
						.data(Map.of("version", String.valueOf(i)))
						.build();

				mockMvc.perform(post("/api/v1/context/update")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(updateCtx)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.contextType").value("version-" + i));
			}

			// DB 검증 - 여전히 1개만 존재
			assertThat(preparedContextRepository.findAll()).hasSize(1);

			// 최종 버전 확인
			Actor actor = actorRepository.findByActorKey("test-user").orElseThrow();
			PreparedContext finalContext = preparedContextRepository.findByActor(actor).orElseThrow();
			assertThat(finalContext.getContextType()).isEqualTo("version-3");
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 수정 시도")
		void updateContext_ActorNotFound_ThrowsException() throws Exception {
			// given
			Context updateContext = Context.builder()
					.userId("non-existent-user")
					.category("some_category")
					.data(contextData)
					.build();

			String requestBody = objectMapper.writeValueAsString(updateContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("실패: Actor는 있지만 Context가 없는 경우")
		void updateContext_ContextNotFound_ThrowsException() throws Exception {
			// given - Actor만 생성
			Actor actor = Actor.create("test-user");
			actorRepository.save(actor);

			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}
	}

	@Nested
	@DisplayName("POST /api/v1/context/delete - 전처리 데이터 삭제")
	class DeleteContextTest {

		@Test
		@DisplayName("성공: Context 삭제")
		void deleteContext_Success() throws Exception {
			// given - Context 먼저 저장
			String saveRequestBody = objectMapper.writeValueAsString(context);
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(saveRequestBody))
					.andExpect(status().isOk());

			// when & then
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().isOk());

			// DB 검증 - Context 삭제됨
			Actor actor = actorRepository.findByActorKey("test-user").orElseThrow();
			assertThat(preparedContextRepository.findByActor(actor)).isEmpty();

			// DB 검증 - Actor는 여전히 존재
			assertThat(actorRepository.findByActorKey("test-user")).isPresent();
		}

		@Test
		@DisplayName("성공: 삭제 후 재저장 가능")
		void deleteContext_ThenResave_Success() throws Exception {
			// given - 저장
			mockMvc.perform(post("/api/v1/context/save")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(context)));

			// when - 삭제
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "test-user"))
					.andExpect(status().isOk());

			// then - 재저장 성공
			Context newContext = Context.builder()
					.userId("test-user")
					.category("new_category")
					.data(Map.of("new", "data"))
					.build();

			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(newContext)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("new_category"));
		}

		@Test
		@DisplayName("성공: 여러 사용자 중 특정 사용자만 삭제")
		void deleteContext_SelectiveDelete_Success() throws Exception {
			// given - 3명의 사용자 Context 저장
			for (int i = 1; i <= 3; i++) {
				Context ctx = Context.builder()
						.userId("user-" + i)
						.category("category-" + i)
						.data(Map.of("key", "value"))
						.build();

				mockMvc.perform(post("/api/v1/context/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(ctx)));
			}

			// when - user-2만 삭제
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "user-2"))
					.andExpect(status().isOk());

			// then - user-1, user-3는 여전히 존재
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "user-1"))
					.andExpect(status().isOk());

			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "user-3"))
					.andExpect(status().isOk());

			// user-2는 삭제됨
			Actor user2 = actorRepository.findByActorKey("user-2").orElseThrow();
			assertThat(preparedContextRepository.findByActor(user2)).isEmpty();

			// DB 검증
			assertThat(preparedContextRepository.findAll()).hasSize(2);
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 삭제 시도")
		void deleteContext_ActorNotFound_ThrowsException() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "non-existent-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("실패: Actor는 있지만 Context가 없는 경우")
		void deleteContext_ContextNotFound_ThrowsException() throws Exception {
			// given - Actor만 생성
			Actor actor = Actor.create("test-user");
			actorRepository.save(actor);

			// when & then
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("실패: userId 파라미터 누락")
		void deleteContext_MissingUserId_Returns400() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v1/context/delete"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("전체 라이프사이클 통합 시나리오")
	class IntegrationScenarioTest {

		@Test
		@DisplayName("시나리오: 생성 -> 조회 -> 수정 -> 조회 -> 삭제 -> 조회 실패")
		void fullLifecycle() throws Exception {
			// 1. Save - Context 생성
			String saveRequestBody = objectMapper.writeValueAsString(context);
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(saveRequestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("running_history"));

			// DB 검증 - Save 후
			Actor savedActor = actorRepository.findByActorKey("test-user").orElseThrow();
			PreparedContext savedContext = preparedContextRepository.findByActor(savedActor).orElseThrow();
			assertThat(savedContext.getContextType()).isEqualTo("running_history");

			// 2. Get - Context 조회
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("running_history"));

			// 3. Update - Context 수정
			Map<String, String> updatedData = new HashMap<>();
			updatedData.put("averagePace", "5:30");
			updatedData.put("totalDistance", "150km");

			Context updatedContext = Context.builder()
					.userId("test-user")
					.category("updated_running_history")
					.data(updatedData)
					.build();

			String updateRequestBody = objectMapper.writeValueAsString(updatedContext);
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(updateRequestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("updated_running_history"));

			// DB 검증 - Update 후
			PreparedContext updatedPreparedContext = preparedContextRepository
					.findByActor(savedActor)
					.orElseThrow();
			assertThat(updatedPreparedContext.getContextType()).isEqualTo("updated_running_history");

			// 4. Get - 수정된 Context 조회
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("updated_running_history"));

			// 5. Delete - Context 삭제
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "test-user"))
					.andExpect(status().isOk());

			// DB 검증 - Delete 후
			assertThat(preparedContextRepository.findByActor(savedActor)).isEmpty();

			// 6. Get - 삭제 후 조회 실패
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("시나리오: 여러 사용자의 독립적인 Context 관리")
		void multipleUsersIndependentContexts() throws Exception {
			// given - 3명의 사용자 데이터
			String[] userIds = {"user1", "user2", "user3"};
			String[] categories = {"running", "cycling", "swimming"};

			// when - 각 사용자별 Context 저장
			for (int i = 0; i < userIds.length; i++) {
				Context ctx = Context.builder()
						.userId(userIds[i])
						.category(categories[i])
						.data(Map.of("activity", categories[i]))
						.build();

				mockMvc.perform(post("/api/v1/context/save")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(ctx)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.contextType").value(categories[i]));
			}

			// then - 각 사용자별 독립적으로 조회 가능
			for (int i = 0; i < userIds.length; i++) {
				mockMvc.perform(post("/api/v1/context/get")
								.param("userId", userIds[i]))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.contextType").value(categories[i]))
						.andExpect(jsonPath("$.actor.actorKey").value(userIds[i]));
			}

			// user2만 삭제
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "user2"))
					.andExpect(status().isOk());

			// user1, user3는 여전히 조회 가능
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "user1"))
					.andExpect(status().isOk());

			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "user3"))
					.andExpect(status().isOk());

			// user2는 조회 실패
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "user2"))
					.andExpect(status().is5xxServerError());

			// DB 검증
			assertThat(actorRepository.findAll()).hasSize(3);
			assertThat(preparedContextRepository.findAll()).hasSize(2);
		}

		@Test
		@DisplayName("시나리오: 동일 사용자의 Context 변경 이력")
		void contextChangeHistory() throws Exception {
			// 1단계: 초기 저장
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(context)))
					.andExpect(status().isOk());

			// 2단계: 첫 번째 수정
			Context update1 = Context.builder()
					.userId("test-user")
					.category("running_history_v2")
					.data(Map.of("version", "2"))
					.build();

			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(update1)))
					.andExpect(status().isOk());

			// 3단계: 두 번째 수정
			Context update2 = Context.builder()
					.userId("test-user")
					.category("running_history_v3")
					.data(Map.of("version", "3"))
					.build();

			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(update2)))
					.andExpect(status().isOk());

			// 최종 상태 확인
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextType").value("running_history_v3"));

			// DB 검증 - Context는 항상 1개만 유지
			assertThat(preparedContextRepository.findAll()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("API 엔드포인트 및 HTTP 메서드 검증")
	class ApiMappingTest {

		@Test
		@DisplayName("모든 엔드포인트가 POST 메서드만 허용")
		void allEndpointsOnlyAcceptPost() throws Exception {
			String[] endpoints = {"/save", "/get", "/update", "/delete"};

			for (String endpoint : endpoints) {
				// GET은 405 Method Not Allowed
				mockMvc.perform(get("/api/v1/context" + endpoint))
						.andExpect(status().isMethodNotAllowed());
			}
		}

		@Test
		@DisplayName("잘못된 경로 접근 시 404")
		void wrongPath_Returns404() throws Exception {
			String requestBody = objectMapper.writeValueAsString(context);

			mockMvc.perform(post("/api/v1/context/invalid")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("기본 경로 확인")
		void basePathVerification() throws Exception {
			// /api/v1/context가 기본 경로
			String requestBody = objectMapper.writeValueAsString(context);

			// 정상 경로
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk());

			// 잘못된 기본 경로
			mockMvc.perform(post("/api/v2/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isNotFound());
		}
	}
}