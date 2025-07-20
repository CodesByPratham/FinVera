package com.pratham.finvera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class OAuthProperties {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
}
