package com.pratham.finvera.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pratham.finvera.entity.OtpToken;
import com.pratham.finvera.entity.User;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByUserAndOtp(User user, String otp);

    Optional<OtpToken> findByUser(User user);
}