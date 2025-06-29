package com.pratham.finvera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FinveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinveraApplication.class, args);
	}
}
