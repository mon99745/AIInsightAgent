package com.aiinsightagent.app.controller;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.service.ActorService;
import com.aiinsightagent.app.service.PreparedContextService;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContextController.class)
@ActiveProfiles("test")
@DisplayName("ContextController 테스트")
class ContextControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ActorService actorService;

	@MockitoBean
	private PreparedContextService contextService;

	private Actor actor;
	private Context context;
	private PreparedContext preparedContext;
	private Map<String, String> contextData;

	@BeforeEach
	void setUp() {
		// Actor 생성
		actor = Actor.create("test-user");
		ReflectionTestUtils.setField(actor, "actorId", 1L);

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

		// PreparedContext 생성
		preparedContext = new PreparedContext(actor, "running_history", contextData.toString());
		ReflectionTestUtils.setField(preparedContext, "contextId", 1L);
		ReflectionTestUtils.setField(preparedContext, "regDate", LocalDateTime.now());
	}

	@Nested
	@DisplayName("POST /api/v1/context/save")
	class SaveContextTest {

		@Test
		@DisplayName("전처리 데이터 저장 성공")
		void saveContext_Success() throws Exception {
			// given
			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(contextService.save(actor, context))
					.willReturn(preparedContext);

			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextId").value(1))
					.andExpect(jsonPath("$.contextType").value("running_history"))
					.andExpect(jsonPath("$.contextScope").value("ACTOR"))
					.andExpect(jsonPath("$.active").value(true))
					.andExpect(jsonPath("$.confidenceLevel").value("MEDIUM"));

			verify(actorService, times(1)).getOrCreate("test-user");
			verify(contextService, times(1)).save(actor, context);
		}

		@Test
		@DisplayName("새로운 Actor 생성 후 Context 저장")
		void saveContext_NewActor_Success() throws Exception {
			// given
			Actor newActor = Actor.create("new-user");
			ReflectionTestUtils.setField(newActor, "actorId", 2L);

			Context newContext = Context.builder()
					.userId("new-user")
					.category("health_metrics")
					.data(Map.of("heartRate", "170"))
					.build();

			PreparedContext newPreparedContext = new PreparedContext(
					newActor, "health_metrics", "{heartRate=170}"
			);
			ReflectionTestUtils.setField(newPreparedContext, "contextId", 2L);

			given(actorService.getOrCreate("new-user"))
					.willReturn(newActor);
			given(contextService.save(newActor, newContext))
					.willReturn(newPreparedContext);

			String requestBody = objectMapper.writeValueAsString(newContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextId").value(2))
					.andExpect(jsonPath("$.contextType").value("health_metrics"));

			verify(actorService, times(1)).getOrCreate("new-user");
			verify(contextService, times(1)).save(newActor, newContext);
		}

		@Test
		@DisplayName("이미 존재하는 Context 저장 시 예외 발생")
		void saveContext_ExistingContext_ThrowsException() throws Exception {
			// given
			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(contextService.save(any(Actor.class), any(Context.class)))
					.willThrow(new InsightException(InsightError.EXIST_ACTOR_PREPARED_CONTEXT + ":test-user"));

			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(actorService, times(1)).getOrCreate("test-user");
			verify(contextService, times(1)).save(any(Actor.class), any(Context.class));
		}

		@Test
		@DisplayName("잘못된 JSON 형식으로 요청 시 400 에러")
		void saveContext_InvalidJson_Returns400() throws Exception {
			// given
			String invalidJson = "{invalid json}";

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(invalidJson))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(actorService, contextService);
		}

		@Test
		@DisplayName("Content-Type이 없는 요청")
		void saveContext_NoContentType_Returns415() throws Exception {
			// given
			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/save")
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isUnsupportedMediaType());

			verifyNoInteractions(actorService, contextService);
		}
	}

	@Nested
	@DisplayName("POST /api/v1/context/get")
	class GetContextTest {

		@Test
		@DisplayName("전처리 데이터 조회 성공")
		void getContext_Success() throws Exception {
			// given
			given(actorService.get("test-user"))
					.willReturn(actor);
			given(contextService.get(actor))
					.willReturn(preparedContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextId").value(1))
					.andExpect(jsonPath("$.contextType").value("running_history"))
					.andExpect(jsonPath("$.contextScope").value("ACTOR"))
					.andExpect(jsonPath("$.active").value(true));

			verify(actorService, times(1)).get("test-user");
			verify(contextService, times(1)).get(actor);
		}

		@Test
		@DisplayName("존재하지 않는 Actor 조회 시 예외 발생")
		void getContext_ActorNotFound_ThrowsException() throws Exception {
			// given
			given(actorService.get("non-existent-user"))
					.willThrow(new InsightException(InsightError.NOT_FOUND_ACTOR + ":non-existent-user"));

			// when & then
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "non-existent-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(actorService, times(1)).get("non-existent-user");
			verifyNoInteractions(contextService);
		}

		@Test
		@DisplayName("PreparedContext가 없는 경우 예외 발생")
		void getContext_ContextNotFound_ThrowsException() throws Exception {
			// given
			given(actorService.get("test-user"))
					.willReturn(actor);
			given(contextService.get(actor))
					.willThrow(new InsightException(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT + ":test-user"));

			// when & then
			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(actorService, times(1)).get("test-user");
			verify(contextService, times(1)).get(actor);
		}

		@Test
		@DisplayName("userId 파라미터 누락 시 400 에러")
		void getContext_MissingUserId_Returns400() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v1/context/get"))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(actorService, contextService);
		}
	}

	@Nested
	@DisplayName("POST /api/v1/context/update")
	class UpdateContextTest {

		@Test
		@DisplayName("전처리 데이터 수정 성공")
		void updateContext_Success() throws Exception {
			// given
			Map<String, String> updatedData = new HashMap<>();
			updatedData.put("averagePace", "5:30");
			updatedData.put("totalDistance", "150km");

			Context updatedContext = Context.builder()
					.userId("test-user")
					.category("updated_running_history")
					.data(updatedData)
					.build();

			PreparedContext updatedPreparedContext = new PreparedContext(
					actor, "updated_running_history", updatedData.toString()
			);
			ReflectionTestUtils.setField(updatedPreparedContext, "contextId", 1L);

			given(actorService.get("test-user"))
					.willReturn(actor);
			given(contextService.update(actor, updatedContext))
					.willReturn(updatedPreparedContext);

			String requestBody = objectMapper.writeValueAsString(updatedContext);

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.contextId").value(1))
					.andExpect(jsonPath("$.contextType").value("updated_running_history"));

			verify(actorService, times(1)).get("test-user");
			verify(contextService, times(1)).update(actor, updatedContext);
		}

		@Test
		@DisplayName("존재하지 않는 Actor 수정 시 예외 발생")
		void updateContext_ActorNotFound_ThrowsException() throws Exception {
			// given
			given(actorService.get("test-user"))
					.willThrow(new InsightException(InsightError.NOT_FOUND_ACTOR + ":test-user"));

			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(actorService, times(1)).get("test-user");
			verifyNoInteractions(contextService);
		}

		@Test
		@DisplayName("PreparedContext가 없는 경우 수정 실패")
		void updateContext_ContextNotFound_ThrowsException() throws Exception {
			// given
			given(actorService.get("test-user"))
					.willReturn(actor);
			given(contextService.update(any(Actor.class), any(Context.class)))
					.willThrow(new InsightException(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT + ":test-user"));

			String requestBody = objectMapper.writeValueAsString(context);

			// when & then
			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(actorService, times(1)).get("test-user");
			verify(contextService, times(1)).update(any(Actor.class), any(Context.class));
		}
	}

	@Nested
	@DisplayName("POST /api/v1/context/delete")
	class DeleteContextTest {

		@Test
		@DisplayName("전처리 데이터 삭제 성공")
		void deleteContext_Success() throws Exception {
			// given
			given(actorService.get("test-user"))
					.willReturn(actor);
			doNothing().when(contextService).delete(actor);

			// when & then
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().isOk());

			verify(actorService, times(1)).get("test-user");
			verify(contextService, times(1)).delete(actor);
		}

		@Test
		@DisplayName("존재하지 않는 Actor 삭제 시 예외 발생")
		void deleteContext_ActorNotFound_ThrowsException() throws Exception {
			// given
			given(actorService.get("non-existent-user"))
					.willThrow(new InsightException(InsightError.NOT_FOUND_ACTOR + ":non-existent-user"));

			// when & then
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "non-existent-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(actorService, times(1)).get("non-existent-user");
			verifyNoInteractions(contextService);
		}

		@Test
		@DisplayName("PreparedContext가 없는 경우 삭제 실패")
		void deleteContext_ContextNotFound_ThrowsException() throws Exception {
			// given
			given(actorService.get("test-user"))
					.willReturn(actor);
			doThrow(new InsightException(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT + ":test-user"))
					.when(contextService).delete(actor);

			// when & then
			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(actorService, times(1)).get("test-user");
			verify(contextService, times(1)).delete(actor);
		}

		@Test
		@DisplayName("userId 파라미터 누락 시 400 에러")
		void deleteContext_MissingUserId_Returns400() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v1/context/delete"))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(actorService, contextService);
		}
	}

	@Nested
	@DisplayName("통합 시나리오 테스트")
	class IntegrationScenarioTest {

		@Test
		@DisplayName("Context 생성 -> 조회 -> 수정 -> 삭제 흐름")
		void fullLifecycle() throws Exception {
			// 1. Save
			given(actorService.getOrCreate("test-user")).willReturn(actor);
			given(contextService.save(any(), any())).willReturn(preparedContext);

			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(context)))
					.andExpect(status().isOk());

			// 2. Get
			given(actorService.get("test-user")).willReturn(actor);
			given(contextService.get(actor)).willReturn(preparedContext);

			mockMvc.perform(post("/api/v1/context/get")
							.param("userId", "test-user"))
					.andExpect(status().isOk());

			// 3. Update
			given(contextService.update(any(), any())).willReturn(preparedContext);

			mockMvc.perform(post("/api/v1/context/update")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(context)))
					.andExpect(status().isOk());

			// 4. Delete
			doNothing().when(contextService).delete(actor);

			mockMvc.perform(post("/api/v1/context/delete")
							.param("userId", "test-user"))
					.andExpect(status().isOk());

			// Verify
			verify(actorService, times(1)).getOrCreate("test-user");
			verify(actorService, times(3)).get("test-user");
			verify(contextService, times(1)).save(any(), any());
			verify(contextService, times(1)).get(actor);
			verify(contextService, times(1)).update(any(), any());
			verify(contextService, times(1)).delete(actor);
		}
	}

	@Nested
	@DisplayName("API 경로 및 메서드 검증")
	class ApiMappingTest {

		@Test
		@DisplayName("POST /api/v1/context/save 경로 확인")
		void saveContextPath() throws Exception {
			given(actorService.getOrCreate(any())).willReturn(actor);
			given(contextService.save(any(), any())).willReturn(preparedContext);

			mockMvc.perform(post("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(context)))
					.andExpect(status().isOk());
		}

		@Test
		@DisplayName("잘못된 경로 접근 시 404")
		void wrongPath_Returns404() throws Exception {
			mockMvc.perform(post("/api/v1/context/invalid")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(context)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("GET 메서드로 접근 시 405")
		void wrongHttpMethod_Returns405() throws Exception {
			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
							.get("/api/v1/context/save")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(context)))
					.andExpect(status().isMethodNotAllowed());
		}
	}
}