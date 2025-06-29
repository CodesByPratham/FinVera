package com.pratham.finvera.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Instant timestamp;
    private HttpStatus status;
    private String message;
}
