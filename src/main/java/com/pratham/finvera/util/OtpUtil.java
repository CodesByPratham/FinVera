package com.pratham.finvera.util;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pratham.finvera.config.OtpProperties;
import com.pratham.finvera.entity.OtpToken;
import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.OtpPurpose;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class OtpUtil {

    private final OtpProperties otpProperties;

    /**
     * Create a new OtpToken for a user with the given purpose and expiry.
     * Generate a 6-digit random OTP as a String.
     */
    public OtpToken createOtpToken(User user, OtpPurpose purpose) {

        return OtpToken.builder()
                .otp(String.valueOf((int) (Math.random() * 900_000) + 100_000))
                .purpose(purpose)
                .expiresAt(Instant.now().plusSeconds(otpProperties.getOtpExpiration()))
                .user(user)
                .build();
    }

    /**
     * Update an existing OtpToken with a session token and new expiry (used for
     * FORGOT_PASSWORD).
     * Generate a UUID-based OTP session token.
     */
    public void attachSessionToken(OtpToken otpToken) {
        otpToken.setOtpSessionToken(UUID.randomUUID().toString());
        otpToken.setExpiresAt(Instant.now().plusSeconds(otpProperties.getSessionExpiration()));
    }
}
