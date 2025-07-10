package com.pratham.finvera.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class OtpProperties {

    @Value("${otp.expiration}")
    private int otpExpiration;

    @Value("${otp.sessionExpiration}")
    private int sessionExpiration;

    @Value("${otp.cleanupFixedRateMs}")
    private long cleanupFixedRateMs;
}
