package com.aiinsightagent.core.adapter;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.queue.GeminiQueueManager;
import com.aiinsightagent.core.queue.GeminiResponse;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class GeminiChatAdapterTest {
	private GeminiChatAdapter geminiChatAdapter;
	private GeminiQueueManager queueManager;

	@BeforeEach
	void setUp() {
		queueManager = Mockito.mock(GeminiQueueManager.class);
		geminiChatAdapter = new GeminiChatAdapter(queueManager);
	}

	@Test
	void getResponse_success() throws Exception {
		// given
		String prompt = "test prompt";
		GenerateContentResponse mockContentResponse = Mockito.mock(GenerateContentResponse.class);
		GeminiResponse mockResponse = new GeminiResponse(mockContentResponse, "m01", "gemini-2.5-flash");

		Mockito.when(queueManager.submitAndWait(anyString()))
				.thenReturn(mockResponse);

		// when
		GeminiResponse result = geminiChatAdapter.getResponse(prompt);

		// then
		assertNotNull(result);
		assertEquals(mockResponse, result);
		Mockito.verify(queueManager).submitAndWait(prompt);
	}

	@Test
	void getResponse_timeout_throwsException() throws Exception {
		// given
		Mockito.when(queueManager.submitAndWait(anyString()))
				.thenThrow(new TimeoutException("Timeout"));

		// when & then
		InsightException exception = assertThrows(InsightException.class,
				() -> geminiChatAdapter.getResponse("test"));

		assertEquals(InsightError.QUEUE_TIMEOUT, exception.getError());
	}

	@Test
	void getResponse_queueFull_throwsException() throws Exception {
		// given
		ExecutionException executionException = new ExecutionException(
				new RejectedExecutionException("Queue full")
		);
		Mockito.when(queueManager.submitAndWait(anyString()))
				.thenThrow(executionException);

		// when & then
		InsightException exception = assertThrows(InsightException.class,
				() -> geminiChatAdapter.getResponse("test"));

		assertEquals(InsightError.QUEUE_FULL, exception.getError());
	}

	@Test
	void getResponseAsync_success() {
		// given
		String prompt = "test prompt";
		GenerateContentResponse mockContentResponse = Mockito.mock(GenerateContentResponse.class);
		GeminiResponse mockResponse = new GeminiResponse(mockContentResponse, "m01", "gemini-2.5-flash");
		CompletableFuture<GeminiResponse> future = CompletableFuture.completedFuture(mockResponse);

		Mockito.when(queueManager.submit(anyString()))
				.thenReturn(future);

		// when
		CompletableFuture<GeminiResponse> result = geminiChatAdapter.getResponseAsync(prompt);

		// then
		assertNotNull(result);
		assertTrue(result.isDone());
		Mockito.verify(queueManager).submit(prompt);
	}
}
