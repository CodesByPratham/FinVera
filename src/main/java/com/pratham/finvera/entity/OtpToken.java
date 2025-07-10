package com.pratham.finvera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.Instant;

import com.pratham.finvera.enums.OtpPurpose;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "OTP cannot be blank")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit number")
    @Column(nullable = false, length = 6)
    private String otp;

    @NotNull(message = "Expiration time cannot be null")
    @Column(nullable = false)
    private Instant expiresAt;

    @NotNull(message = "User cannot be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) // Explicitly declare the foreign key
    private User user;

    @NotNull(message = "OTP purpose is required")
    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose; // e.g., "REGISTER", "FORGOT_PASSWORD"

    private String otpSessionToken; // For session after OTP is verified
}
