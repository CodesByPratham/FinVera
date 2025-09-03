package com.pratham.finvera.payload;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
public class GetTransactionsResponse extends MessageResponse {
    private HttpStatus status;
    private String message;
    private List<TransactionResponse> transactions;
    private Pagination pagination;
}