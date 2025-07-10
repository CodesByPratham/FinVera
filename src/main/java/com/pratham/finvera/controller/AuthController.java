package com.pratham.finvera.controller;

import com.pratham.finvera.dto.*;
import com.pratham.finvera.payload.AuthResponse;
import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		return ResponseEntity.ok(authService.verifyOtp(request));
	}

	@PostMapping("/resend-otp")
	public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
		return ResponseEntity.ok(authService.resendOtp(request));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		return ResponseEntity.ok(authService.forgotPassword(request));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		return ResponseEntity.ok(authService.resetPassword(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/admin-login")
	public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody AuthRequest request) {
		return ResponseEntity.ok(authService.adminLogin(request));
	}
}