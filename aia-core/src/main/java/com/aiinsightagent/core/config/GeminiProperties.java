package com.aiinsightagent.core.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConfigurationProperties(prefix = GeminiProperties.PROPERTY_PREFIX)
public class GeminiProperties {
	/**
	 * 설정 타이틀
	 */
	public static final String PROPERTY_PREFIX = "spring.ai.gemini";

	/**
	 * 설정 정보
	 */
	@Getter
	private static GeminiProperties instance = new GeminiProperties();

	/**
	 * 모델 설정 목록
	 */
	private List<ModelConfig> models = new ArrayList<>();

	/**
	 * 모델 설정 클래스
	 */
	@Data
	@NoArgsConstructor
	public static class ModelConfig {
		private String id;
		private String name;
		private String apiKey;

		@Override
		public String toString() {
			return "ModelConfig(id=" + id + ", name=" + name + ", apiKey=****)";
		}
	}

	/**
	 * 유효한 모델 설정 목록 반환 (API 키가 비어있지 않은 것만)
	 */
	public List<ModelConfig> getValidModels() {
		return models.stream()
				.filter(model -> model.getApiKey() != null && !model.getApiKey().isBlank())
				.toList();
	}

	/**
	 * 첫 번째 유효한 API 키 반환 (호환성 유지)
	 */
	public String getApiKey() {
		List<ModelConfig> validModels = getValidModels();
		return validModels.isEmpty() ? null : validModels.get(0).getApiKey();
	}

	/**
	 * 온도 설정
	 */
	private double temperature;

	/**
	 * 기본 URL
	 */
	private String baseUrl;

	/**
	 * 완성 경로
	 */
	private String completionsPath;

	/**
	 * 최대 출력 토큰 수 (응답 속도에 영향)
	 */
	private Integer maxOutputTokens;
}