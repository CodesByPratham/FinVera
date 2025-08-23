package com.pratham.finvera.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pratham.finvera.dto.BatchDeleteRequest;
import com.pratham.finvera.dto.TransactionRequest;
import com.pratham.finvera.entity.SubCategory;
import com.pratham.finvera.entity.Transaction;
import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.PaymentType;
import com.pratham.finvera.enums.TransactionType;
import com.pratham.finvera.exception.ResourceNotFoundException;
import com.pratham.finvera.exception.UnauthorizedException;
import com.pratham.finvera.payload.GetTransactionResponse;
import com.pratham.finvera.payload.GetTransactionsResponse;
import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.payload.TransactionResponse;
import com.pratham.finvera.repository.SubCategoryRepository;
import com.pratham.finvera.repository.TransactionRepository;
import com.pratham.finvera.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SubCategoryRepository subCategoryRepository;

    public MessageResponse addTransaction(TransactionRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(email);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        Long catId = Long.parseLong(request.getSubCategoryId());
        SubCategory subCategory = subCategoryRepository.findById(catId).orElseThrow(
                () -> new ResourceNotFoundException("Subcategory not found with ID: " + catId));

        String payType = request.getPaymentType();

        Transaction transaction = Transaction.builder()
                .type(TransactionType.valueOf(request.getType()))
                .amount(new BigDecimal(request.getAmount()))
                .subCategory(subCategory)
                .description(request.getDescription())
                .paymentType(payType == null ? null : PaymentType.valueOf(payType))
                .location(request.getLocation())
                .receiptImageUrl(request.getReceiptImageUrl())
                .user(user)
                .build();

        transactionRepository.save(transaction);

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Transaction added successfully")
                .build();
    }

    public MessageResponse getTransactionById(Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Validate ownership
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        return GetTransactionResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Transaction found with ID: " + id)
                .transaction(TransactionResponse.fromTransaction(transaction))
                .build();
    }

    public MessageResponse updateTransaction(Long id, TransactionRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        Long catId = Long.parseLong(request.getSubCategoryId());
        SubCategory subCategory = subCategoryRepository.findById(catId).orElseThrow(
                () -> new ResourceNotFoundException("Subcategory not found with ID: " + catId));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        String payType = request.getPaymentType();

        transaction.setAmount(new BigDecimal(request.getAmount()));
        transaction.setType(TransactionType.valueOf(request.getType()));
        transaction.setSubCategory(subCategory);
        transaction.setDescription(request.getDescription());
        transaction.setPaymentType(payType == null ? null : PaymentType.valueOf(payType));
        transaction.setLocation(request.getLocation());
        transaction.setReceiptImageUrl(request.getReceiptImageUrl());

        transactionRepository.save(transaction);

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Transaction updated successfully")
                .build();
    }

    public MessageResponse deleteTransaction(Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Ownership validation
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this transaction");
        }

        transactionRepository.delete(transaction);

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Transaction deleted successfully")
                .build();
    }

    public MessageResponse deleteTransactionsInBatch(BatchDeleteRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        List<Transaction> transactions = transactionRepository.findAllById(request.getTransactionIds());

        // Check for ownership
        for (Transaction transaction : transactions) {
            if (!transaction.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("You do not have access to transaction ID: " + transaction.getId());
            }
        }

        transactionRepository.deleteAll(transactions);

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message(transactions.size() + " Transactions deleted successfully")
                .build();
    }

    public MessageResponse getAllTransactions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(TransactionResponse::fromTransaction)
                .collect(Collectors.toList());

        return GetTransactionsResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Transactions retrieved successfully")
                .transactions(transactionResponses)
                .build();
    }
}