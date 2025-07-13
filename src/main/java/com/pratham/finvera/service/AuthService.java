package com.pratham.finvera.service;

import com.pratham.finvera.dto.AuthRequest;
import com.pratham.finvera.dto.ForgotPasswordRequest;
import com.pratham.finvera.dto.RegisterRequest;
import com.pratham.finvera.dto.ResendOtpRequest;
import com.pratham.finvera.dto.ResetPasswordRequest;
import com.pratham.finvera.dto.VerifyOtpRequest;
import com.pratham.finvera.entity.OtpToken;
import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.OtpPurpose;
import com.pratham.finvera.enums.Role;
import com.pratham.finvera.exception.BadRequestException;
import com.pratham.finvera.exception.ResourceNotFoundException;
import com.pratham.finvera.exception.UnauthorizedException;
import com.pratham.finvera.payload.AuthResponse;
import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.payload.OtpVerifiedResponse;
import com.pratham.finvera.payload.UserResponse;
import com.pratham.finvera.repository.OtpTokenRepository;
import com.pratham.finvera.repository.UserRepository;
import com.pratham.finvera.security.CustomUserDetails;
import com.pratham.finvera.util.JwtUtils;
import com.pratham.finvera.util.OtpUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final OtpUtil otpUtil;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    public MessageResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Role.ROLE_USER)
                .password(passwordEncoder.encode(request.getPassword()))
                .isVerified(false) // set true after OTP later
                .build();

        userRepository.save(user);

        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);
        OtpToken otpToken = otpUtil.createOtpToken(user, OtpPurpose.REGISTER);
        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(user.getEmail(), otpToken.getOtp(), user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("User registered successfully. Please verify OTP.")
                .build();
    }

    public MessageResponse verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail() + "."));

        OtpPurpose purpose = request.getPurpose();

        OtpToken otpToken = otpTokenRepository.findByPurposeAndUserAndOtp(purpose, user, request.getOtp())
                .orElseThrow(() -> new BadRequestException("OTP is invalid."));

        if (otpToken.getExpiresAt().isBefore(Instant.now())) {
            otpTokenRepository.delete(otpToken); // clean up
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        return switch (purpose) {
            case REGISTER -> handleRegisterOtpVerification(user, otpToken);
            case FORGOT_PASSWORD -> handleForgotPasswordOtpVerification(user, otpToken);
            default -> throw new BadRequestException("Unsupported OTP purpose.");
        };
    }

    private MessageResponse handleRegisterOtpVerification(User user, OtpToken otpToken) {

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

    private OtpVerifiedResponse handleForgotPasswordOtpVerification(User user, OtpToken otpToken) {

        otpUtil.attachSessionToken(otpToken);

        otpTokenRepository.save(otpToken);

        return OtpVerifiedResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("OTP verified.")
                .otpSessionToken(otpToken.getOtpSessionToken())
                .build();
    }

    public MessageResponse resendOtp(ResendOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "
                        + request.getEmail() + "."));

        OtpPurpose purpose = request.getPurpose();

        if (purpose.equals(OtpPurpose.REGISTER) && user.isVerified()) {
            throw new BadRequestException("User is already verified.");
        }

        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);
        OtpToken otpToken = otpUtil.createOtpToken(user, request.getPurpose());
        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(user.getEmail(), otpToken.getOtp(), user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("A new OTP has been sent to your email.")
                .build();
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "
                        + request.getEmail() + "."));

        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);
        OtpToken otpToken = otpUtil.createOtpToken(user, OtpPurpose.FORGOT_PASSWORD);
        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(user.getEmail(), otpToken.getOtp(), user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("OTP sent to your email for password reset.")
                .build();
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "
                        + request.getEmail() + "."));

        OtpToken otpToken = otpTokenRepository.findByUserAndOtpSessionToken(user, request.getOtpSessionToken())
                .orElseThrow(() -> new BadRequestException("Invalid OTP session token"));

        if (otpToken.getExpiresAt().isBefore(Instant.now())) {
            otpTokenRepository.delete(otpToken); // Clean up expired token
            throw new BadRequestException("OTP session token has expired.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpTokenRepository.delete(otpToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName());

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Password reset successfully.")
                .build();
    }

    public AuthResponse login(AuthRequest request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            throw new UnauthorizedException("Account not verified. Please verify OTP first.");
        } catch (AuthenticationException e) {
            throw new BadRequestException("Invalid email or password.");
        }

        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole());
        log.info("User {} logged in with role {}", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Login Successful.")
                .token(token)
                .user(UserResponse.fromUser(user))
                .build();
    }
}