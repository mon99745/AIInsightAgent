package com.aiinsightagent.core.queue;

import lombok.Getter;

import java.util.concurrent.CompletableFuture;

/**
 * BlockingQueue에 담길 요청 객체
 */
@Getter
public class GeminiRequest {
	private final String prompt;
	private final String traceId;
	private final CompletableFuture<GeminiResponse> future;
	private final long createdAt;

	public GeminiRequest(String prompt, String traceId) {
		this.prompt = prompt;
		this.traceId = traceId;
		this.future = new CompletableFuture<>();
		this.createdAt = System.currentTimeMillis();
	}
}