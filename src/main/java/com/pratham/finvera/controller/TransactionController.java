package com.pratham.finvera.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pratham.finvera.dto.BatchDeleteRequest;
import com.pratham.finvera.dto.GetTransactionParamRequest;
import com.pratham.finvera.dto.AddTransactionRequest;
import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<MessageResponse> getTransactions(@Valid GetTransactionParamRequest request) {
        return ResponseEntity.ok(transactionService.getUserTransactions(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> addTransaction(@Valid @RequestBody AddTransactionRequest request) {
        return ResponseEntity.ok(transactionService.addTransaction(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateTransaction(
            @PathVariable Long id, @Valid @RequestBody AddTransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.deleteTransaction(id));
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteTransactions(@Valid @RequestBody BatchDeleteRequest request) {
        return ResponseEntity.ok(transactionService.deleteTransactionsInBatch(request));
    }
}
