package com.pratham.finvera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class GoogleSignInProperties {
    @Value("${google.sign-in.client-id}")
    private String clientId;
}
