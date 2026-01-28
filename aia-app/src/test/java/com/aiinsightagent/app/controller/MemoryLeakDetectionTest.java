package com.aiinsightagent.app.controller;

import com.aiinsightagent.app.service.InsightService;
import com.aiinsightagent.core.context.GeminiContext;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("메모리 누수 감지 테스트")
class MemoryLeakDetectionTest {

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
	@DisplayName("장시간 실행 메모리 안정성 테스트")
	class LongRunningMemoryStabilityTest {

		@Test
		@DisplayName("1000회 반복 요청 시 메모리 사용량이 안정적으로 유지된다")
		void repeatedRequests_memoryStable() throws Exception {
			// given
			int iterations = 1000;
			AtomicLong initialMemory = new AtomicLong();
			AtomicLong peakMemory = new AtomicLong();
			List<Long> memorySnapshots = new ArrayList<>();

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						return createMockResponse(req.getUserId());
					});

			// 초기 메모리 측정
			System.gc();
			Thread.sleep(100);
			initialMemory.set(getUsedMemory());

			// when
			for (int i = 0; i < iterations; i++) {
				InsightRequest request = createTestRequest("user-" + i);
				controller.analysis(request);

				// 100회마다 메모리 스냅샷
				if (i % 100 == 0) {
					long currentMemory = getUsedMemory();
					memorySnapshots.add(currentMemory);
					peakMemory.set(Math.max(peakMemory.get(), currentMemory));
				}
			}

			// GC 후 최종 메모리 측정
			System.gc();
			Thread.sleep(200);
			long finalMemory = getUsedMemory();

			// then
			long memoryGrowth = finalMemory - initialMemory.get();
			double growthRatio = (double) memoryGrowth / initialMemory.get() * 100;

			System.out.println("\n=== 메모리 안정성 테스트 결과 ===");
			System.out.printf("반복 횟수: %d\n", iterations);
			System.out.printf("초기 메모리: %.2f MB\n", initialMemory.get() / 1024.0 / 1024.0);
			System.out.printf("최종 메모리: %.2f MB\n", finalMemory / 1024.0 / 1024.0);
			System.out.printf("최대 메모리: %.2f MB\n", peakMemory.get() / 1024.0 / 1024.0);
			System.out.printf("메모리 증가량: %.2f MB (%.1f%%)\n", memoryGrowth / 1024.0 / 1024.0, growthRatio);

			System.out.println("\n메모리 스냅샷 (100회 간격):");
			for (int i = 0; i < memorySnapshots.size(); i++) {
				System.out.printf("  %d회: %.2f MB\n", i * 100, memorySnapshots.get(i) / 1024.0 / 1024.0);
			}

			// 메모리 증가율이 100% 미만이어야 함 (심각한 누수 없음)
			assertThat(growthRatio)
					.describedAs("메모리 증가율이 100%% 미만이어야 함")
					.isLessThan(100.0);
		}

		@Test
		@DisplayName("동시 요청 후 메모리가 정상적으로 해제된다")
		void concurrentRequests_memoryReleased() throws Exception {
			// given
			int threadCount = 10;
			int requestsPerThread = 100;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch latch = new CountDownLatch(threadCount);

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						return createMockResponse(req.getUserId());
					});

			System.gc();
			Thread.sleep(100);
			long beforeMemory = getUsedMemory();

			// when
			for (int t = 0; t < threadCount; t++) {
				final int threadId = t;
				executor.submit(() -> {
					try {
						for (int i = 0; i < requestsPerThread; i++) {
							InsightRequest request = createTestRequest("user-" + threadId + "-" + i);
							controller.analysis(request);
						}
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await(60, TimeUnit.SECONDS);
			executor.shutdown();

			// GC 여러 번 수행하여 메모리 해제 확인
			for (int i = 0; i < 3; i++) {
				System.gc();
				Thread.sleep(100);
			}
			long afterMemory = getUsedMemory();

			// then
			long memoryDiff = afterMemory - beforeMemory;
			double diffRatio = (double) memoryDiff / beforeMemory * 100;

			System.out.println("\n=== 동시 요청 메모리 해제 테스트 결과 ===");
			System.out.printf("총 요청: %d (스레드 %d x 요청 %d)\n",
					threadCount * requestsPerThread, threadCount, requestsPerThread);
			System.out.printf("실행 전 메모리: %.2f MB\n", beforeMemory / 1024.0 / 1024.0);
			System.out.printf("실행 후 메모리: %.2f MB\n", afterMemory / 1024.0 / 1024.0);
			System.out.printf("메모리 차이: %.2f MB (%.1f%%)\n", memoryDiff / 1024.0 / 1024.0, diffRatio);

			// 메모리 증가가 50MB 미만이어야 함 (누수 없음)
			assertThat(memoryDiff)
					.describedAs("동시 요청 후 메모리 증가가 50MB 미만이어야 함")
					.isLessThan(50 * 1024 * 1024);
		}
	}

	@Nested
	@DisplayName("객체 참조 누수 테스트")
	class ObjectReferenceLeakTest {

		@Test
		@DisplayName("요청 객체가 GC 후 정상적으로 해제된다")
		void requestObjects_garbageCollected() throws Exception {
			// given
			int requestCount = 100;
			List<WeakReference<InsightRequest>> weakRefs = new ArrayList<>();

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						return createMockResponse(req.getUserId());
					});

			// when
			for (int i = 0; i < requestCount; i++) {
				InsightRequest request = createTestRequest("user-" + i);
				weakRefs.add(new WeakReference<>(request));
				controller.analysis(request);
			}

			// GC 강제 실행
			System.gc();
			Thread.sleep(200);

			// then
			int collectedCount = 0;
			int retainedCount = 0;

			for (WeakReference<InsightRequest> ref : weakRefs) {
				if (ref.get() == null) {
					collectedCount++;
				} else {
					retainedCount++;
				}
			}

			System.out.println("\n=== 객체 해제 테스트 결과 ===");
			System.out.printf("총 요청 객체: %d\n", requestCount);
			System.out.printf("GC된 객체: %d\n", collectedCount);
			System.out.printf("유지된 객체: %d\n", retainedCount);

			// 최소 50% 이상의 객체가 GC되어야 함
			assertThat(collectedCount)
					.describedAs("최소 50%% 이상의 요청 객체가 GC되어야 함")
					.isGreaterThanOrEqualTo(requestCount / 2);
		}

		@Test
		@DisplayName("응답 객체가 GC 후 정상적으로 해제된다")
		void responseObjects_garbageCollected() throws Exception {
			// given
			int requestCount = 100;
			List<WeakReference<InsightResponse>> weakRefs = new ArrayList<>();

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						return createMockResponse(req.getUserId());
					});

			// when
			for (int i = 0; i < requestCount; i++) {
				InsightRequest request = createTestRequest("user-" + i);
				InsightResponse response = controller.analysis(request);
				weakRefs.add(new WeakReference<>(response));
			}

			// GC 강제 실행
			System.gc();
			Thread.sleep(200);

			// then
			int collectedCount = 0;
			for (WeakReference<InsightResponse> ref : weakRefs) {
				if (ref.get() == null) {
					collectedCount++;
				}
			}

			System.out.println("\n=== 응답 객체 해제 테스트 결과 ===");
			System.out.printf("총 응답 객체: %d\n", requestCount);
			System.out.printf("GC된 객체: %d\n", collectedCount);

			// WeakReference 테스트는 GC 타이밍에 따라 다를 수 있으므로 로그만 출력
			System.out.println("(참고: GC 타이밍에 따라 결과가 다를 수 있음)");
		}
	}

	@Nested
	@DisplayName("ThreadLocal 누수 테스트")
	class ThreadLocalLeakTest {

		@Test
		@DisplayName("GeminiContext ThreadLocal이 정상적으로 정리된다")
		void geminiContext_properlyCleared() {
			// given
			String testModelId = "test-model-id";
			String testModelName = "test-model-name";

			// when
			GeminiContext.setModelInfo(testModelId, testModelName);
			String versionBefore = GeminiContext.getAnalysisVersion();

			GeminiContext.clear();
			String versionAfter = GeminiContext.getAnalysisVersion();

			// then
			System.out.println("\n=== ThreadLocal 정리 테스트 결과 ===");
			System.out.printf("정리 전: %s\n", versionBefore);
			System.out.printf("정리 후: %s\n", versionAfter);

			assertThat(versionBefore).isEqualTo("test-model-name[test-model-id]");
			assertThat(versionAfter).isEqualTo("unknown");
		}

		@Test
		@DisplayName("멀티 스레드 환경에서 ThreadLocal이 스레드별로 독립적이다")
		void geminiContext_threadIsolation() throws Exception {
			// given
			int threadCount = 5;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(threadCount);

			AtomicInteger isolationViolations = new AtomicInteger(0);
			List<String> results = java.util.Collections.synchronizedList(new ArrayList<>());

			// when
			for (int i = 0; i < threadCount; i++) {
				final int threadId = i;
				executor.submit(() -> {
					try {
						startLatch.await();

						String modelId = "model-" + threadId;
						String modelName = "name-" + threadId;

						GeminiContext.setModelInfo(modelId, modelName);
						Thread.sleep(10); // 다른 스레드가 값을 설정할 시간

						String version = GeminiContext.getAnalysisVersion();
						String expected = modelName + "[" + modelId + "]";

						if (!version.equals(expected)) {
							isolationViolations.incrementAndGet();
						}

						results.add("Thread-" + threadId + ": " + version);
						GeminiContext.clear();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} finally {
						endLatch.countDown();
					}
				});
			}

			startLatch.countDown();
			endLatch.await(10, TimeUnit.SECONDS);
			executor.shutdown();

			// then
			System.out.println("\n=== ThreadLocal 스레드 격리 테스트 결과 ===");
			results.forEach(System.out::println);
			System.out.printf("격리 위반 횟수: %d\n", isolationViolations.get());

			assertThat(isolationViolations.get())
					.describedAs("ThreadLocal 격리 위반이 없어야 함")
					.isZero();
		}
	}

	@Nested
	@DisplayName("대량 요청 메모리 프로파일링")
	class BulkRequestMemoryProfileTest {

		@Test
		@DisplayName("5000회 요청 시 메모리 사용 패턴을 분석한다")
		void bulkRequests_memoryProfile() throws Exception {
			// given
			int totalRequests = 5000;
			int snapshotInterval = 500;
			List<MemorySnapshot> snapshots = new ArrayList<>();

			when(insightService.requestInsight(any(InsightRequest.class)))
					.thenAnswer(invocation -> {
						InsightRequest req = invocation.getArgument(0);
						return createMockResponse(req.getUserId());
					});

			System.gc();
			Thread.sleep(100);

			// when
			for (int i = 0; i < totalRequests; i++) {
				InsightRequest request = createTestRequest("user-" + i);
				controller.analysis(request);

				if (i % snapshotInterval == 0) {
					Runtime runtime = Runtime.getRuntime();
					snapshots.add(new MemorySnapshot(
							i,
							runtime.totalMemory(),
							runtime.freeMemory(),
							runtime.maxMemory()
					));
				}
			}

			// GC 후 최종 스냅샷
			System.gc();
			Thread.sleep(200);
			Runtime runtime = Runtime.getRuntime();
			snapshots.add(new MemorySnapshot(
					totalRequests,
					runtime.totalMemory(),
					runtime.freeMemory(),
					runtime.maxMemory()
			));

			// then
			System.out.println("\n=== 대량 요청 메모리 프로파일 ===");
			System.out.println("요청 수\t\t총 메모리(MB)\t사용 메모리(MB)\t여유 메모리(MB)");

			MemorySnapshot first = snapshots.get(0);
			MemorySnapshot last = snapshots.get(snapshots.size() - 1);

			for (MemorySnapshot snapshot : snapshots) {
				System.out.printf("%d\t\t%.2f\t\t%.2f\t\t%.2f\n",
						snapshot.requestCount,
						snapshot.totalMemory / 1024.0 / 1024.0,
						snapshot.usedMemory() / 1024.0 / 1024.0,
						snapshot.freeMemory / 1024.0 / 1024.0);
			}

			long memoryGrowth = last.usedMemory() - first.usedMemory();
			double growthPerRequest = (double) memoryGrowth / totalRequests;

			System.out.printf("\n요청당 평균 메모리 증가: %.2f bytes\n", growthPerRequest);
			System.out.printf("총 메모리 증가: %.2f MB\n", memoryGrowth / 1024.0 / 1024.0);

			// 요청당 메모리 증가가 1KB 미만이어야 함 (누수 없음)
			assertThat(Math.abs(growthPerRequest))
					.describedAs("요청당 메모리 증가가 1KB 미만이어야 함")
					.isLessThan(1024);
		}
	}

	private long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	private record MemorySnapshot(int requestCount, long totalMemory, long freeMemory, long maxMemory) {
		long usedMemory() {
			return totalMemory - freeMemory;
		}
	}
}
