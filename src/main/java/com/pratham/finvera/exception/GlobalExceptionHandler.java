package com.pratham.finvera.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.pratham.finvera.payload.MessageResponse;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MessageResponse> handleBadRequest(BadRequestException ex, WebRequest req) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<MessageResponse> handleUnauthorized(UnauthorizedException ex, WebRequest req) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<MessageResponse> handleEmailSendException(EmailSendException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<MessageResponse> handleDisabledUser(DisabledException ex, WebRequest req) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Account not verified. Please verify OTP first");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<MessageResponse> handleNoHandlerFound(NoHandlerFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Endpoint not found: " + ex.getRequestURL());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, Object> message = new HashMap<>();

        message.put("timestamp", Instant.now());
        message.put("status", HttpStatus.BAD_REQUEST);

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (msg1, msg2) -> msg1 + "; " + msg2 // merge if multiple errors on same field
                ));

        message.put("errors", errors);

        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponse> handleInvalidFormat(HttpMessageNotReadableException ex) {
        if (ex.getCause() != null && ex.getCause().getCause() instanceof DateTimeParseException) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Date of birth must be in format yyyy-MM-dd");
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneral(Exception ex, WebRequest req) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<MessageResponse> buildErrorResponse(HttpStatus status, String message) {
        MessageResponse response = MessageResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }
}