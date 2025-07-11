package com.pratham.finvera.job;

import com.pratham.finvera.entity.OtpToken;
import com.pratham.finvera.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupJob {

    private final OtpTokenRepository otpTokenRepository;

    @Scheduled(fixedRateString = "#{@otpProperties.cleanupFixedRateMs}")
    public void cleanExpiredOtpTokens() {
        Instant now = Instant.now();
        List<OtpToken> expiredTokens = otpTokenRepository.findAllByExpiresAtBefore(now);

        if (!expiredTokens.isEmpty()) {
            otpTokenRepository.deleteAll(expiredTokens);
            log.info("Deleted {} expired OTP token(s).", expiredTokens.size());
        } else {
            log.info("No expired OTP tokens found during cleanup.");
        }
    }
}