package com.pratham.finvera.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import com.pratham.finvera.enums.OtpPurpose;

@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_user_purpose_otp", columnList = "user_id, purpose, otp"),
        @Index(name = "idx_otp_user_token", columnList = "user_id, otpSessionToken")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) // Unique because one user one otp
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose")
    private OtpPurpose purpose;

    @Column(name = "otp_session_token")
    private String otpSessionToken;
}