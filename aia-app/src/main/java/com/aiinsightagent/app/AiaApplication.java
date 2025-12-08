package com.aiinsightagent.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"com.aiinsightagent.app", "com.aiinsightagent.core"})
@ConfigurationPropertiesScan
public class AiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiaApplication.class, args);
	}

}
