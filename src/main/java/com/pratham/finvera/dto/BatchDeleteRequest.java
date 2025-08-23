package com.pratham.finvera.dto;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Data
public class BatchDeleteRequest {
    @NotNull(message = "Transaction ID list cannot be null")
    @NotEmpty(message = "Transaction ID list cannot be empty")
    @Size(min = 1, message = "At least one transaction ID must be provided")
    private List<@NotNull(message = "Transaction ID cannot be null") @Positive(message = "Transaction ID must be a positive number") Long> transactionIds;
}
