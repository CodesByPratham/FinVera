package com.pratham.finvera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties
public class FinveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinveraApplication.class, args);
	}
}
