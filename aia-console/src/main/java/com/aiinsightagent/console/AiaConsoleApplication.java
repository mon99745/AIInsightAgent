package com.aiinsightagent.console;

import com.aiinsightagent.common.config.SpringDocConfig;
import com.aiinsightagent.common.controller.IndexController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {
		"com.aiinsightagent.console",
		"com.aiinsightagent.common"
})
@ComponentScan(
		basePackages = {"com.aiinsightagent.console", "com.aiinsightagent.common"},
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = {IndexController.class, SpringDocConfig.class}
		)
)
@EntityScan("com.aiinsightagent.console.entity")
@EnableJpaRepositories("com.aiinsightagent.console.repository")
@ConfigurationPropertiesScan
public class AiaConsoleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiaConsoleApplication.class, args);
	}
}
