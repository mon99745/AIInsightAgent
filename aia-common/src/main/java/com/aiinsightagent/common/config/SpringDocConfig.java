package com.aiinsightagent.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({"local", "beta"})
@RequiredArgsConstructor
public class SpringDocConfig {
	private final SpringDocProperties props;

	@Value("${springdoc.server.url}")
	private String serverUrl;

	@Value("${springdoc.server.description:}")
	private String serverDescription;

	@Bean
	public OpenAPI openAPI() {
		Contact contact = new Contact()
				.name(props.getTitle())
				.url(props.getContactUrl())
				.email(props.getContactEmail());

		Info info = new Info()
				.title(props.getTitle())
				.version(props.getVersion())
				.description(props.getDescription())
				.contact(contact);

		return new OpenAPI().info(info)
				.servers(List.of(
						new Server()
								.url(serverUrl)
								.description(serverDescription)
				));
	}
}