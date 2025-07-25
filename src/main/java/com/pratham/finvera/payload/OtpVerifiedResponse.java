package com.pratham.finvera.payload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
public class OtpVerifiedResponse extends MessageResponse {

    private String otpSessionToken;
}