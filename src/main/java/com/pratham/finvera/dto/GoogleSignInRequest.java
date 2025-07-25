package com.pratham.finvera.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleSignInRequest {

    @NotBlank(message = "ID token is required")
    private String idToken;
}
