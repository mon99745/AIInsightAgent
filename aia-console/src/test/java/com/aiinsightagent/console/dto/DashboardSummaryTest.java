package com.aiinsightagent.console.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DashboardSummary 테스트")
class DashboardSummaryTest {

	@Test
	@DisplayName("Builder로 정상 생성")
	void builder_CreatesInstance() {
		DashboardSummary summary = DashboardSummary.builder()
				.totalAnalysis(100)
				.successCount(80)
				.failedCount(20)
				.appHealthStatus("UP")
				.build();

		assertThat(summary.getTotalAnalysis()).isEqualTo(100);
		assertThat(summary.getSuccessCount()).isEqualTo(80);
		assertThat(summary.getFailedCount()).isEqualTo(20);
		assertThat(summary.getAppHealthStatus()).isEqualTo("UP");
	}

	@Test
	@DisplayName("Builder 기본값 - 숫자 필드는 0, 문자열은 null")
	void builder_DefaultValues() {
		DashboardSummary summary = DashboardSummary.builder().build();

		assertThat(summary.getTotalAnalysis()).isZero();
		assertThat(summary.getSuccessCount()).isZero();
		assertThat(summary.getFailedCount()).isZero();
		assertThat(summary.getAppHealthStatus()).isNull();
	}
}
