package com.pratham.finvera.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GetTransactionParamRequest {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 10;

    @Pattern(regexp = "createdAt|updatedAt|amount", message = "sortBy must be 'createdAt', 'updatedAt' or 'amount'")
    private String sortBy = "createdAt";

    @Pattern(regexp = "asc|desc", message = "sortDir must be 'asc' or 'desc'")
    private String sortDir = "desc";
}
