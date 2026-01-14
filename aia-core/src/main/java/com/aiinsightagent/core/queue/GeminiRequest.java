package com.aiinsightagent.core.queue;

import com.google.genai.types.GenerateContentResponse;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

/**
 * BlockingQueue에 담길 요청 객체
 */
@Getter
public class GeminiRequest {
	private final String prompt;
	private final CompletableFuture<GenerateContentResponse> future;
	private final long createdAt;

	public GeminiRequest(String prompt) {
		this.prompt = prompt;
		this.future = new CompletableFuture<>();
		this.createdAt = System.currentTimeMillis();
	}
}