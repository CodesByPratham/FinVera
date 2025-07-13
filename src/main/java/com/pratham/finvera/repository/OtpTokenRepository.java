package com.pratham.finvera.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pratham.finvera.entity.OtpToken;
import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.OtpPurpose;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByPurposeAndUserAndOtp(OtpPurpose purpose, User user, String otp);

    Optional<OtpToken> findByUser(User user);

    Optional<OtpToken> findByUserAndOtpSessionToken(User user, String otpSessionToken);

    // Method to delete expired OTPs
    List<OtpToken> findAllByExpiresAtBefore(Instant time);
}