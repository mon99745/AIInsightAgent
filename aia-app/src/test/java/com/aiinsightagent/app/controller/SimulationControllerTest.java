package com.aiinsightagent.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationControllerTest {

	private SimulationController controller;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		controller = new SimulationController();
		objectMapper = new ObjectMapper();
	}

	@Nested
	@DisplayName("createUuid 테스트")
	class CreateUuidTest {

		@Test
		@DisplayName("유효한 UUID 형식의 문자열을 반환한다")
		void createUuid_returnsValidUuidFormat() {
			// when
			String result = controller.createUuid();

			// then
			assertThat(result).isNotNull();
			assertThat(UUID.fromString(result)).isNotNull();
		}

		@Test
		@DisplayName("매번 다른 UUID를 반환한다")
		void createUuid_returnsDifferentUuids() {
			// when
			String uuid1 = controller.createUuid();
			String uuid2 = controller.createUuid();

			// then
			assertThat(uuid1).isNotEqualTo(uuid2);
		}
	}

	@Nested
	@DisplayName("createInsightRequestMsg 테스트")
	class CreateInsightRequestMsgTest {

		@Test
		@DisplayName("유효한 JSON 형식을 반환한다")
		void createInsightRequestMsg_returnsValidJson() throws JsonProcessingException {
			// given
			String uuid = "test-uuid-123";

			// when
			String result = controller.createInsightRequestMsg(uuid);

			// then
			assertThat(result).isNotNull();
			JsonNode jsonNode = objectMapper.readTree(result);
			assertThat(jsonNode).isNotNull();
		}

		@Test
		@DisplayName("userId 필드에 전달된 UUID가 포함된다")
		void createInsightRequestMsg_containsUserId() throws JsonProcessingException {
			// given
			String uuid = "test-uuid-456";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			assertThat(jsonNode.get("userId").asText()).isEqualTo(uuid);
		}

		@Test
		@DisplayName("purpose 필드가 running_style_analysis로 설정된다")
		void createInsightRequestMsg_hasPurposeField() throws JsonProcessingException {
			// given
			String uuid = "test-uuid-789";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			assertThat(jsonNode.get("purpose").asText()).isEqualTo("running_style_analysis");
		}

		@Test
		@DisplayName("userPrompt 배열에 10개의 요소가 포함된다")
		void createInsightRequestMsg_contains10Prompts() throws JsonProcessingException {
			// given
			String uuid = "test-uuid";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			JsonNode userPrompt = jsonNode.get("userPrompt");
			assertThat(userPrompt.isArray()).isTrue();
			assertThat(userPrompt.size()).isEqualTo(10);
		}

		@Test
		@DisplayName("각 userPrompt 요소에 dataKey와 data 필드가 포함된다")
		void createInsightRequestMsg_promptHasRequiredFields() throws JsonProcessingException {
			// given
			String uuid = "test-uuid";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			JsonNode firstPrompt = jsonNode.get("userPrompt").get(0);
			assertThat(firstPrompt.has("dataKey")).isTrue();
			assertThat(firstPrompt.has("data")).isTrue();
			assertThat(firstPrompt.get("dataKey").asText()).isEqualTo("running_session_1");
		}

		@Test
		@DisplayName("data 필드에 duration, heartRate, distance, stepCount가 포함된다")
		void createInsightRequestMsg_dataHasRequiredFields() throws JsonProcessingException {
			// given
			String uuid = "test-uuid";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			JsonNode data = jsonNode.get("userPrompt").get(0).get("data");
			assertThat(data.has("duration")).isTrue();
			assertThat(data.has("heartRate")).isTrue();
			assertThat(data.has("distance")).isTrue();
			assertThat(data.has("stepCount")).isTrue();
		}

		@Test
		@DisplayName("duration 값이 900-3600 범위 내에 있다")
		void createInsightRequestMsg_durationInValidRange() throws JsonProcessingException {
			// given
			String uuid = "test-uuid";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			int duration = jsonNode.get("userPrompt").get(0).get("data").get("duration").asInt();
			assertThat(duration).isBetween(900, 3600);
		}

		@Test
		@DisplayName("heartRate 값이 160-195 범위 내에 있다")
		void createInsightRequestMsg_heartRateInValidRange() throws JsonProcessingException {
			// given
			String uuid = "test-uuid";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			long heartRate = jsonNode.get("userPrompt").get(0).get("data").get("heartRate").asLong();
			assertThat(heartRate).isBetween(160L, 195L);
		}

		@Test
		@DisplayName("stepCount 값이 3000-11000 범위 내에 있다")
		void createInsightRequestMsg_stepCountInValidRange() throws JsonProcessingException {
			// given
			String uuid = "test-uuid";

			// when
			String result = controller.createInsightRequestMsg(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			int stepCount = jsonNode.get("userPrompt").get(0).get("data").get("stepCount").asInt();
			assertThat(stepCount).isBetween(3000, 11000);
		}
	}

	@Nested
	@DisplayName("createContext 테스트")
	class CreateContextTest {

		@Test
		@DisplayName("유효한 JSON 형식을 반환한다")
		void createContext_returnsValidJson() throws JsonProcessingException {
			// given
			String uuid = "context-uuid-123";

			// when
			String result = controller.createContext(uuid);

			// then
			assertThat(result).isNotNull();
			JsonNode jsonNode = objectMapper.readTree(result);
			assertThat(jsonNode).isNotNull();
		}

		@Test
		@DisplayName("userId 필드에 전달된 UUID가 포함된다")
		void createContext_containsUserId() throws JsonProcessingException {
			// given
			String uuid = "context-uuid-456";

			// when
			String result = controller.createContext(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			assertThat(jsonNode.get("userId").asText()).isEqualTo(uuid);
		}

		@Test
		@DisplayName("category 필드가 user_profile로 설정된다")
		void createContext_hasCategoryField() throws JsonProcessingException {
			// given
			String uuid = "context-uuid-789";

			// when
			String result = controller.createContext(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			assertThat(jsonNode.get("category").asText()).isEqualTo("user_profile");
		}

		@Test
		@DisplayName("data 필드에 age, gender, height, weight가 포함된다")
		void createContext_dataHasRequiredFields() throws JsonProcessingException {
			// given
			String uuid = "context-uuid";

			// when
			String result = controller.createContext(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			JsonNode data = jsonNode.get("data");
			assertThat(data.has("age")).isTrue();
			assertThat(data.has("gender")).isTrue();
			assertThat(data.has("height")).isTrue();
			assertThat(data.has("weight")).isTrue();
		}

		@Test
		@DisplayName("age 값이 20-50 범위 내에 있다")
		void createContext_ageInValidRange() throws JsonProcessingException {
			// given
			String uuid = "context-uuid";

			// when
			String result = controller.createContext(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			int age = jsonNode.get("data").get("age").asInt();
			assertThat(age).isBetween(20, 50);
		}

		@Test
		@DisplayName("gender 값이 MALE 또는 FEMALE이다")
		void createContext_genderIsValid() throws JsonProcessingException {
			// given
			String uuid = "context-uuid";

			// when
			String result = controller.createContext(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			String gender = jsonNode.get("data").get("gender").asText();
			assertThat(gender).isIn("MALE", "FEMALE");
		}

		@Test
		@DisplayName("height 값이 160-180 범위 내에 있다")
		void createContext_heightInValidRange() throws JsonProcessingException {
			// given
			String uuid = "context-uuid";

			// when
			String result = controller.createContext(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			int height = jsonNode.get("data").get("height").asInt();
			assertThat(height).isBetween(160, 180);
		}

		@Test
		@DisplayName("weight 값이 55-80 범위 내에 있다")
		void createContext_weightInValidRange() throws JsonProcessingException {
			// given
			String uuid = "context-uuid";

			// when
			String result = controller.createContext(uuid);
			JsonNode jsonNode = objectMapper.readTree(result);

			// then
			int weight = jsonNode.get("data").get("weight").asInt();
			assertThat(weight).isBetween(55, 80);
		}
	}
}
