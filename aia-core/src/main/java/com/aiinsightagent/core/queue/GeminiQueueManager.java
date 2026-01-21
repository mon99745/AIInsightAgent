package com.aiinsightagent.core.queue;

import com.aiinsightagent.core.config.GeminiProperties;
import com.aiinsightagent.core.config.RequestQueueProperties;
import com.google.genai.Models;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BlockingQueue와 Worker 스레드 풀을 관리하는 매니저
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiQueueManager {
	private final List<Models> geminiModelsList;
	private final GeminiProperties geminiProperties;
	private final RequestQueueProperties queueProperties;

	private BlockingQueue<GeminiRequest> requestQueue;
	private ExecutorService workerExecutor;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final List<Future<?>> workerFutures = new ArrayList<>();

	@PostConstruct
	public void init() {
		requestQueue = new LinkedBlockingQueue<>(queueProperties.getQueueCapacity());
		workerExecutor = Executors.newFixedThreadPool(
				queueProperties.getWorkerCount(),
				new GeminiWorkerThreadFactory()
		);

		running.set(true);

		List<GeminiProperties.ModelConfig> validModels = geminiProperties.getValidModels();
		int modelCount = validModels.size();
		for (int i = 0; i < queueProperties.getWorkerCount(); i++) {
			String workerName = "gemini-worker-" + i;
			// 워커별로 다른 모델 설정 할당 (순환 방식)
			int modelIndex = i % modelCount;
			GeminiProperties.ModelConfig modelConfig = validModels.get(modelIndex);
			Models assignedModels = geminiModelsList.get(modelIndex);
			GeminiWorker worker = new GeminiWorker(
					workerName,
					modelConfig,
					requestQueue,
					assignedModels,
					geminiProperties,
					running
			);
			workerFutures.add(workerExecutor.submit(worker));
			log.debug("[{}] assigned model: id={}, name={}", workerName, modelConfig.getId(), modelConfig.getName());
		}

		log.info("GeminiQueueManager initialized: workers={}, model-count={}, queueCapacity={}",
				queueProperties.getWorkerCount(), modelCount, queueProperties.getQueueCapacity());
	}

	/**
	 * 요청을 큐에 제출하고 CompletableFuture 반환
	 */
	public CompletableFuture<GeminiResponse> submit(String prompt) {
		if (!running.get()) {
			CompletableFuture<GeminiResponse> future = new CompletableFuture<>();
			future.completeExceptionally(
					new IllegalStateException("GeminiQueueManager is not running")
			);
			return future;
		}

		GeminiRequest request = new GeminiRequest(prompt);

		boolean offered = requestQueue.offer(request);
		if (!offered) {
			request.getFuture().completeExceptionally(
					new RejectedExecutionException("Request queue is full")
			);
		}

		log.debug("Request submitted, queueSize={}", requestQueue.size());
		return request.getFuture();
	}

	/**
	 * 동기식 호출 (기존 인터페이스 호환)
	 */
	public GeminiResponse submitAndWait(String prompt)
			throws ExecutionException, InterruptedException, TimeoutException {
		return submit(prompt).get(
				queueProperties.getRequestTimeoutSeconds(),
				TimeUnit.SECONDS
		);
	}

	@PreDestroy
	public void shutdown() {
		log.info("Shutting down GeminiQueueManager...");
		running.set(false);

		workerExecutor.shutdown();
		try {
			if (!workerExecutor.awaitTermination(
					queueProperties.getShutdownTimeoutSeconds(), TimeUnit.SECONDS)) {
				log.warn("Workers did not terminate in time, forcing shutdown");
				workerExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			workerExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}

		// 남은 요청들 실패 처리
		int remaining = requestQueue.size();
		if (remaining > 0) {
			log.warn("Cancelling {} remaining requests", remaining);
			requestQueue.forEach(req ->
					req.getFuture().completeExceptionally(
							new CancellationException("Queue manager shutdown")
					)
			);
		}

		log.info("GeminiQueueManager shutdown complete");
	}

	public int getWorkerCount() {
		return queueProperties.getWorkerCount();
	}

	public boolean isRunning() {
		return running.get();
	}

	private static class GeminiWorkerThreadFactory implements ThreadFactory {
		private int counter = 0;

		@Override
		public Thread newThread(@NonNull Runnable r) {
			Thread thread = new Thread(r, "gemini-worker-" + counter++);
			thread.setDaemon(false);
			return thread;
		}
	}
}