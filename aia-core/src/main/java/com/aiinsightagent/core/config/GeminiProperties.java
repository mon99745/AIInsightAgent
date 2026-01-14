package com.aiinsightagent.core.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
	 * API 키
	 */
	private String apiKey;

	/**
	 * 모델 명
	 */
	private String model;

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