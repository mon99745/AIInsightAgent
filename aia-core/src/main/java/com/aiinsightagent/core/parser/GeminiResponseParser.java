package com.aiinsightagent.core.parser;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.preprocess.LlmJsonPreprocessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.Candidate;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class GeminiResponseParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private GeminiResponseParser() {
	}

	public static InsightResponse toInsightResponse(GenerateContentResponse response) {
		// 응답 절단 여부 확인
		checkResponseTruncation(response);

		String raw = response.text();
		String pureJson = LlmJsonPreprocessor.extractPureJson(raw);
		if (pureJson == null || pureJson.isBlank()) {
			throw new InsightException(InsightError.EMPTY_GEMINI_RESPONSE);
		}

		InsightDetail insightDetail = null;
		try {
			insightDetail = objectMapper.readValue(pureJson, InsightDetail.class);
		} catch (IOException e) {
			// JSON 파싱 실패 시 응답 절단 가능성 재확인
			if (isLikelyTruncated(raw)) {
				throw new InsightException(InsightError.RESPONSE_TRUNCATED, e);
			}
			throw new InsightException(InsightError.FAIL_JSON_PARSING, e);
		}

		return InsightResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insight(insightDetail)
				.build();
	}

	/**
	 * Gemini API의 finishReason을 확인하여 응답 절단 여부 검사
	 */
	private static void checkResponseTruncation(GenerateContentResponse response) {
		Optional<List<Candidate>> candidatesOpt = response.candidates();
		if (candidatesOpt.isEmpty() || candidatesOpt.get().isEmpty()) {
			return;
		}

		Candidate candidate = candidatesOpt.get().get(0);
		Optional<FinishReason> finishReasonOpt = candidate.finishReason();

		if (finishReasonOpt.isPresent()) {
			FinishReason finishReason = finishReasonOpt.get();
			String reasonStr = finishReason.toString();
			// MAX_TOKENS로 끝난 경우 응답이 잘린 것
			if (reasonStr.contains("MAX_TOKENS") || reasonStr.contains("LENGTH")) {
				throw new InsightException(InsightError.RESPONSE_TRUNCATED);
			}
		}
	}

	/**
	 * JSON 구조가 불완전한지 휴리스틱 검사
	 */
	private static boolean isLikelyTruncated(String raw) {
		if (raw == null || raw.isBlank()) {
			return false;
		}

		String trimmed = raw.trim();

		// JSON이 열려있지만 닫히지 않은 경우
		long openBraces = trimmed.chars().filter(ch -> ch == '{').count();
		long closeBraces = trimmed.chars().filter(ch -> ch == '}').count();
		long openBrackets = trimmed.chars().filter(ch -> ch == '[').count();
		long closeBrackets = trimmed.chars().filter(ch -> ch == ']').count();

		return openBraces > closeBraces || openBrackets > closeBrackets;
	}
}