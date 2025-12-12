package com.aiinsightagent.common.config;

import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class SpringDocConfig {
	private final SpringDocProperties props;

	@Bean
	public OpenAPI openAPI() {
		Contact contact = new Contact()
				.name(props.getTitle())
				.url(props.getUrl())
				.email(props.getEmail());

		Info info = new Info()
				.title(props.getTitle())
				.version(props.getVersion())
				.description(props.getDescription())
				.contact(contact);

		OpenAPI openAPI = new OpenAPI().info(info);

		Optional.ofNullable(props.getUrl())
				.filter(StringUtils::isNotEmpty)
				.ifPresent(a -> {
					List<Server> servers = Collections.singletonList(new Server().url(a));
					openAPI.servers(servers);
				});

		return openAPI;
	}
}