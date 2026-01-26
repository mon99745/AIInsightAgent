package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import com.aiinsightagent.app.entity.AnalysisResult;
import com.aiinsightagent.app.enums.AnalysisStatus;
import com.aiinsightagent.app.enums.AnalysisType;
import com.aiinsightagent.app.enums.InputType;
import com.aiinsightagent.app.repository.AnalysisResultRepository;
import com.aiinsightagent.app.util.InsightResultSerializer;
import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisResultService 테스트")
class AnalysisResultServiceTest {

	@Mock
	private AnalysisResultRepository resultRepository;

	@Mock
	private InsightResultSerializer serializer;

	@InjectMocks
	private AnalysisResultService analysisResultService;

	private Actor actor;
	private AnalysisRawData analysisRawData;
	private InsightResponse insightResponse;
	private InsightDetail insightDetail;
	private String serializedResult;
	private AnalysisResult analysisResult;

	@BeforeEach
	void setUp() {
		// Actor 생성
		actor = Actor.create("test-user-actor");
		ReflectionTestUtils.setField(actor, "actorId", 1L);

		// AnalysisRawData 생성
		String runningDataJson = createRunningDataJson();
		analysisRawData = new AnalysisRawData(actor, InputType.JSON, "running_style_analysis", runningDataJson);
		ReflectionTestUtils.setField(analysisRawData, "inputId", 1L);
		ReflectionTestUtils.setField(analysisRawData, "regDate", LocalDateTime.now());

		// InsightDetail 생성 (Mock 또는 실제 객체)
		insightDetail = mock(InsightDetail.class);

		// InsightResponse 생성
		insightResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Success")
				.insight(insightDetail)
				.build();

		// 직렬화된 결과
		serializedResult = createSerializedResult();

		// AnalysisResult 생성
		analysisResult = new AnalysisResult(
				actor,
				analysisRawData,
				AnalysisType.STYLE,
				AnalysisStatus.SUCCESS,
				serializedResult,
				"gemini-2.5-flash#00"
		);
		ReflectionTestUtils.setField(analysisResult, "resultId", 1L);
		ReflectionTestUtils.setField(analysisResult, "requestId", UUID.randomUUID());
		ReflectionTestUtils.setField(analysisResult, "regDate", LocalDateTime.now());
	}

	private String createRunningDataJson() {
		return "[{\"dataKey\":\"A0398D47-38EB-4FEA-A8C2-34DF8E46DC99\"," +
				"\"data\":{\"duration\":\"3556\",\"heartRate\":\"194.63\"," +
				"\"distance\":\"9.95\",\"stepCount\":\"10114\"}}]";
	}

	private String createSerializedResult() {
		return "{\"runningStyle\":\"endurance\"," +
				"\"averagePace\":\"6:00\"," +
				"\"averageHeartRate\":\"185\"," +
				"\"recommendation\":\"페이스 조절이 필요합니다\"}";
	}

	@Test
	@DisplayName("save - 분석 결과 정상 저장")
	void save_Success() {
		// given
		given(serializer.serialize(insightDetail))
				.willReturn(serializedResult);
		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(analysisResult);

		// when
		AnalysisResult result = analysisResultService.save(actor, analysisRawData, insightResponse, "gemini-2.5-flash#00");

		// then
		assertThat(result).isNotNull();
		assertThat(result.getActor()).isEqualTo(actor);
		assertThat(result.getAnalysisInput()).isEqualTo(analysisRawData);
		assertThat(result.getAnalysisType()).isEqualTo(AnalysisType.STYLE);
		assertThat(result.getStatus()).isEqualTo(AnalysisStatus.SUCCESS);
		assertThat(result.getResultPayload()).isEqualTo(serializedResult);
		assertThat(result.getResultId()).isEqualTo(1L);

		verify(serializer, times(1)).serialize(insightDetail);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - ArgumentCaptor로 저장되는 AnalysisResult 검증")
	void save_VerifyAnalysisResultWithArgumentCaptor() {
		// given
		ArgumentCaptor<AnalysisResult> captor = ArgumentCaptor.forClass(AnalysisResult.class);

		given(serializer.serialize(insightDetail))
				.willReturn(serializedResult);
		given(resultRepository.save(captor.capture()))
				.willReturn(analysisResult);

		// when
		analysisResultService.save(actor, analysisRawData, insightResponse, "gemini-2.5-flash#00");

		// then
		AnalysisResult capturedResult = captor.getValue();
		assertThat(capturedResult.getActor()).isEqualTo(actor);
		assertThat(capturedResult.getAnalysisInput()).isEqualTo(analysisRawData);
		assertThat(capturedResult.getAnalysisType()).isEqualTo(AnalysisType.STYLE);
		assertThat(capturedResult.getStatus()).isEqualTo(AnalysisStatus.SUCCESS);
		assertThat(capturedResult.getResultPayload()).isEqualTo(serializedResult);

		verify(serializer, times(1)).serialize(insightDetail);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - InsightResponse의 InsightDetail을 직렬화하여 저장")
	void save_SerializesInsightDetailFromResponse() {
		// given
		InsightDetail customInsightDetail = mock(InsightDetail.class);
		InsightResponse customResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Success")
				.insight(customInsightDetail)
				.build();

		String customSerializedResult = "{\"customField\":\"customValue\"}";

		given(serializer.serialize(customInsightDetail))
				.willReturn(customSerializedResult);
		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(analysisResult);

		// when
		analysisResultService.save(actor, analysisRawData, customResponse, "gemini-2.5-flash#00");

		// then
		verify(serializer, times(1)).serialize(customInsightDetail);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - InsightDetail이 null인 경우")
	void save_WithNullInsightDetail() {
		// given
		InsightResponse nullInsightResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Success")
				.insight(null)
				.build();

		given(serializer.serialize(null))
				.willReturn(null);

		AnalysisResult nullPayloadResult = new AnalysisResult(
				actor,
				analysisRawData,
				AnalysisType.STYLE,
				AnalysisStatus.SUCCESS,
				null,
				"gemini-2.5-flash#00"
		);

		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(nullPayloadResult);

		// when
		AnalysisResult result = analysisResultService.save(actor, analysisRawData, nullInsightResponse, "gemini-2.5-flash#00");

		// then
		assertThat(result).isNotNull();
		verify(serializer, times(1)).serialize(null);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - 러닝 스타일 분석 InsightResponse 저장")
	void save_RunningStyleAnalysisResponse_Success() {
		// given
		InsightDetail runningStyleDetail = mock(InsightDetail.class);
		InsightResponse runningResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Running Style Analysis Complete")
				.insight(runningStyleDetail)
				.build();

		String runningStyleJson = "{\"runningStyle\":\"speed\",\"averagePace\":\"4:30\"," +
				"\"strongPoints\":\"빠른 페이스 유지\",\"weakPoints\":\"장거리 지구력\"}";

		given(serializer.serialize(runningStyleDetail))
				.willReturn(runningStyleJson);
		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(analysisResult);

		// when
		AnalysisResult result = analysisResultService.save(actor, analysisRawData, runningResponse, "gemini-2.5-flash#00");

		// then
		assertThat(result).isNotNull();
		assertThat(result.getAnalysisType()).isEqualTo(AnalysisType.STYLE);
		verify(serializer, times(1)).serialize(runningStyleDetail);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - 여러 Actor에 대한 분석 결과 저장")
	void save_MultipleActors_Success() {
		// given
		Actor actor1 = Actor.create("user-1");
		ReflectionTestUtils.setField(actor1, "actorId", 1L);

		Actor actor2 = Actor.create("user-2");
		ReflectionTestUtils.setField(actor2, "actorId", 2L);

		AnalysisRawData rawData1 = new AnalysisRawData(actor1, InputType.JSON, "running_style_analysis", "{}");
		ReflectionTestUtils.setField(rawData1, "inputId", 1L);

		AnalysisRawData rawData2 = new AnalysisRawData(actor2, InputType.JSON, "running_style_analysis", "{}");
		ReflectionTestUtils.setField(rawData2, "inputId", 2L);

		AnalysisResult result1 = new AnalysisResult(
				actor1, rawData1, AnalysisType.STYLE, AnalysisStatus.SUCCESS, serializedResult, "gemini-2.5-flash#00"
		);
		ReflectionTestUtils.setField(result1, "resultId", 1L);

		AnalysisResult result2 = new AnalysisResult(
				actor2, rawData2, AnalysisType.STYLE, AnalysisStatus.SUCCESS, serializedResult, "gemini-2.5-flash#00"
		);
		ReflectionTestUtils.setField(result2, "resultId", 2L);

		given(serializer.serialize(any(InsightDetail.class)))
				.willReturn(serializedResult);
		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(result1)
				.willReturn(result2);

		// when
		AnalysisResult savedResult1 = analysisResultService.save(actor1, rawData1, insightResponse, "gemini-2.5-flash#00");
		AnalysisResult savedResult2 = analysisResultService.save(actor2, rawData2, insightResponse, "gemini-2.5-flash#00");

		// then
		assertThat(savedResult1.getResultId()).isEqualTo(1L);
		assertThat(savedResult2.getResultId()).isEqualTo(2L);
		verify(serializer, times(2)).serialize(any(InsightDetail.class));
		verify(resultRepository, times(2)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - Serializer가 빈 문자열을 반환하는 경우")
	void save_SerializerReturnsEmptyString() {
		// given
		given(serializer.serialize(insightDetail))
				.willReturn("");

		AnalysisResult emptyPayloadResult = new AnalysisResult(
				actor,
				analysisRawData,
				AnalysisType.STYLE,
				AnalysisStatus.SUCCESS,
				"",
				"gemini-2.5-flash#00"
		);

		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(emptyPayloadResult);

		// when
		AnalysisResult result = analysisResultService.save(actor, analysisRawData, insightResponse, "gemini-2.5-flash#00");

		// then
		assertThat(result).isNotNull();
		assertThat(result.getResultPayload()).isEmpty();
		verify(serializer, times(1)).serialize(insightDetail);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - AnalysisType이 STYLE로 고정되어 저장")
	void save_AlwaysSavesWithAnalysisTypeStyle() {
		// given
		ArgumentCaptor<AnalysisResult> captor = ArgumentCaptor.forClass(AnalysisResult.class);

		given(serializer.serialize(insightDetail))
				.willReturn(serializedResult);
		given(resultRepository.save(captor.capture()))
				.willReturn(analysisResult);

		// when
		analysisResultService.save(actor, analysisRawData, insightResponse, "gemini-2.5-flash#00");

		// then
		AnalysisResult capturedResult = captor.getValue();
		assertThat(capturedResult.getAnalysisType()).isEqualTo(AnalysisType.STYLE);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - AnalysisStatus가 SUCCESS로 고정되어 저장")
	void save_AlwaysSavesWithAnalysisStatusSuccess() {
		// given
		ArgumentCaptor<AnalysisResult> captor = ArgumentCaptor.forClass(AnalysisResult.class);

		given(serializer.serialize(insightDetail))
				.willReturn(serializedResult);
		given(resultRepository.save(captor.capture()))
				.willReturn(analysisResult);

		// when
		analysisResultService.save(actor, analysisRawData, insightResponse, "gemini-2.5-flash#00");

		// then
		AnalysisResult capturedResult = captor.getValue();
		assertThat(capturedResult.getStatus()).isEqualTo(AnalysisStatus.SUCCESS);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - 동일한 Actor와 RawData로 여러 번 저장 시 각각 다른 requestId 생성")
	void save_MultipleSavesGenerateDifferentRequestIds() {
		// given
		AnalysisResult result1 = new AnalysisResult(
				actor, analysisRawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, serializedResult, "gemini-2.5-flash#00"
		);
		UUID requestId1 = UUID.randomUUID();
		ReflectionTestUtils.setField(result1, "resultId", 1L);
		ReflectionTestUtils.setField(result1, "requestId", requestId1);

		AnalysisResult result2 = new AnalysisResult(
				actor, analysisRawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, serializedResult, "gemini-2.5-flash#00"
		);
		UUID requestId2 = UUID.randomUUID();
		ReflectionTestUtils.setField(result2, "resultId", 2L);
		ReflectionTestUtils.setField(result2, "requestId", requestId2);

		given(serializer.serialize(insightDetail))
				.willReturn(serializedResult);
		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(result1)
				.willReturn(result2);

		// when
		AnalysisResult savedResult1 = analysisResultService.save(actor, analysisRawData, insightResponse, "gemini-2.5-flash#00");
		AnalysisResult savedResult2 = analysisResultService.save(actor, analysisRawData, insightResponse, "gemini-2.5-flash#00");

		// then
		assertThat(savedResult1.getResultId()).isEqualTo(1L);
		assertThat(savedResult2.getResultId()).isEqualTo(2L);
		assertThat(savedResult1.getRequestId()).isNotEqualTo(savedResult2.getRequestId());
		verify(serializer, times(2)).serialize(insightDetail);
		verify(resultRepository, times(2)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - 큰 사이즈의 InsightDetail 직렬화 결과 저장")
	void save_LargeInsightDetail_Success() {
		// given
		InsightDetail largeInsightDetail = mock(InsightDetail.class);
		InsightResponse largeResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Success")
				.insight(largeInsightDetail)
				.build();

		// 큰 사이즈의 JSON 문자열 생성
		StringBuilder largeJson = new StringBuilder("{");
		for (int i = 0; i < 1000; i++) {
			largeJson.append("\"field").append(i).append("\":\"value").append(i).append("\"");
			if (i < 999) largeJson.append(",");
		}
		largeJson.append("}");

		given(serializer.serialize(largeInsightDetail))
				.willReturn(largeJson.toString());
		given(resultRepository.save(any(AnalysisResult.class)))
				.willReturn(analysisResult);

		// when
		AnalysisResult result = analysisResultService.save(actor, analysisRawData, largeResponse, "gemini-2.5-flash#00");

		// then
		assertThat(result).isNotNull();
		verify(serializer, times(1)).serialize(largeInsightDetail);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}

	@Test
	@DisplayName("save - InsightResponse의 resultCode와 resultMsg는 저장되지 않음")
	void save_DoesNotSaveResultCodeAndResultMsg() {
		// given
		ArgumentCaptor<AnalysisResult> captor = ArgumentCaptor.forClass(AnalysisResult.class);

		InsightResponse responseWithCodes = InsightResponse.builder()
				.resultCode(500)
				.resultMsg("Error Message")
				.insight(insightDetail)
				.build();

		given(serializer.serialize(insightDetail))
				.willReturn(serializedResult);
		given(resultRepository.save(captor.capture()))
				.willReturn(analysisResult);

		// when
		analysisResultService.save(actor, analysisRawData, responseWithCodes, "gemini-2.5-flash#00");

		// then
		AnalysisResult capturedResult = captor.getValue();
		// AnalysisResult에는 resultCode나 resultMsg 필드가 없음을 확인
		assertThat(capturedResult.getResultPayload()).isEqualTo(serializedResult);
		assertThat(capturedResult.getStatus()).isEqualTo(AnalysisStatus.SUCCESS);

		verify(serializer, times(1)).serialize(insightDetail);
		verify(resultRepository, times(1)).save(any(AnalysisResult.class));
	}
}