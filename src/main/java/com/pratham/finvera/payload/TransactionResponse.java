package com.pratham.finvera.payload;

import java.math.BigDecimal;
import java.time.Instant;

import com.pratham.finvera.entity.Transaction;
import com.pratham.finvera.enums.PaymentType;
import com.pratham.finvera.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private String subCategory;
    private String description;
    private PaymentType paymentType;
    private String location;
    private String receiptImageUrl;
    private Instant createdAt;

    public static TransactionResponse fromTransaction(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getSubCategory().getCategory().getName())
                .subCategory(transaction.getSubCategory().getName())
                .description(transaction.getDescription())
                .paymentType(transaction.getPaymentType())
                .location(transaction.getLocation())
                .receiptImageUrl(transaction.getReceiptImageUrl())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
