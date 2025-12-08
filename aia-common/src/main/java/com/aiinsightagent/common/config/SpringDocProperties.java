package com.aiinsightagent.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "common.api-doc")
public class SpringDocProperties {
	private String title;
	private String description;
	private String version;
	private String email;
	private String url;

	private List<Group> groups;

	@Data
	public static class Group {
		private String name;
		private String pkg;
	}
}