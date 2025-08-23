package com.pratham.finvera.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class TransactionRequest {

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Purpose must be either REGISTER or FORGOT_PASSWORD")
    private String type;

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "^\\d+(\\.\\d{2})$", message = "Amount must be a valid number with exactly two decimal places")
    private String amount;

    @NotBlank(message = "Subcategory ID is required")
    @Pattern(regexp = "^\\d+$", message = "Subcategory ID must be a valid integer")
    private String subCategoryId;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @Pattern(regexp = "^$|^(CASH|DEBIT_CARD|CREDIT_CARD|BANK_TRANSFER|UPI)$", message = "Payment Type must be either CASH, DEBIT_CARD, CREDIT_CARD, BANK_TRANSFER or UPI")
    private String paymentType;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @Size(max = 500, message = "Receipt image URL must be at most 500 characters")
    @URL(message = "Receipt image must be a valid image URL")
    private String receiptImageUrl;
}
