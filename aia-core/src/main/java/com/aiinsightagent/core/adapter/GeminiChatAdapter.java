package com.aiinsightagent.core.adapter;

import com.aiinsightagent.core.context.GeminiContext;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.queue.GeminiQueueManager;
import com.aiinsightagent.core.queue.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiChatAdapter {
	private final GeminiQueueManager queueManager;

	/**
	 * 동기식 Gemini Chat 응답 생성
	 *
	 * @param prompt 프롬프트
	 * @return GeminiResponse
	 */
	public GeminiResponse getResponse(String prompt) {
		try {
			GeminiResponse response = queueManager.submitAndWait(prompt);
			GeminiContext.setModelInfo(response.getModelId(), response.getModelName());
			return response;
		} catch (TimeoutException e) {
			throw new InsightException(InsightError.QUEUE_TIMEOUT, e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof InsightException insightException) {
				throw insightException;
			}
			if (cause instanceof RejectedExecutionException) {
				throw new InsightException(InsightError.QUEUE_FULL, cause);
			}
			if (cause instanceof IllegalStateException) {
				throw new InsightException(InsightError.QUEUE_NOT_RUNNING, cause);
			}
			throw new InsightException(InsightError.INTERNAL_SERVER_ERROR, cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new InsightException(InsightError.INTERNAL_SERVER_ERROR, e);
		}
	}

	/**
	 * 비동기식 Gemini Chat 응답 생성
	 *
	 * @param prompt 프롬프트
	 * @return CompletableFuture
	 */
	public CompletableFuture<GeminiResponse> getResponseAsync(String prompt) {
		return queueManager.submit(prompt);
	}
}
