package com.aiinsightagent.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.aiinsightagent.app",
                "com.aiinsightagent.core",
                "com.aiinsightagent.common"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AiaApplication.class
        )
)
@EntityScan("com.aiinsightagent.app.entity")
@EnableJpaRepositories("com.aiinsightagent.app.repository")
public class TestApplication {
        public static void main(String[] args) {
                SpringApplication.run(TestApplication.class, args);
        }
}
