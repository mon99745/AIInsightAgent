package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import com.aiinsightagent.app.entity.AnalysisResult;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.enums.AnalysisStatus;
import com.aiinsightagent.app.enums.AnalysisType;
import com.aiinsightagent.app.enums.InputType;
import com.aiinsightagent.app.util.InsightRequestValidator;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.facade.InsightFacade;
import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightHistoryResponse;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InsightService 테스트")
class InsightServiceTest {

	@Mock
	private InsightFacade insightFacade;

	@Mock
	private ActorService actorService;

	@Mock
	private AnalysisRawDataService rawDataService;

	@Mock
	private AnalysisResultService resultService;

	@Mock
	private PreparedContextService contextService;

	@InjectMocks
	private InsightService insightService;

	private Actor actor;
	private InsightRequest insightRequest;
	private List<UserPrompt> userPrompts;
	private AnalysisRawData analysisRawData;
	private PreparedContext preparedContext;
	private InsightResponse insightResponse;
	private AnalysisResult analysisResult;

	@BeforeEach
	void setUp() {
		// Actor 생성
		actor = Actor.create("test-user");
		ReflectionTestUtils.setField(actor, "actorId", 1L);

		// UserPrompt 리스트 생성
		userPrompts = createUserPrompts();

		// InsightRequest 생성
		insightRequest = InsightRequest.builder()
				.userId("test-user")
				.purpose("running_style_analysis")
				.userPrompt(userPrompts)
				.build();

		// AnalysisRawData 생성
		String rawJson = "[{\"dataKey\":\"A0398D47\",\"data\":{\"duration\":\"3556\"}}]";
		analysisRawData = new AnalysisRawData(actor, InputType.JSON, rawJson);
		ReflectionTestUtils.setField(analysisRawData, "inputId", 1L);
		ReflectionTestUtils.setField(analysisRawData, "regDate", LocalDateTime.now());

		// PreparedContext 생성
		String contextPayload = "사용자의 평균 페이스는 6분/km입니다.";
		preparedContext = new PreparedContext(actor, "running_history", contextPayload);
		ReflectionTestUtils.setField(preparedContext, "contextId", 1L);

		// InsightResponse 생성
		InsightDetail insightDetail = mock(InsightDetail.class);
		insightResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Success")
				.insight(insightDetail)
				.build();

		// AnalysisResult 생성
		String resultPayload = "{\"runningStyle\":\"endurance\"}";
		analysisResult = new AnalysisResult(
				actor,
				analysisRawData,
				AnalysisType.STYLE,
				AnalysisStatus.SUCCESS,
				resultPayload
		);
		ReflectionTestUtils.setField(analysisResult, "resultId", 1L);
	}

	private List<UserPrompt> createUserPrompts() {
		Map<String, String> data1 = new HashMap<>();
		data1.put("duration", "3556");
		data1.put("heartRate", "194.63");
		data1.put("distance", "9.95");
		data1.put("stepCount", "10114");

		Map<String, String> data2 = new HashMap<>();
		data2.put("duration", "1965");
		data2.put("heartRate", "181.92");
		data2.put("distance", "6.01");
		data2.put("stepCount", "5702");

		return Arrays.asList(
				UserPrompt.builder()
						.dataKey("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99")
						.data(data1)
						.build(),
				UserPrompt.builder()
						.dataKey("2FEC0793-820B-4F82-BBBA-951FB26B7455")
						.data(data2)
						.build()
		);
	}

	@Test
	@DisplayName("requestInsight(purpose, prompt) - 단순 질의 응답 성공")
	void requestInsight_WithPurposeAndPrompt_Success() {
		// given
		String purpose = "running_style_analysis";
		String prompt = "내 러닝 스타일을 분석해줘";

		InsightDetail insightDetail = mock(InsightDetail.class);
		InsightResponse expectedResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Success")
				.insight(insightDetail)
				.build();

		given(insightFacade.answer(purpose, prompt))
				.willReturn(expectedResponse);

		// when
		InsightResponse response = insightService.requestInsight(purpose, prompt);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getResultCode()).isEqualTo(200);
		assertThat(response.getResultMsg()).isEqualTo("Success");

		verify(insightFacade, times(1)).answer(purpose, prompt);
		verifyNoInteractions(actorService, rawDataService, resultService, contextService);
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - 전체 분석 프로세스 성공")
	void requestInsight_WithInsightRequest_Success() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(rawDataService.save(actor, userPrompts))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(actor))
					.willReturn(Optional.of(preparedContext));
			given(insightFacade.analysis(insightRequest, preparedContext.asPromptText()))
					.willReturn(insightResponse);
			given(resultService.save(actor, analysisRawData, insightResponse))
					.willReturn(analysisResult);

			// when
			InsightResponse response = insightService.requestInsight(insightRequest);

			// then
			assertThat(response).isNotNull();
			assertThat(response.getResultCode()).isEqualTo(200);
			assertThat(response.getResultMsg()).isEqualTo("Success");

			// 검증: 모든 단계가 순서대로 실행되었는지
			mockedValidator.verify(() -> InsightRequestValidator.validate(insightRequest), times(1));
			verify(actorService, times(1)).getOrCreate("test-user");
			verify(rawDataService, times(1)).save(actor, userPrompts);
			verify(contextService, times(1)).findByActorKey(actor);
			verify(insightFacade, times(1)).analysis(insightRequest, preparedContext.asPromptText());
			verify(resultService, times(1)).save(actor, analysisRawData, insightResponse);
		}
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - PreparedContext가 없는 경우")
	void requestInsight_WithoutPreparedContext_Success() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(rawDataService.save(actor, userPrompts))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(actor))
					.willReturn(Optional.empty());
			given(insightFacade.analysis(insightRequest, null))
					.willReturn(insightResponse);
			given(resultService.save(actor, analysisRawData, insightResponse))
					.willReturn(analysisResult);

			// when
			InsightResponse response = insightService.requestInsight(insightRequest);

			// then
			assertThat(response).isNotNull();

			verify(contextService, times(1)).findByActorKey(actor);
			verify(insightFacade, times(1)).analysis(insightRequest, null);
		}
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - 데이터 검증 실패 시 예외 발생")
	void requestInsight_ValidationFails_ThrowsException() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.thenThrow(new IllegalArgumentException("Invalid request"));

			// when & then
			assertThatThrownBy(() -> insightService.requestInsight(insightRequest))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Invalid request");

			// 검증: 검증 실패 후 다른 메서드가 호출되지 않음
			verifyNoInteractions(actorService, rawDataService, contextService,
					insightFacade, resultService);
		}
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - Actor 생성 및 조회")
	void requestInsight_CreatesOrGetsActor() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			Actor newActor = Actor.create("new-user");
			ReflectionTestUtils.setField(newActor, "actorId", 2L);

			InsightRequest newUserRequest = InsightRequest.builder()
					.userId("new-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			given(actorService.getOrCreate("new-user"))
					.willReturn(newActor);
			given(rawDataService.save(newActor, userPrompts))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(newActor))
					.willReturn(Optional.empty());
			given(insightFacade.analysis(newUserRequest, null))
					.willReturn(insightResponse);
			given(resultService.save(newActor, analysisRawData, insightResponse))
					.willReturn(analysisResult);

			// when
			insightService.requestInsight(newUserRequest);

			// then
			verify(actorService, times(1)).getOrCreate("new-user");
		}
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - 원본 데이터 저장 확인")
	void requestInsight_SavesRawData() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(rawDataService.save(actor, userPrompts))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(actor))
					.willReturn(Optional.empty());
			given(insightFacade.analysis(any(InsightRequest.class), any()))
					.willReturn(insightResponse);
			given(resultService.save(any(Actor.class), any(AnalysisRawData.class),
					any(InsightResponse.class)))
					.willReturn(analysisResult);

			// when
			insightService.requestInsight(insightRequest);

			// then
			verify(rawDataService, times(1)).save(actor, userPrompts);
		}
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - 분석 결과 저장 확인")
	void requestInsight_SavesAnalysisResult() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(rawDataService.save(actor, userPrompts))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(actor))
					.willReturn(Optional.empty());
			given(insightFacade.analysis(insightRequest, null))
					.willReturn(insightResponse);
			given(resultService.save(actor, analysisRawData, insightResponse))
					.willReturn(analysisResult);

			// when
			insightService.requestInsight(insightRequest);

			// then
			verify(resultService, times(1)).save(actor, analysisRawData, insightResponse);
		}
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - 컨텍스트 텍스트가 분석에 전달됨")
	void requestInsight_PassesContextTextToAnalysis() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			String expectedContextText = "사용자의 평균 페이스는 6분/km입니다.";

			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(rawDataService.save(actor, userPrompts))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(actor))
					.willReturn(Optional.of(preparedContext));
			given(insightFacade.analysis(insightRequest, expectedContextText))
					.willReturn(insightResponse);
			given(resultService.save(actor, analysisRawData, insightResponse))
					.willReturn(analysisResult);

			// when
			insightService.requestInsight(insightRequest);

			// then
			verify(insightFacade, times(1)).analysis(insightRequest, expectedContextText);
		}
	}

	@Test
	@DisplayName("getHistory - 사용자 히스토리 조회 성공")
	void getHistory_Success() {
		// given
		String userId = "test-user";

		InsightHistoryResponse expectedResponse = InsightHistoryResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insightRecords(Collections.emptyList())
				.build();

		given(actorService.get(userId))
				.willReturn(actor);
		given(rawDataService.getUserPromtListByActor(actor))
				.willReturn(expectedResponse);

		// when
		InsightHistoryResponse response = insightService.getHistory(userId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getResultCode()).isEqualTo(HttpStatus.OK.value());

		verify(actorService, times(1)).get(userId);
		verify(rawDataService, times(1)).getUserPromtListByActor(actor);
	}

	@Test
	@DisplayName("getHistory - 존재하지 않는 사용자 조회 시 예외 발생")
	void getHistory_UserNotFound_ThrowsException() {
		// given
		String userId = "non-existent-user";

		given(actorService.get(userId))
				.willThrow(new InsightException(InsightError.NOT_FOUND_ACTOR + ":" + userId));

		// when & then
		assertThatThrownBy(() -> insightService.getHistory(userId))
				.isInstanceOf(InsightException.class)
				.hasMessageContaining(InsightError.NOT_FOUND_ACTOR.toString())
				.hasMessageContaining(userId);

		verify(actorService, times(1)).get(userId);
		verifyNoInteractions(rawDataService);
	}

	@Test
	@DisplayName("getHistory - 빈 히스토리 반환")
	void getHistory_EmptyHistory_Success() {
		// given
		String userId = "test-user";

		InsightHistoryResponse emptyResponse = InsightHistoryResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insightRecords(Collections.emptyList())
				.build();

		given(actorService.get(userId))
				.willReturn(actor);
		given(rawDataService.getUserPromtListByActor(actor))
				.willReturn(emptyResponse);

		// when
		InsightHistoryResponse response = insightService.getHistory(userId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getInsightRecords()).isEmpty();

		verify(actorService, times(1)).get(userId);
		verify(rawDataService, times(1)).getUserPromtListByActor(actor);
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - 트랜잭션 롤백 시나리오")
	void requestInsight_TransactionRollback_WhenAnalysisFails() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			given(actorService.getOrCreate("test-user"))
					.willReturn(actor);
			given(rawDataService.save(actor, userPrompts))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(actor))
					.willReturn(Optional.empty());
			given(insightFacade.analysis(insightRequest, null))
					.willThrow(new RuntimeException("Analysis failed"));

			// when & then
			assertThatThrownBy(() -> insightService.requestInsight(insightRequest))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("Analysis failed");

			// 분석 실패 후 결과 저장은 호출되지 않아야 함
			verify(resultService, never()).save(any(), any(), any());
		}
	}

	@Test
	@DisplayName("requestInsight(InsightRequest) - 여러 사용자의 동시 요청 처리")
	void requestInsight_MultipleUsers_Success() {
		// given
		try (MockedStatic<InsightRequestValidator> mockedValidator =
					 mockStatic(InsightRequestValidator.class)) {

			mockedValidator.when(() -> InsightRequestValidator.validate(any(InsightRequest.class)))
					.then(invocation -> null);

			Actor actor1 = Actor.create("user-1");
			Actor actor2 = Actor.create("user-2");

			InsightRequest request1 = InsightRequest.builder()
					.userId("user-1")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			InsightRequest request2 = InsightRequest.builder()
					.userId("user-2")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			given(actorService.getOrCreate("user-1")).willReturn(actor1);
			given(actorService.getOrCreate("user-2")).willReturn(actor2);
			given(rawDataService.save(any(Actor.class), any()))
					.willReturn(analysisRawData);
			given(contextService.findByActorKey(any(Actor.class)))
					.willReturn(Optional.empty());
			given(insightFacade.analysis(any(InsightRequest.class), any()))
					.willReturn(insightResponse);
			given(resultService.save(any(), any(), any()))
					.willReturn(analysisResult);

			// when
			insightService.requestInsight(request1);
			insightService.requestInsight(request2);

			// then
			verify(actorService, times(1)).getOrCreate("user-1");
			verify(actorService, times(1)).getOrCreate("user-2");
			verify(insightFacade, times(2)).analysis(any(InsightRequest.class), any());
		}
	}
}