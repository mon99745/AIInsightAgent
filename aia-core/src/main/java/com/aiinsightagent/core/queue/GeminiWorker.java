package com.aiinsightagent.core.queue;

import com.aiinsightagent.core.config.GeminiProperties;
import com.aiinsightagent.core.model.TokenUsage;
import com.aiinsightagent.core.util.GeminiTokenExtractor;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 큐에서 요청을 꺼내 Gemini API를 호출하는 Worker
 */
@Slf4j
public class GeminiWorker implements Runnable {
	private final String workerName;
	private final BlockingQueue<GeminiRequest> requestQueue;
	private final Models models;
	private final GeminiProperties geminiProperties;
	private final AtomicBoolean running;

	public GeminiWorker(
			String workerName,
			BlockingQueue<GeminiRequest> requestQueue,
			Models models,
			GeminiProperties geminiProperties,
			AtomicBoolean running
	) {
		this.workerName = workerName;
		this.requestQueue = requestQueue;
		this.models = models;
		this.geminiProperties = geminiProperties;
		this.running = running;
	}

	@Override
	public void run() {
		log.info("[{}] Worker started", workerName);

		while (running.get() || !requestQueue.isEmpty()) {
			try {
				GeminiRequest request = requestQueue.poll(1, TimeUnit.SECONDS);
				if (request == null) continue;

				processRequest(request);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn("[{}] Worker interrupted", workerName);
				break;
			}
		}

		log.info("[{}] Worker stopped", workerName);
	}

	private void processRequest(GeminiRequest request) {
		long waitTime = System.currentTimeMillis() - request.getCreatedAt();
		long startTime = System.currentTimeMillis();

		try {
			GenerateContentConfig config = buildConfig();

			GenerateContentResponse response = models.generateContent(
					geminiProperties.getModel(),
					request.getPrompt(),
					config
			);

			long duration = System.currentTimeMillis() - startTime;
			TokenUsage tokenUsage = GeminiTokenExtractor.extract(response);

			log.info("[{}] model={}, waitTime={}ms, apiTime={}ms",
					workerName, geminiProperties.getModel(), waitTime, duration);
			log.debug("[{}] tokens: prompt={}, completion={}, total={}",
					workerName, tokenUsage.getPromptTokens(),
					tokenUsage.getCompletionTokens(), tokenUsage.getTotalTokens());

			request.getFuture().complete(response);
		} catch (Exception e) {
			log.error("[{}] API call failed: {}", workerName, e.getMessage(), e);
			request.getFuture().completeExceptionally(e);
		}
	}

	private GenerateContentConfig buildConfig() {
		GenerateContentConfig.Builder builder = GenerateContentConfig.builder();

		if (geminiProperties.getMaxOutputTokens() != null) {
			builder.maxOutputTokens(geminiProperties.getMaxOutputTokens());
		}

		if (geminiProperties.getTemperature() > 0) {
			builder.temperature((float) geminiProperties.getTemperature());
		}

		return builder.build();
	}
}