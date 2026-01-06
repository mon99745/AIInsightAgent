package com.aiinsightagent.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.aiinsightagent.app", "com.aiinsightagent.core", "com.aiinsightagent.common"})
@EntityScan("com.aiinsightagent.app.entity")
@EnableJpaRepositories("com.aiinsightagent.app.repository")
@ConfigurationPropertiesScan
public class AiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiaApplication.class, args);
	}

}
