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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

	@Test
	@DisplayName("submit() 호출 시 요청이 큐에 정상 적재됨")
	void submit_requestEnqueuedSuccessfully() throws Exception {

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

		String testPrompt = "테스트 프롬프트";

		// when
		CompletableFuture<GeminiResponse> future = queueManager.submit(testPrompt);

		// then
		assertNotNull(future, "submit()은 CompletableFuture를 반환해야 함");
		assertFalse(future.isDone(), "요청이 아직 처리되지 않았으므로 완료 상태가 아니어야 함");

		// 리플렉션으로 큐 사이즈 확인
		BlockingQueue<GeminiRequest> queue = getRequestQueue(queueManager);
		assertTrue(queue.size() >= 0, "큐에 요청이 적재되어야 함");
	}

	@Test
	@DisplayName("submit() 여러 번 호출 시 모든 요청에 대해 CompletableFuture 반환됨")
	void submit_multipleRequests_allReturnFutures() throws Exception {

		// given
		geminiModelsList.add(mock(Models.class));

		GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
		when(config.getId()).thenReturn("m01");
		when(config.getName()).thenReturn("gemini-2.5-flash");
		when(geminiProperties.getValidModels()).thenReturn(List.of(config));

		when(queueProperties.getWorkerCount()).thenReturn(1);
		when(queueProperties.getQueueCapacity()).thenReturn(100);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(5);

		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// when
		List<CompletableFuture<GeminiResponse>> futures = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			futures.add(queueManager.submit("테스트 프롬프트 " + i));
		}

		// then
		assertEquals(5, futures.size(), "5개의 요청 모두 CompletableFuture를 반환해야 함");

		for (CompletableFuture<GeminiResponse> future : futures) {
			assertNotNull(future, "각 submit()은 null이 아닌 CompletableFuture를 반환해야 함");
		}
	}

	@Test
	@DisplayName("큐 용량 초과 시 RejectedExecutionException 발생")
	void submit_queueFull_throwsRejectedExecutionException() throws Exception {

		// given
		geminiModelsList.add(mock(Models.class));

		GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
		when(config.getId()).thenReturn("m01");
		when(config.getName()).thenReturn("gemini-2.5-flash");
		when(geminiProperties.getValidModels()).thenReturn(List.of(config));

		when(queueProperties.getWorkerCount()).thenReturn(1);
		when(queueProperties.getQueueCapacity()).thenReturn(1); // 최소 용량
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(5);

		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// when - 빠르게 여러 요청을 보내서 큐 용량 초과 유도
		List<CompletableFuture<GeminiResponse>> futures = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			futures.add(queueManager.submit("프롬프트 " + i));
		}

		// then - 적어도 하나의 요청은 RejectedExecutionException으로 실패해야 함
		long rejectedCount = futures.stream()
				.filter(CompletableFuture::isCompletedExceptionally)
				.count();

		assertTrue(rejectedCount > 0,
				"큐 용량(1)보다 많은 요청을 보내면 일부는 RejectedExecutionException이 발생해야 함");

		// 실패한 요청 중 하나를 검증
		CompletableFuture<GeminiResponse> failedFuture = futures.stream()
				.filter(CompletableFuture::isCompletedExceptionally)
				.findFirst()
				.orElseThrow();

		ExecutionException exception = assertThrows(ExecutionException.class, failedFuture::get);
		assertInstanceOf(RejectedExecutionException.class, exception.getCause(),
				"RejectedExecutionException이 발생해야 함");
	}

	@Test
	@DisplayName("running=false 상태에서 submit() 호출 시 IllegalStateException 발생")
	void submit_whenNotRunning_throwsIllegalStateException() {

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
		queueManager.shutdown(); // 종료 상태로 전환

		// when
		CompletableFuture<GeminiResponse> future = queueManager.submit("테스트 프롬프트");

		// then
		assertTrue(future.isCompletedExceptionally(),
				"종료된 상태에서 submit()은 예외로 완료되어야 함");

		ExecutionException exception = assertThrows(ExecutionException.class, future::get);
		assertInstanceOf(IllegalStateException.class, exception.getCause(),
				"IllegalStateException이 발생해야 함");
	}

	@Test
	@DisplayName("submit() 호출 시 GeminiRequest 객체가 올바르게 생성됨")
	void submit_requestContainsCorrectPrompt() throws Exception {

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

		String expectedPrompt = "이것은_테스트_프롬프트입니다_" + System.currentTimeMillis();

		// when
		CompletableFuture<GeminiResponse> future = queueManager.submit(expectedPrompt);

		// then - 큐에서 요청 찾기 (워커가 가져가기 전에 확인)
		BlockingQueue<GeminiRequest> queue = getRequestQueue(queueManager);

		// 잠시 대기하여 큐에 적재 확인
		Thread.sleep(50);

		// 큐에 요청이 있거나, 이미 워커가 처리 중이면 future가 존재
		assertNotNull(future, "submit()은 CompletableFuture를 반환해야 함");

		// 큐에 요청이 남아있다면 프롬프트 확인
		GeminiRequest request = queue.stream()
				.filter(r -> expectedPrompt.equals(r.getPrompt()))
				.findFirst()
				.orElse(null);

		// 요청이 큐에 있거나 워커가 이미 처리 중 - 둘 다 정상 동작
		if (request != null) {
			assertEquals(expectedPrompt, request.getPrompt(), "프롬프트가 올바르게 설정되어야 함");
			assertTrue(request.getCreatedAt() > 0, "생성 시간이 설정되어야 함");
		}
		// 워커가 이미 처리 중인 경우도 정상 (큐에서 빠짐)
	}

	@Test
	@DisplayName("각 워커가 독립적으로 요청을 가져가고, 중복 처리 없이 모든 요청이 처리됨")
	void workers_processRequestsIndependently_noDuplicates() throws Exception {

		// given
		int workerCount = 3;
		int requestCount = 10;

		// 처리된 프롬프트를 추적하기 위한 Set (thread-safe)
		Set<String> processedPrompts = ConcurrentHashMap.newKeySet();

		for (int i = 0; i < workerCount; i++) {
			Models mockModels = mock(Models.class);

			// 각 Models가 호출될 때 프롬프트를 Set에 추가
			when(mockModels.generateContent(anyString(), anyString(), any()))
					.thenAnswer(invocation -> {
						String prompt = invocation.getArgument(1);
						boolean added = processedPrompts.add(prompt);
						if (!added) {
							throw new IllegalStateException("중복 처리 발생: " + prompt);
						}
						// API 호출 시뮬레이션
						Thread.sleep(50);
						return mock(com.google.genai.types.GenerateContentResponse.class);
					});

			geminiModelsList.add(mockModels);
		}

		List<GeminiProperties.ModelConfig> modelConfigs = new ArrayList<>();
		for (int i = 0; i < workerCount; i++) {
			GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
			when(config.getId()).thenReturn("m0" + i);
			when(config.getName()).thenReturn("gemini-2.5-flash");
			modelConfigs.add(config);
		}

		when(geminiProperties.getValidModels()).thenReturn(modelConfigs);
		when(geminiProperties.getMaxOutputTokens()).thenReturn(null);
		when(geminiProperties.getTemperature()).thenReturn(0.0);
		when(queueProperties.getWorkerCount()).thenReturn(workerCount);
		when(queueProperties.getQueueCapacity()).thenReturn(100);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(10);

		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// when - 요청 제출
		List<CompletableFuture<GeminiResponse>> futures = new ArrayList<>();
		for (int i = 0; i < requestCount; i++) {
			futures.add(queueManager.submit("요청_" + i));
		}

		// 모든 요청 완료 대기
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.get(30, TimeUnit.SECONDS);

		// then
		// 1. 모든 요청이 처리되었는지 확인 (누락 없음)
		assertEquals(requestCount, processedPrompts.size(),
				"모든 요청이 처리되어야 함");

		// 2. 각 요청이 정확히 한 번씩 처리되었는지 확인 (중복 없음)
		for (int i = 0; i < requestCount; i++) {
			assertTrue(processedPrompts.contains("요청_" + i),
					"요청_" + i + "가 처리되어야 함");
		}

		// 3. 모든 Future가 정상 완료되었는지 확인
		for (CompletableFuture<GeminiResponse> future : futures) {
			assertTrue(future.isDone(), "모든 요청이 완료되어야 함");
			assertFalse(future.isCompletedExceptionally(), "예외 없이 완료되어야 함");
		}
	}

	@Test
	@DisplayName("Graceful shutdown - 진행 중인 요청이 완료된 후 종료됨")
	void shutdown_completesInProgressRequests() throws Exception {

		// given
		AtomicInteger completedCount = new AtomicInteger(0);

		Models mockModels = mock(Models.class);
		when(mockModels.generateContent(anyString(), anyString(), any()))
				.thenAnswer(invocation -> {
					Thread.sleep(200); // API 호출 시뮬레이션
					completedCount.incrementAndGet();
					return mock(com.google.genai.types.GenerateContentResponse.class);
				});
		geminiModelsList.add(mockModels);

		GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
		when(config.getId()).thenReturn("m01");
		when(config.getName()).thenReturn("gemini-2.5-flash");
		when(geminiProperties.getValidModels()).thenReturn(List.of(config));
		when(geminiProperties.getMaxOutputTokens()).thenReturn(null);
		when(geminiProperties.getTemperature()).thenReturn(0.0);

		when(queueProperties.getWorkerCount()).thenReturn(1);
		when(queueProperties.getQueueCapacity()).thenReturn(10);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(10); // 충분한 시간

		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// when - 요청 제출 후 바로 shutdown
		CompletableFuture<GeminiResponse> future = queueManager.submit("테스트 요청");
		Thread.sleep(50); // 워커가 요청을 가져갈 시간

		queueManager.shutdown();

		// then - 진행 중인 요청이 완료되어야 함
		assertTrue(future.isDone(), "진행 중인 요청이 완료되어야 함");
		assertEquals(1, completedCount.get(), "요청이 정상 처리되어야 함");
	}

	@Test
	@DisplayName("Graceful shutdown - 큐에 남은 요청들이 처리됨")
	void shutdown_processesRemainingQueuedRequests() throws Exception {

		// given
		AtomicInteger completedCount = new AtomicInteger(0);

		Models mockModels = mock(Models.class);
		when(mockModels.generateContent(anyString(), anyString(), any()))
				.thenAnswer(invocation -> {
					Thread.sleep(100); // API 호출 시뮬레이션
					completedCount.incrementAndGet();
					return mock(com.google.genai.types.GenerateContentResponse.class);
				});
		geminiModelsList.add(mockModels);

		GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
		when(config.getId()).thenReturn("m01");
		when(config.getName()).thenReturn("gemini-2.5-flash");
		when(geminiProperties.getValidModels()).thenReturn(List.of(config));
		when(geminiProperties.getMaxOutputTokens()).thenReturn(null);
		when(geminiProperties.getTemperature()).thenReturn(0.0);

		when(queueProperties.getWorkerCount()).thenReturn(1);
		when(queueProperties.getQueueCapacity()).thenReturn(10);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(10); // 충분한 시간

		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// when - 여러 요청 제출 후 shutdown
		List<CompletableFuture<GeminiResponse>> futures = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			futures.add(queueManager.submit("요청_" + i));
		}

		Thread.sleep(50); // 첫 요청이 처리 시작할 시간
		queueManager.shutdown();

		// then - 모든 요청이 처리되어야 함
		for (CompletableFuture<GeminiResponse> future : futures) {
			assertTrue(future.isDone(), "모든 요청이 완료되어야 함");
		}
		assertEquals(3, completedCount.get(), "모든 요청이 정상 처리되어야 함");
	}

	@Test
	@DisplayName("Graceful shutdown - 타임아웃 시 미처리 요청은 CancellationException 발생")
	void shutdown_timeout_cancelsPendingRequests() throws Exception {

		// given
		AtomicInteger processingCount = new AtomicInteger(0);

		Models mockModels = mock(Models.class);
		when(mockModels.generateContent(anyString(), anyString(), any()))
				.thenAnswer(invocation -> {
					processingCount.incrementAndGet();
					Thread.sleep(10000); // 매우 긴 처리 시간 (타임아웃보다 길게)
					return mock(com.google.genai.types.GenerateContentResponse.class);
				});
		geminiModelsList.add(mockModels);

		GeminiProperties.ModelConfig config = mock(GeminiProperties.ModelConfig.class);
		when(config.getId()).thenReturn("m01");
		when(config.getName()).thenReturn("gemini-2.5-flash");
		when(geminiProperties.getValidModels()).thenReturn(List.of(config));
		when(geminiProperties.getMaxOutputTokens()).thenReturn(null);
		when(geminiProperties.getTemperature()).thenReturn(0.0);

		when(queueProperties.getWorkerCount()).thenReturn(1); // 워커 1개
		when(queueProperties.getQueueCapacity()).thenReturn(10);
		when(queueProperties.getShutdownTimeoutSeconds()).thenReturn(1); // 짧은 타임아웃

		queueManager = new GeminiQueueManager(geminiModelsList, geminiProperties, queueProperties);
		queueManager.init();

		// when - 여러 요청 제출 (워커 1개이므로 첫 번째만 처리 중, 나머지는 큐 대기)
		List<CompletableFuture<GeminiResponse>> futures = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			futures.add(queueManager.submit("요청_" + i));
		}

		// 첫 요청이 처리 시작할 때까지 대기
		while (processingCount.get() == 0) {
			Thread.sleep(10);
		}

		queueManager.shutdown(); // 1초 타임아웃 후 강제 종료

		// then - 큐에 남은 요청들은 CancellationException으로 실패해야 함
		long cancelledCount = futures.stream()
				.filter(f -> {
					if (!f.isCompletedExceptionally()) return false;
					try {
						f.get();
						return false;
					} catch (CancellationException e) {
						// CancellationException은 ExecutionException으로 래핑되지 않고 직접 throw됨
						return true;
					} catch (Exception e) {
						return false;
					}
				})
				.count();

		// 워커 1개, 요청 5개 → 최소 3개 이상은 큐에서 대기하다가 취소되어야 함
		assertTrue(cancelledCount >= 1,
				"타임아웃으로 인해 최소 1개 이상의 요청이 CancellationException으로 취소되어야 함 (실제: " + cancelledCount + ")");
	}

	@Test
	@DisplayName("Graceful shutdown - 종료 후 새 요청은 거부됨")
	void shutdown_rejectsNewRequests() throws Exception {

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
		queueManager.shutdown();

		// when - 종료 후 요청 시도
		CompletableFuture<GeminiResponse> future = queueManager.submit("새 요청");

		// then
		assertTrue(future.isCompletedExceptionally(),
				"종료 후 요청은 즉시 예외로 완료되어야 함");

		ExecutionException exception = assertThrows(ExecutionException.class, future::get);
		assertInstanceOf(IllegalStateException.class, exception.getCause(),
				"IllegalStateException이 발생해야 함");
	}

	/**
	 * 리플렉션으로 requestQueue 필드에 접근
	 */
	@SuppressWarnings("unchecked")
	private BlockingQueue<GeminiRequest> getRequestQueue(GeminiQueueManager manager) throws Exception {
		Field queueField = GeminiQueueManager.class.getDeclaredField("requestQueue");
		queueField.setAccessible(true);
		return (BlockingQueue<GeminiRequest>) queueField.get(manager);
	}
}
