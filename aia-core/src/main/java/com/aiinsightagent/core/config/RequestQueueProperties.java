package com.aiinsightagent.core.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConfigurationProperties(prefix = RequestQueueProperties.PROPERTY_PREFIX)
public class RequestQueueProperties {
	/**
	 * 설정 타이틀
	 */
	public static final String PROPERTY_PREFIX = "aiinsight.request.queue";

	/**
	 * 설정 정보
	 */
	@Getter
	private static RequestQueueProperties instance = new RequestQueueProperties();

	/**
	 * Worker 스레드 수 (동시 API 요청 수)
	 */
	private int workerCount = 10;

	/**
	 * 대기열 최대 크기
	 */
	private int queueCapacity = 100;

	/**
	 * 요청 타임아웃 (초)
	 */
	private int requestTimeoutSeconds = 60;

	/**
	 * Shutdown 시 대기 시간 (초)
	 */
	private int shutdownTimeoutSeconds = 30;
}