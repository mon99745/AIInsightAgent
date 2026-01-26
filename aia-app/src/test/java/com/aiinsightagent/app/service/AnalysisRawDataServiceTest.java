package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import com.aiinsightagent.app.enums.InputType;
import com.aiinsightagent.app.exception.InsightAppError;
import com.aiinsightagent.app.repository.AnalysisRawDataRepository;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightHistoryResponse;
import com.aiinsightagent.core.model.InsightRecord;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisRawDataService 테스트")
class AnalysisRawDataServiceTest {

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private AnalysisRawDataRepository rawDataRepository;

	@InjectMocks
	private AnalysisRawDataService analysisRawDataService;

	private Actor actor;
	private List<UserPrompt> runningDataPrompts;
	private String runningDataJsonPayload;
	private String purpose;
	private AnalysisRawData analysisRawData;

	@BeforeEach
	void setUp() {
		// Actor 생성
		actor = Actor.create("test-user-actor");
		ReflectionTestUtils.setField(actor, "actorId", 1L);

		// 러닝 데이터 UserPrompt 리스트 생성
		runningDataPrompts = createRunningDataPrompts();

		// purpose 설정
		purpose = "running_style_analysis";

		// JSON payload (실제 러닝 데이터 형식)
		runningDataJsonPayload = createRunningDataJson();

		// AnalysisRawData 생성
		analysisRawData = new AnalysisRawData(actor, InputType.JSON, "running_style_analysis", runningDataJsonPayload);
		ReflectionTestUtils.setField(analysisRawData, "inputId", 1L);
		ReflectionTestUtils.setField(analysisRawData, "regDate", LocalDateTime.now());
	}

	private List<UserPrompt> createRunningDataPrompts() {
		return Arrays.asList(
				UserPrompt.builder()
						.dataKey("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99")
						.data(createRunningData("3556", "194.63", "9.95", "10114"))
						.build(),
				UserPrompt.builder()
						.dataKey("2FEC0793-820B-4F82-BBBA-951FB26B7455")
						.data(createRunningData("1965", "181.92", "6.01", "5702"))
						.build(),
				UserPrompt.builder()
						.dataKey("646500B9-A275-44F3-95BF-5EB19A41694A")
						.data(createRunningData("3587", "184.75", "10.01", "10294"))
						.build(),
				UserPrompt.builder()
						.dataKey("82A3ED54-13FF-4767-ACB1-45A3CBC60F6D")
						.data(createRunningData("1907", "174.76", "5.01", "5384"))
						.build(),
				UserPrompt.builder()
						.dataKey("2195E7C5-7E23-455B-9545-688F78521EDE")
						.data(createRunningData("1785", "182.31", "5.35", "5766"))
						.build()
		);
	}

	private Map<String, String> createRunningData(String duration, String heartRate,
												  String distance, String stepCount) {
		Map<String, String> data = new HashMap<>();
		data.put("duration", duration);
		data.put("heartRate", heartRate);
		data.put("distance", distance);
		data.put("stepCount", stepCount);
		return data;
	}

	private String createRunningDataJson() {
		return "[" +
				"{\"dataKey\":\"A0398D47-38EB-4FEA-A8C2-34DF8E46DC99\"," +
				"\"data\":{\"duration\":\"3556\",\"heartRate\":\"194.63\",\"distance\":\"9.95\",\"stepCount\":\"10114\"}}," +
				"{\"dataKey\":\"2FEC0793-820B-4F82-BBBA-951FB26B7455\"," +
				"\"data\":{\"duration\":\"1965\",\"heartRate\":\"181.92\",\"distance\":\"6.01\",\"stepCount\":\"5702\"}}," +
				"{\"dataKey\":\"646500B9-A275-44F3-95BF-5EB19A41694A\"," +
				"\"data\":{\"duration\":\"3587\",\"heartRate\":\"184.75\",\"distance\":\"10.01\",\"stepCount\":\"10294\"}}," +
				"{\"dataKey\":\"82A3ED54-13FF-4767-ACB1-45A3CBC60F6D\"," +
				"\"data\":{\"duration\":\"1907\",\"heartRate\":\"174.76\",\"distance\":\"5.01\",\"stepCount\":\"5384\"}}," +
				"{\"dataKey\":\"2195E7C5-7E23-455B-9545-688F78521EDE\"," +
				"\"data\":{\"duration\":\"1785\",\"heartRate\":\"182.31\",\"distance\":\"5.35\",\"stepCount\":\"5766\"}}" +
				"]";
	}

	@Test
	@DisplayName("save - 러닝 데이터를 포함한 AnalysisRawData 저장 성공")
	void save_WithRunningData_Success() throws JsonProcessingException {
		// given
		given(objectMapper.writeValueAsString(runningDataPrompts))
				.willReturn(runningDataJsonPayload);
		given(rawDataRepository.save(any(AnalysisRawData.class)))
				.willReturn(analysisRawData);

		// when
		AnalysisRawData result = analysisRawDataService.save(actor, purpose, runningDataPrompts);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getActor()).isEqualTo(actor);
		assertThat(result.getInputType()).isEqualTo(InputType.JSON);
		assertThat(result.getRawPayload()).contains("duration");
		assertThat(result.getRawPayload()).contains("heartRate");
		assertThat(result.getRawPayload()).contains("distance");
		assertThat(result.getRawPayload()).contains("stepCount");
		assertThat(result.getInputId()).isEqualTo(1L);

		verify(objectMapper, times(1)).writeValueAsString(runningDataPrompts);
		verify(rawDataRepository, times(1)).save(any(AnalysisRawData.class));
	}

	@Test
	@DisplayName("save - 10개의 러닝 데이터 저장")
	void save_With10RunningRecords_Success() throws JsonProcessingException {
		// given
		List<UserPrompt> tenRunningData = createTenRunningDataPrompts();
		String tenRecordsJson = createTenRecordsJson();

		given(objectMapper.writeValueAsString(tenRunningData))
				.willReturn(tenRecordsJson);
		given(rawDataRepository.save(any(AnalysisRawData.class)))
				.willReturn(analysisRawData);

		// when
		AnalysisRawData result = analysisRawDataService.save(actor, purpose, tenRunningData);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getRawPayload()).isNotEmpty();
		verify(objectMapper, times(1)).writeValueAsString(tenRunningData);
		verify(rawDataRepository, times(1)).save(any(AnalysisRawData.class));
	}

	@Test
	@DisplayName("save - JSON 직렬화 실패 시 InsightException 발생")
	void save_JsonSerializationFails_ThrowsInsightException() throws JsonProcessingException {
		// given
		given(objectMapper.writeValueAsString(runningDataPrompts))
				.willThrow(new JsonProcessingException("Serialization error") {});

		// when & then
		assertThatThrownBy(() -> analysisRawDataService.save(actor, purpose, runningDataPrompts))
				.isInstanceOf(InsightException.class)
				.extracting(e -> ((InsightException) e).getError())
				.isEqualTo(InsightAppError.FAIL_JSON_SERIALIZATION);

		verify(objectMapper, times(1)).writeValueAsString(runningDataPrompts);
		verify(rawDataRepository, never()).save(any(AnalysisRawData.class));
	}

	@Test
	@DisplayName("getUserPromtListByActor - 러닝 데이터 조회 성공")
	void getUserPromtListByActor_WithRunningData_Success() throws JsonProcessingException {
		// given
		List<AnalysisRawData> rawDataList = Arrays.asList(analysisRawData);

		given(rawDataRepository.findAllByActor(actor))
				.willReturn(rawDataList);
		given(objectMapper.readValue(anyString(), any(TypeReference.class)))
				.willReturn(runningDataPrompts);

		// when
		InsightHistoryResponse response = analysisRawDataService.getUserPromtListByActor(actor);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getResultCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getResultMsg()).isEqualTo(HttpStatus.OK.getReasonPhrase());
		assertThat(response.getInsightRecords()).hasSize(5);

		// 첫 번째 러닝 기록 검증
		InsightRecord firstRecord = response.getInsightRecords().get(0);
		assertThat(firstRecord.getInputId()).isEqualTo(1L);
		assertThat(firstRecord.getUserPrompt().getDataKey())
				.isEqualTo("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99");
		assertThat(firstRecord.getUserPrompt().getData())
				.containsEntry("duration", "3556")
				.containsEntry("heartRate", "194.63")
				.containsEntry("distance", "9.95")
				.containsEntry("stepCount", "10114");

		verify(rawDataRepository, times(1)).findAllByActor(actor);
		verify(objectMapper, times(1)).readValue(anyString(), any(TypeReference.class));
	}

	@Test
	@DisplayName("getUserPromtListByActor - 여러 세션의 러닝 데이터 병합")
	void getUserPromtListByActor_MultipleRunningSessions_Success() throws JsonProcessingException {
		// given
		AnalysisRawData session1 = new AnalysisRawData(actor, InputType.JSON, "running_style_analysis", runningDataJsonPayload);
		ReflectionTestUtils.setField(session1, "inputId", 1L);
		ReflectionTestUtils.setField(session1, "regDate", LocalDateTime.now().minusDays(2));

		AnalysisRawData session2 = new AnalysisRawData(actor, InputType.JSON, "running_style_analysis", runningDataJsonPayload);
		ReflectionTestUtils.setField(session2, "inputId", 2L);
		ReflectionTestUtils.setField(session2, "regDate", LocalDateTime.now().minusDays(1));

		AnalysisRawData session3 = new AnalysisRawData(actor, InputType.JSON, "running_style_analysis", runningDataJsonPayload);
		ReflectionTestUtils.setField(session3, "inputId", 3L);
		ReflectionTestUtils.setField(session3, "regDate", LocalDateTime.now());

		List<AnalysisRawData> rawDataList = Arrays.asList(session1, session2, session3);

		given(rawDataRepository.findAllByActor(actor))
				.willReturn(rawDataList);
		given(objectMapper.readValue(anyString(), any(TypeReference.class)))
				.willReturn(runningDataPrompts);

		// when
		InsightHistoryResponse response = analysisRawDataService.getUserPromtListByActor(actor);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getInsightRecords()).hasSize(15); // 3개 세션 * 5개 기록

		// 각 세션의 inputId 검증
		long session1Count = response.getInsightRecords().stream()
				.filter(r -> r.getInputId() == 1L)
				.count();
		long session2Count = response.getInsightRecords().stream()
				.filter(r -> r.getInputId() == 2L)
				.count();
		long session3Count = response.getInsightRecords().stream()
				.filter(r -> r.getInputId() == 3L)
				.count();

		assertThat(session1Count).isEqualTo(5);
		assertThat(session2Count).isEqualTo(5);
		assertThat(session3Count).isEqualTo(5);

		verify(rawDataRepository, times(1)).findAllByActor(actor);
		verify(objectMapper, times(3)).readValue(anyString(), any(TypeReference.class));
	}

	@Test
	@DisplayName("getUserPromtListByActor - Actor에 러닝 데이터가 없을 때 빈 리스트 반환")
	void getUserPromtListByActor_NoRunningData_ReturnsEmptyList() throws JsonProcessingException {
		// given
		given(rawDataRepository.findAllByActor(actor))
				.willReturn(Collections.emptyList());

		// when
		InsightHistoryResponse response = analysisRawDataService.getUserPromtListByActor(actor);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getResultCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getInsightRecords()).isEmpty();

		verify(rawDataRepository, times(1)).findAllByActor(actor);
		verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
	}

	@Test
	@DisplayName("getUserPromtListByActor - JSON 파싱 실패 시 InsightException 발생")
	void getUserPromtListByActor_JsonParsingFails_ThrowsInsightException() throws JsonProcessingException {
		// given
		List<AnalysisRawData> rawDataList = Arrays.asList(analysisRawData);

		given(rawDataRepository.findAllByActor(actor))
				.willReturn(rawDataList);
		given(objectMapper.readValue(anyString(), any(TypeReference.class)))
				.willThrow(new JsonProcessingException("Parsing error") {});

		// when & then
		assertThatThrownBy(() -> analysisRawDataService.getUserPromtListByActor(actor))
				.isInstanceOf(InsightException.class)
				.extracting(e -> ((InsightException) e).getError())
				.isEqualTo(InsightAppError.FAIL_JSON_PARSING_RAW_DATA);

		verify(rawDataRepository, times(1)).findAllByActor(actor);
		verify(objectMapper, times(1)).readValue(anyString(), any(TypeReference.class));
	}

	@Test
	@DisplayName("getUserPromtListByActor - 손상된 러닝 데이터 JSON 처리")
	void getUserPromtListByActor_CorruptedRunningDataJson_ThrowsInsightException()
			throws JsonProcessingException {
		// given
		String corruptedJson = "[{\"dataKey\":\"invalid\",\"data\":{\"duration\":";
		AnalysisRawData corruptedData = new AnalysisRawData(actor, InputType.JSON, "running_style_analysis", corruptedJson);
		ReflectionTestUtils.setField(corruptedData, "inputId", 99L);

		List<AnalysisRawData> rawDataList = Arrays.asList(corruptedData);

		given(rawDataRepository.findAllByActor(actor))
				.willReturn(rawDataList);
		given(objectMapper.readValue(eq(corruptedJson), any(TypeReference.class)))
				.willThrow(new JsonProcessingException("Unexpected end of JSON") {});

		// when & then
		assertThatThrownBy(() -> analysisRawDataService.getUserPromtListByActor(actor))
				.isInstanceOf(InsightException.class)
				.extracting(e -> ((InsightException) e).getError())
				.isEqualTo(InsightAppError.FAIL_JSON_PARSING_RAW_DATA);
	}

	@Test
	@DisplayName("getUserPromtListByActor - 다양한 거리와 시간의 러닝 데이터 처리")
	void getUserPromtListByActor_VariousRunningDistances_Success() throws JsonProcessingException {
		// given
		List<UserPrompt> variousDistancePrompts = Arrays.asList(
				UserPrompt.builder()
						.dataKey("SHORT-RUN")
						.data(createRunningData("942", "165.63", "3.26", "3320"))
						.build(),
				UserPrompt.builder()
						.dataKey("MEDIUM-RUN")
						.data(createRunningData("1965", "181.92", "6.01", "5702"))
						.build(),
				UserPrompt.builder()
						.dataKey("LONG-RUN")
						.data(createRunningData("3587", "184.75", "10.01", "10294"))
						.build()
		);

		List<AnalysisRawData> rawDataList = Arrays.asList(analysisRawData);

		given(rawDataRepository.findAllByActor(actor))
				.willReturn(rawDataList);
		given(objectMapper.readValue(anyString(), any(TypeReference.class)))
				.willReturn(variousDistancePrompts);

		// when
		InsightHistoryResponse response = analysisRawDataService.getUserPromtListByActor(actor);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getInsightRecords()).hasSize(3);

		// 다양한 거리 데이터 검증
		assertThat(response.getInsightRecords().get(0).getUserPrompt().getData())
				.containsEntry("distance", "3.26");
		assertThat(response.getInsightRecords().get(1).getUserPrompt().getData())
				.containsEntry("distance", "6.01");
		assertThat(response.getInsightRecords().get(2).getUserPrompt().getData())
				.containsEntry("distance", "10.01");
	}

	@Test
	@DisplayName("save - 빈 러닝 데이터 리스트 저장")
	void save_WithEmptyRunningDataList_Success() throws JsonProcessingException {
		// given
		List<UserPrompt> emptyList = Collections.emptyList();
		String emptyJson = "[]";
		AnalysisRawData emptyRawData = new AnalysisRawData(actor, InputType.JSON, "running_style_analysis", emptyJson);
		ReflectionTestUtils.setField(emptyRawData, "inputId", 2L);

		given(objectMapper.writeValueAsString(emptyList))
				.willReturn(emptyJson);
		given(rawDataRepository.save(any(AnalysisRawData.class)))
				.willReturn(emptyRawData);

		// when
		AnalysisRawData result = analysisRawDataService.save(actor, purpose, emptyList);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getRawPayload()).isEqualTo(emptyJson);
		verify(objectMapper, times(1)).writeValueAsString(emptyList);
		verify(rawDataRepository, times(1)).save(any(AnalysisRawData.class));
	}

	// Helper methods for creating test data
	private List<UserPrompt> createTenRunningDataPrompts() {
		return Arrays.asList(
				createUserPrompt("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99", "3556", "194.63", "9.95", "10114"),
				createUserPrompt("2FEC0793-820B-4F82-BBBA-951FB26B7455", "1965", "181.92", "6.01", "5702"),
				createUserPrompt("646500B9-A275-44F3-95BF-5EB19A41694A", "3587", "184.75", "10.01", "10294"),
				createUserPrompt("82A3ED54-13FF-4767-ACB1-45A3CBC60F6D", "1907", "174.76", "5.01", "5384"),
				createUserPrompt("2195E7C5-7E23-455B-9545-688F78521EDE", "1785", "182.31", "5.35", "5766"),
				createUserPrompt("7F2BDF2E-E68D-4AE3-A229-6D8697C72F06", "1805", "179.03", "5.18", "5202"),
				createUserPrompt("D631E366-26EA-458D-8A7A-0F1528AA305B", "1657", "184.76", "5.01", "4836"),
				createUserPrompt("1151D069-4EEE-4973-8BC9-16F2CA75DBF5", "942", "165.63", "3.26", "3320"),
				createUserPrompt("54C407BE-5198-4C77-8D82-B03D4D4A82E2", "2290", "172.95", "6.04", "6950"),
				createUserPrompt("E6B5B900-87D1-4DFA-A087-4630762D6B67", "1708", "176.57", "5.01", "5064")
		);
	}

	private UserPrompt createUserPrompt(String dataKey, String duration, String heartRate,
										String distance, String stepCount) {
		return UserPrompt.builder()
				.dataKey(dataKey)
				.data(createRunningData(duration, heartRate, distance, stepCount))
				.build();
	}

	private String createTenRecordsJson() {
		return "[" +
				"{\"dataKey\":\"A0398D47-38EB-4FEA-A8C2-34DF8E46DC99\",\"data\":{\"duration\":\"3556\",\"heartRate\":\"194.63\",\"distance\":\"9.95\",\"stepCount\":\"10114\"}}," +
				"{\"dataKey\":\"2FEC0793-820B-4F82-BBBA-951FB26B7455\",\"data\":{\"duration\":\"1965\",\"heartRate\":\"181.92\",\"distance\":\"6.01\",\"stepCount\":\"5702\"}}," +
				"{\"dataKey\":\"646500B9-A275-44F3-95BF-5EB19A41694A\",\"data\":{\"duration\":\"3587\",\"heartRate\":\"184.75\",\"distance\":\"10.01\",\"stepCount\":\"10294\"}}," +
				"{\"dataKey\":\"82A3ED54-13FF-4767-ACB1-45A3CBC60F6D\",\"data\":{\"duration\":\"1907\",\"heartRate\":\"174.76\",\"distance\":\"5.01\",\"stepCount\":\"5384\"}}," +
				"{\"dataKey\":\"2195E7C5-7E23-455B-9545-688F78521EDE\",\"data\":{\"duration\":\"1785\",\"heartRate\":\"182.31\",\"distance\":\"5.35\",\"stepCount\":\"5766\"}}," +
				"{\"dataKey\":\"7F2BDF2E-E68D-4AE3-A229-6D8697C72F06\",\"data\":{\"duration\":\"1805\",\"heartRate\":\"179.03\",\"distance\":\"5.18\",\"stepCount\":\"5202\"}}," +
				"{\"dataKey\":\"D631E366-26EA-458D-8A7A-0F1528AA305B\",\"data\":{\"duration\":\"1657\",\"heartRate\":\"184.76\",\"distance\":\"5.01\",\"stepCount\":\"4836\"}}," +
				"{\"dataKey\":\"1151D069-4EEE-4973-8BC9-16F2CA75DBF5\",\"data\":{\"duration\":\"942\",\"heartRate\":\"165.63\",\"distance\":\"3.26\",\"stepCount\":\"3320\"}}," +
				"{\"dataKey\":\"54C407BE-5198-4C77-8D82-B03D4D4A82E2\",\"data\":{\"duration\":\"2290\",\"heartRate\":\"172.95\",\"distance\":\"6.04\",\"stepCount\":\"6950\"}}," +
				"{\"dataKey\":\"E6B5B900-87D1-4DFA-A087-4630762D6B67\",\"data\":{\"duration\":\"1708\",\"heartRate\":\"176.57\",\"distance\":\"5.01\",\"stepCount\":\"5064\"}}" +
				"]";
	}
}