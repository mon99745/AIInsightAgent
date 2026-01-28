package com.aiinsightagent.app.controller;

import com.aiinsightagent.app.service.InsightService;
import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("동시성 및 확장성 테스트")
class ConcurrencyScalabilityTest {

	@Mock
	private InsightService insightService;

	@InjectMocks
	private InsightController controller;

	private InsightRequest createTestRequest(String userId) {
		UserPrompt prompt = UserPrompt.builder()
				.dataKey("test_data_1")
				.data(Map.of("value", "100"))
				.build();

		return InsightRequest.builder()
				.userId(userId)
				.purpose("test_analysis")
				.userPrompt(List.of(prompt))
				.build();
	}

	private InsightResponse createMockResponse(String userId) {
		InsightDetail detail = InsightDetail.builder()
				.summary("Test summary for " + userId)
				.priorityScore(1)
				.build();

		return InsightResponse.builder()
				.resultCode(200)
				.resultMsg("SUCCESS")
				.insight(detail)
				.build();
	}

	@Nested
	@DisplayName("동시 요청 처리 안전성 테스트")
	class ConcurrentRequestSafetyTest {

		@Test
		@DisplayName("10개의 동시 요청이 모두 성공적으로 처리된다")
		void concurrentRequests_10threads_allSucceed() throws Exception {
			// given
			int threadCount = 10;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(threadCount);

			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failCount = new AtomicInteger(0);
			List<String> errors = Collections.synchronizedList(new ArrayList<>());

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						Thread.sleep(50); // 실제 처리 시뮬레이션
						return createMockResponse(req.getUserId());
					});

			// when
			for (int i = 0; i < threadCount; i++) {
				final int threadId = i;
				executor.submit(() -> {
					try {
						startLatch.await(); // 모든 스레드가 동시에 시작

						String userId = "user-" + threadId;
						InsightRequest request = createTestRequest(userId);
						InsightResponse response = controller.analysis(request);

						if (response != null && response.getResultCode() == 200) {
							successCount.incrementAndGet();
						} else {
							failCount.incrementAndGet();
							errors.add("Thread " + threadId + ": Invalid response");
						}
					} catch (Exception e) {
						failCount.incrementAndGet();
						errors.add("Thread " + threadId + ": " + e.getMessage());
					} finally {
						endLatch.countDown();
					}
				});
			}

			startLatch.countDown(); // 동시 시작
			boolean completed = endLatch.await(30, TimeUnit.SECONDS);

			// then
			executor.shutdown();
			assertThat(completed).isTrue();
			assertThat(successCount.get()).isEqualTo(threadCount);
			assertThat(failCount.get()).isZero();
			assertThat(errors).isEmpty();

			System.out.println("\n=== 동시 요청 테스트 결과 ===");
			System.out.printf("스레드 수: %d, 성공: %d, 실패: %d\n",
					threadCount, successCount.get(), failCount.get());
		}

		@Test
		@DisplayName("동시 요청 시 각 요청이 독립적으로 처리된다")
		void concurrentRequests_isolation_maintained() throws Exception {
			// given
			int threadCount = 10;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(threadCount);

			ConcurrentHashMap<String, Boolean> processedUsers = new ConcurrentHashMap<>();
			AtomicInteger duplicateCount = new AtomicInteger(0);

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						String userId = req.getUserId();

						// 중복 처리 감지
						if (processedUsers.putIfAbsent(userId, true) != null) {
							duplicateCount.incrementAndGet();
						}

						Thread.sleep(30 + (int)(Math.random() * 50));
						return createMockResponse(userId);
					});

			// when
			for (int i = 0; i < threadCount; i++) {
				final String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
				executor.submit(() -> {
					try {
						startLatch.await();
						InsightRequest request = createTestRequest(userId);
						controller.analysis(request);
					} catch (Exception e) {
						// ignore
					} finally {
						endLatch.countDown();
					}
				});
			}

			startLatch.countDown();
			endLatch.await(30, TimeUnit.SECONDS);
			executor.shutdown();

			// then
			assertThat(processedUsers).hasSize(threadCount);
			assertThat(duplicateCount.get()).isZero();

			System.out.println("\n=== 요청 독립성 테스트 결과 ===");
			System.out.printf("처리된 고유 사용자: %d, 중복 처리: %d\n",
					processedUsers.size(), duplicateCount.get());
		}
	}

	@Nested
	@DisplayName("워커 확장성 테스트")
	class WorkerScalabilityTest {

		@Test
		@DisplayName("워커 수 증가 시 처리량이 선형적으로 증가한다")
		void workerScaling_linearThroughput() throws Exception {
			// given
			int[] workerCounts = {1, 2, 4, 8};
			int requestsPerWorker = 5;
			Map<Integer, Long> throughputResults = new ConcurrentHashMap<>();
			Map<Integer, Long> durationResults = new ConcurrentHashMap<>();

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						Thread.sleep(20); // 고정 처리 시간
						return createMockResponse(req.getUserId());
					});

			// when
			for (int workerCount : workerCounts) {
				int totalRequests = workerCount * requestsPerWorker;
				ExecutorService executor = Executors.newFixedThreadPool(workerCount);

				List<Callable<InsightResponse>> tasks = new ArrayList<>();
				for (int i = 0; i < totalRequests; i++) {
					final int taskId = i;
					tasks.add(() -> {
						InsightRequest request = createTestRequest("user-" + taskId);
						return controller.analysis(request);
					});
				}

				long startTime = System.currentTimeMillis();
				List<Future<InsightResponse>> futures = executor.invokeAll(tasks);

				for (Future<InsightResponse> future : futures) {
					future.get();
				}

				long endTime = System.currentTimeMillis();
				long duration = endTime - startTime;

				long throughput = (totalRequests * 1000) / Math.max(duration, 1);
				throughputResults.put(workerCount, throughput);
				durationResults.put(workerCount, duration);

				executor.shutdown();
			}

			// then
			System.out.println("\n=== 워커 확장성 테스트 결과 ===");
			System.out.println("워커 수\t요청 수\t소요시간(ms)\t처리량(req/s)\t효율성");

			long baselineThroughput = throughputResults.get(1);
			boolean isLinearlyScalable = true;

			for (int workerCount : workerCounts) {
				int totalRequests = workerCount * requestsPerWorker;
				long throughput = throughputResults.get(workerCount);
				long duration = durationResults.get(workerCount);
				double efficiency = (double) throughput / (baselineThroughput * workerCount) * 100;

				System.out.printf("%d\t%d\t%d\t\t%d\t\t%.1f%%\n",
						workerCount, totalRequests, duration, throughput, efficiency);

				// 효율성이 50% 미만이면 선형 확장 실패
				if (workerCount > 1 && efficiency < 50) {
					isLinearlyScalable = false;
				}
			}

			System.out.println("\n선형 확장 가능 여부: " + (isLinearlyScalable ? "✅ 예" : "❌ 아니오"));

			// 워커 2배 시 처리량 최소 1.5배 증가 검증
			assertThat(throughputResults.get(2))
					.describedAs("워커 2배 시 처리량 증가")
					.isGreaterThanOrEqualTo((long)(baselineThroughput * 1.5));
		}

		@Test
		@DisplayName("워커 수 증가 시 평균 응답 시간이 안정적으로 유지된다")
		void workerScaling_stableResponseTime() throws Exception {
			// given
			int[] workerCounts = {1, 5, 10};
			int totalRequests = 20;
			Map<Integer, Double> avgResponseTimes = new ConcurrentHashMap<>();
			Map<Integer, Double> maxResponseTimes = new ConcurrentHashMap<>();

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						Thread.sleep(30);
						return createMockResponse(req.getUserId());
					});

			// when
			for (int workerCount : workerCounts) {
				ExecutorService executor = Executors.newFixedThreadPool(workerCount);
				List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
				CountDownLatch latch = new CountDownLatch(totalRequests);

				for (int i = 0; i < totalRequests; i++) {
					final int taskId = i;
					executor.submit(() -> {
						try {
							long start = System.currentTimeMillis();
							InsightRequest request = createTestRequest("user-" + taskId);
							controller.analysis(request);
							long end = System.currentTimeMillis();
							responseTimes.add(end - start);
						} finally {
							latch.countDown();
						}
					});
				}

				latch.await(60, TimeUnit.SECONDS);

				double avgTime = responseTimes.stream()
						.mapToLong(Long::longValue)
						.average()
						.orElse(0);
				double maxTime = responseTimes.stream()
						.mapToLong(Long::longValue)
						.max()
						.orElse(0);

				avgResponseTimes.put(workerCount, avgTime);
				maxResponseTimes.put(workerCount, maxTime);

				executor.shutdown();
			}

			// then
			System.out.println("\n=== 응답 시간 안정성 테스트 결과 ===");
			System.out.println("워커 수\t평균 응답시간(ms)\t최대 응답시간(ms)");

			for (int workerCount : workerCounts) {
				System.out.printf("%d\t%.2f\t\t\t%.2f\n",
						workerCount,
						avgResponseTimes.get(workerCount),
						maxResponseTimes.get(workerCount));
			}

			// 워커 증가 시 평균 응답 시간이 급격히 증가하지 않아야 함
			double baselineTime = avgResponseTimes.get(1);
			for (int workerCount : workerCounts) {
				assertThat(avgResponseTimes.get(workerCount))
						.describedAs("워커 %d개 시 응답 시간 안정성", workerCount)
						.isLessThan(baselineTime * 3);
			}
		}
	}

	@Nested
	@DisplayName("스레드 안전성 테스트")
	class ThreadSafetyTest {

		@Test
		@DisplayName("동시 요청 시 데이터 경합 없이 모든 요청이 처리된다")
		void noDataRace_allRequestsProcessed() throws Exception {
			// given
			int threadCount = 10;
			int iterationsPerThread = 50;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);

			AtomicInteger totalRequests = new AtomicInteger(0);
			AtomicInteger totalSuccesses = new AtomicInteger(0);
			List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						return createMockResponse(req.getUserId());
					});

			// when
			List<Future<?>> futures = new ArrayList<>();
			for (int t = 0; t < threadCount; t++) {
				final int threadId = t;
				futures.add(executor.submit(() -> {
					for (int i = 0; i < iterationsPerThread; i++) {
						try {
							totalRequests.incrementAndGet();
							InsightRequest request = createTestRequest("user-" + threadId + "-" + i);
							InsightResponse response = controller.analysis(request);
							if (response != null && response.getResultCode() == 200) {
								totalSuccesses.incrementAndGet();
							}
						} catch (Exception e) {
							exceptions.add(e);
						}
					}
				}));
			}

			for (Future<?> future : futures) {
				future.get(60, TimeUnit.SECONDS);
			}

			executor.shutdown();

			// then
			int expectedTotal = threadCount * iterationsPerThread;

			System.out.println("\n=== 스레드 안전성 테스트 결과 ===");
			System.out.printf("총 요청: %d, 성공: %d, 예외: %d\n",
					totalRequests.get(), totalSuccesses.get(), exceptions.size());

			assertThat(exceptions).isEmpty();
			assertThat(totalRequests.get()).isEqualTo(expectedTotal);
			assertThat(totalSuccesses.get()).isEqualTo(expectedTotal);
		}
	}
}
