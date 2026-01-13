package com.aiinsightagent.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "common.api-logging")
public class ApiLoggingProperties {

	private boolean enabled = true;

	private List<String> includePaths = List.of("/api/**");

	private List<String> excludePaths = List.of("/api/docs/**", "/api/swagger-ui/**");
}
