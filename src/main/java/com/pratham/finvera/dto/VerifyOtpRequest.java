package com.pratham.finvera.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Purpose is required")
    @Pattern(regexp = "^(REGISTER|FORGOT_PASSWORD)$", message = "Purpose must be either REGISTER or FORGOT_PASSWORD")
    private String purpose;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
    private String otp;
}