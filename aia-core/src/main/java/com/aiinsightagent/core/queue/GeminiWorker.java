package com.aiinsightagent.core.queue;

import com.aiinsightagent.common.filter.TraceIdFilter;
import com.aiinsightagent.core.config.GeminiProperties;
import com.aiinsightagent.core.model.TokenUsage;
import com.aiinsightagent.core.util.GeminiTokenExtractor;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 큐에서 요청을 꺼내 Gemini API를 호출하는 Worker
 */
@Slf4j
public class GeminiWorker implements Runnable {
	private final String workerName;
	private final GeminiProperties.ModelConfig modelConfig;
	private final BlockingQueue<GeminiRequest> requestQueue;
	private final Models models;
	private final GeminiProperties geminiProperties;
	private final AtomicBoolean running;

	public GeminiWorker(
			String workerName,
			GeminiProperties.ModelConfig modelConfig,
			BlockingQueue<GeminiRequest> requestQueue,
			Models models,
			GeminiProperties geminiProperties,
			AtomicBoolean running
	) {
		this.workerName = workerName;
		this.modelConfig = modelConfig;
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
		MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, request.getTraceId());
		try {
			long waitTime = System.currentTimeMillis() - request.getCreatedAt();
			long startTime = System.currentTimeMillis();

			try {
				GenerateContentConfig config = buildConfig();

				GenerateContentResponse response = models.generateContent(
						modelConfig.getName(),
						request.getPrompt(),
						config
				);

				long duration = System.currentTimeMillis() - startTime;
				TokenUsage tokenUsage = GeminiTokenExtractor.extract(response);

				log.info("[{}] modelId={}, model={}, waitTime={}ms, apiTime={}ms",
						workerName, modelConfig.getId(), modelConfig.getName(), waitTime, duration);
				log.debug("[{}] tokens: prompt={}, completion={}, total={}",
						workerName, tokenUsage.getPromptTokens(),
						tokenUsage.getCompletionTokens(), tokenUsage.getTotalTokens());

				request.getFuture().complete(new GeminiResponse(response, modelConfig.getId(), modelConfig.getName()));
			} catch (Exception e) {
				log.error("[{}] API call failed: {}", workerName, e.getMessage(), e);
				request.getFuture().completeExceptionally(e);
			}
		} finally {
			MDC.remove(TraceIdFilter.TRACE_ID_MDC_KEY);
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