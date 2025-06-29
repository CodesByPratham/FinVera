package com.pratham.finvera.service;

import com.pratham.finvera.dto.AuthRequest;
import com.pratham.finvera.dto.ForgotPasswordRequest;
import com.pratham.finvera.dto.RegisterRequest;
import com.pratham.finvera.dto.ResendOtpRequest;
import com.pratham.finvera.dto.ResetPasswordRequest;
import com.pratham.finvera.dto.VerifyOtpRequest;
import com.pratham.finvera.entity.OtpToken;
import com.pratham.finvera.entity.Role;
import com.pratham.finvera.entity.User;
import com.pratham.finvera.exception.BadRequestException;
import com.pratham.finvera.exception.ResourceNotFoundException;
import com.pratham.finvera.exception.UnauthorizedException;
import com.pratham.finvera.payload.AuthResponse;
import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.payload.UserResponse;
import com.pratham.finvera.repository.OtpTokenRepository;
import com.pratham.finvera.repository.RoleRepository;
import com.pratham.finvera.repository.UserRepository;
import com.pratham.finvera.utiil.JwtUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use.");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found."));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .isVerified(false) // set true after OTP later
                .roles(Collections.singleton(userRole))
                .build();

        userRepository.save(user);

        // Generate OTP
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        OtpToken otpToken = OtpToken.builder()
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(300)) // 5 minutes
                .user(user)
                .build();

        otpTokenRepository.save(otpToken);

        // Send email
        emailService.sendOtpEmail(user.getEmail(), otp, user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("User registered successfully. Please verify OTP.")
                .build();
    }

    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        // Verify the user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: " + request.getEmail() + "."));

        // Verify the OTP
        OtpToken otpToken = otpTokenRepository.findByUserAndOtp(user, request.getOtp())
                .orElseThrow(() -> new BadRequestException("OTP is valid."));

        // Verify OTP expiration
        if (otpToken.getExpiresAt().isBefore(Instant.now())) {
            otpTokenRepository.delete(otpToken); // clean up
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        user.setVerified(true);
        userRepository.save(user);
        otpTokenRepository.delete(otpToken);

        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Account verified successfully.")
                .build();
    }

    public MessageResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: "
                                + request.getEmail() + "."));

        if (user.isVerified()) {
            throw new BadRequestException("User is already verified.");
        }

        // Delete old OTP if exists
        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);

        // Generate new OTP
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        OtpToken newOtp = OtpToken.builder()
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(300)) // 5 minutes
                .user(user)
                .build();

        otpTokenRepository.save(newOtp);

        // Send new OTP via email
        emailService.sendOtpEmail(user.getEmail(), otp, user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("A new OTP has been sent to your email.")
                .build();
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: "
                                + request.getEmail() + "."));

        // Delete old OTP if exists
        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);

        // Generate new OTP token
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        OtpToken otpToken = OtpToken.builder()
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(300))
                .user(user)
                .build();

        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(user.getEmail(), otp, user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("OTP sent to your email for password reset.")
                .build();

    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: "
                                + request.getEmail() + "."));

        OtpToken otpToken = otpTokenRepository.findByUserAndOtp(user, request.getOtp())
                .orElseThrow(() -> new BadRequestException("OTP is invalid"));

        if (otpToken.getExpiresAt().isBefore(Instant.now())) {
            otpTokenRepository.delete(otpToken);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpTokenRepository.delete(otpToken);

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Password reset successfully.")
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: "
                                + request.getEmail() + "."));

        if (!user.isVerified()) {
            throw new UnauthorizedException("Account not verified. Please verify OTP first.");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return AuthResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Login Successful")
                .token(token)
                .user(UserResponse.fromUser(user))
                .build();
    }
}
