package com.pratham.finvera;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableConfigurationProperties
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
@EnableCaching
public class FinveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinveraApplication.class, args);
	}
 
	@Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}