package com.pratham.finvera.dto;

import com.pratham.finvera.enums.OtpPurpose;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Purpose is required")
    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;
}