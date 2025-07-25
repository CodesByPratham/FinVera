package com.pratham.finvera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableConfigurationProperties
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
public class FinveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinveraApplication.class, args);
	}
}