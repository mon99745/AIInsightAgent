package com.aiinsightagent.console.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "console")
public class ConsoleProperties {

	private String appBaseUrl = "http://localhost:28080";
}
