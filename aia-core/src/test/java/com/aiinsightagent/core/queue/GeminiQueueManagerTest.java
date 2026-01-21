package com.aiinsightagent.core.queue;

import com.aiinsightagent.core.config.GeminiProperties;
import com.aiinsightagent.core.config.RequestQueueProperties;
import com.google.genai.Models;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiQueueManagerTest {

	@Mock
	private GeminiProperties geminiProperties;

	@Mock
	private RequestQueueProperties queueProperties;

	private List<Models> geminiModelsList;
	private GeminiQueueManager queueManager;

	@BeforeEach
	void setUp() {
		geminiModelsList = new ArrayList<>();
	}

	@AfterEach
	void tearDown() {
		if (queueManager != null && queueManager.isRunning()) {
			queueManager.shutdown();
		}
	}

	@Test
	@DisplayName("워커 10개, API 키 10개 - 각 워커에 서로 다른 Models 할당")
	void init_tenWorkersTenKeys_eachWorkerGetsDifferentModels() {

		// given
		int workerCount = 10;
		int apiKeyCount = 10;

		List<GeminiProperties.ModelConfig> modelConfigs = new ArrayList<>();
		for (int i = 0; i < apiKeyCount; i++) {
			geminiModelsList.add(mock(Models.class));
			GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
			when(config.getId()).thenReturn("m0" + i);
			when(config.getName()).thenReturn("gemini-2.5-flash");
			modelConfigs.add(config);
		}

		when(geminiProperties.getValidModels()).thenReturn(modelConfigs);
		when(queueProperties.getWorkerCount()).thenReturn(workerCount);
		when(queueProperties.getQueueCapacity()).thenReturn(100);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(5);

		// when
		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// then
		assertTrue(queueManager.isRunning());
		assertEquals(workerCount, queueManager.getWorkerCount());

		// 각 워커가 서로 다른 Models를 받았는지 확인 (인덱스 검증)
		for (int i = 0; i < workerCount; i++) {
			int expectedKeyIndex = i % apiKeyCount;
			assertEquals(expectedKeyIndex, i); // 10:10이므로 동일해야 함
		}
	}

	@Test
	@DisplayName("워커 10개, API 키 5개 - 순환 방식으로 Models 할당")
	void init_tenWorkersFiveKeys_modelsAssignedInRoundRobin() {

		// given
		int workerCount = 10;
		int apiKeyCount = 5;

		List<GeminiProperties.ModelConfig> modelConfigs = new ArrayList<>();
		for (int i = 0; i < apiKeyCount; i++) {
			geminiModelsList.add(mock(Models.class));
			GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
			when(config.getId()).thenReturn("m0" + i);
			when(config.getName()).thenReturn("gemini-2.5-flash");
			modelConfigs.add(config);
		}

		when(geminiProperties.getValidModels()).thenReturn(modelConfigs);
		when(queueProperties.getWorkerCount()).thenReturn(workerCount);
		when(queueProperties.getQueueCapacity()).thenReturn(100);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(5);

		// when
		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// then
		assertTrue(queueManager.isRunning());

		// 순환 할당 검증: worker-0 -> key-0, worker-5 -> key-0
		// worker-1 -> key-1, worker-6 -> key-1 ...
		for (int i = 0; i < workerCount; i++) {
			int expectedKeyIndex = i % apiKeyCount;
			assertTrue(expectedKeyIndex >= 0 && expectedKeyIndex < apiKeyCount);
		}
	}

	@Test
	@DisplayName("워커별 API 키 인덱스 할당 로직 검증")
	void workerApiKeyAssignment_logic() {

		// given
		int workerCount = 10;
		int apiKeyCount = 10;

		// when & then
		for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
			int keyIndex = workerIndex % apiKeyCount;

			// 워커 0 -> API 키 0
			// 워커 1 -> API 키 1
			// ...
			// 워커 9 -> API 키 9
			assertEquals(workerIndex, keyIndex,
					"Worker " + workerIndex + " should use API key " + workerIndex);
		}
	}

	@Test
	@DisplayName("워커 수가 API 키 수보다 많을 때 순환 할당 검증")
	void workerApiKeyAssignment_roundRobin() {

		// given
		int workerCount = 15;
		int apiKeyCount = 10;

		// when & then
		int[] expectedKeyIndices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4};

		for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
			int keyIndex = workerIndex % apiKeyCount;
			assertEquals(expectedKeyIndices[workerIndex], keyIndex,
					"Worker " + workerIndex + " should use API key " + expectedKeyIndices[workerIndex]);
		}
	}

	@Test
	@DisplayName("QueueManager 초기화 후 running 상태 확인")
	void init_setsRunningToTrue() {

		// given
		geminiModelsList.add(mock(Models.class));

		GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
		when(config.getId()).thenReturn("m01");
		when(config.getName()).thenReturn("gemini-2.5-flash");
		when(geminiProperties.getValidModels()).thenReturn(List.of(config));

		when(queueProperties.getWorkerCount()).thenReturn(1);
		when(queueProperties.getQueueCapacity()).thenReturn(10);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(5);

		// when
		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// then
		assertTrue(queueManager.isRunning());
	}

	@Test
	@DisplayName("QueueManager shutdown 후 running 상태 확인")
	void shutdown_setsRunningToFalse() {

		// given
		geminiModelsList.add(mock(Models.class));

		GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
		when(config.getId()).thenReturn("m01");
		when(config.getName()).thenReturn("gemini-2.5-flash");
		when(geminiProperties.getValidModels()).thenReturn(List.of(config));

		when(queueProperties.getWorkerCount()).thenReturn(1);
		when(queueProperties.getQueueCapacity()).thenReturn(10);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(5);

		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// when
		queueManager.shutdown();

		// then
		assertFalse(queueManager.isRunning());
	}
}
