package com.aiinsightagent.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "beta"})
@RequiredArgsConstructor
public class SpringDocConfig {
	private final SpringDocProperties props;

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

		OpenAPI openAPI = new OpenAPI().info(info);

		return openAPI;
	}
}