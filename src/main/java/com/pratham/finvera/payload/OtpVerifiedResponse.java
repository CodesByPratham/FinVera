package com.pratham.finvera.payload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
public class OtpVerifiedResponse extends MessageResponse {

    private Instant timestamp;
    private HttpStatus status;
    private String message;
    private String otpSessionToken;
}