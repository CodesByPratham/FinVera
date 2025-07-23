package com.pratham.finvera.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String message;
}
