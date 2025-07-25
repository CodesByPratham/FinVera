package com.pratham.finvera.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.pratham.finvera.config.GoogleSignInProperties;
import com.pratham.finvera.dto.AuthRequest;
import com.pratham.finvera.dto.ForgotPasswordRequest;
import com.pratham.finvera.dto.RegisterRequest;
import com.pratham.finvera.dto.ResendOtpRequest;
import com.pratham.finvera.dto.ResetPasswordRequest;
import com.pratham.finvera.dto.VerifyOtpRequest;
import com.pratham.finvera.entity.OtpToken;
import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.AuthProvider;
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
import com.pratham.finvera.util.JwtUtil;
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
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtils;
    private final OtpUtil otpUtil;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final GoogleSignInProperties googleSignInProperties;

    public MessageResponse register(RegisterRequest request) {

        Optional<User> presentUser = userRepository.findByEmail(request.getEmail());
        User user;

        if (presentUser.isPresent()) {

            user = presentUser.get();

            if (user.isVerified()) {
                // Case 2: User found with given email, and is verified.
                throw new BadRequestException("Email already in use.");
            }

            // Case 3: User found with given email, and is not verified so update the user.
            user.setName(request.getName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.ROLE_USER);
            user.setAuthProvider(AuthProvider.LOCAL);
        } else {
            // Case 1: No such user found with given email, hence register the user.
            user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .role(Role.ROLE_USER)
                    .authProvider(AuthProvider.LOCAL)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();
            userRepository.save(user);
        }

        userRepository.save(user);

        sendOtp(user, OtpPurpose.REGISTER);

        return buildSuccessResponse("User registered successfully. Please verify OTP.");
    }

    public MessageResponse verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail() + "."));

        OtpPurpose purpose = request.getPurpose();
        OtpToken otpToken = otpTokenRepository.findByPurposeAndUserAndOtp(purpose, user, request.getOtp())
                .orElseThrow(() -> new BadRequestException("OTP is invalid."));

        if (otpToken.getExpiresAt().isBefore(Instant.now())) {
            otpTokenRepository.delete(otpToken);
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

        return buildSuccessResponse("Account verified successfully.");
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

        sendOtp(user, purpose);

        return buildSuccessResponse("A new OTP has been sent to your email.");
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "
                        + request.getEmail() + "."));

        sendOtp(user, OtpPurpose.FORGOT_PASSWORD);

        return buildSuccessResponse("OTP sent to your email for password reset.");
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

        return buildSuccessResponse("Password reset successfully.");
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

    public AuthResponse loginWithGoogle(String idTokenString) {

        // Verify the received Google ID token using Google's public certificates
        // This ensures the token is not tampered with and is issued for your app
        GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);

        // Extract payload (user info) from the verified ID token
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // Try to find the user by email in the database
        User user = userRepository.findByEmail(email).orElseGet(() -> {

            // If user does not exist, register them using data from Google
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .profilePhoto(picture)
                    .password(passwordEncoder.encode("SOCIAL_LOGIN"))
                    .isVerified(true)
                    .authProvider(AuthProvider.GOOGLE)
                    .role(Role.ROLE_USER)
                    .build();

            userRepository.save(newUser);
            emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getName());
            return newUser;
        });

        // Prevent users from logging in with Google if their account was created via password
        if (user.getAuthProvider() != AuthProvider.GOOGLE) {
            throw new BadRequestException(
                    "This email is already registered via password. Please log in using email and password.");
        }

        log.info("User {} logged in with role {}", user.getEmail(), user.getRole());

        String jwt = jwtUtils.generateToken(user.getEmail(), Role.ROLE_USER);

        return AuthResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Login via Google successful.")
                .token(jwt)
                .user(UserResponse.fromUser(user))
                .build();
    }

    /* 
     * This method receives an idTokenString, which is a JWT (JSON Web Token) issued 
     * by Google after a user logs in using Google Sign-In.
     * 
     * The method aims to verify the token's authenticity using Google's public keys 
     * and confirm that it was issued for your app.
     * 
     * It returns a GoogleIdToken object if valid, or throws an UnauthorizedException 
     * if the token is invalid.
     */
    private GoogleIdToken verifyGoogleIdToken(String idTokenString) {
        try {
            /* 
             * GoogleIdTokenVerifier.Builder is used to build a verifier object that will perform the actual checks.
             * It's part of the Google API Client Libraries (com.google.api.client.googleapis.auth.oauth2).
             * The builder pattern is used here to configure the verifier before building it.
             */
            return new GoogleIdTokenVerifier.Builder(
                    // NetHttpTransport is a transport mechanism used to fetch Google's public keys and certificates over HTTPS.
                    new NetHttpTransport(),
                    // GsonFactory is used to parse the JSON data (like the certificates and token payload) from Google's servers.
                    new GsonFactory())

                    /* 
                     * .setAudience(...) ensures that the token was intended for your application.
                     * he aud (audience) field in the ID token must match your app’s Client ID.
                     * This is critical to prevent token reuse across apps.
                     * Collections.singletonList(...) is used because Google ID tokens support 
                     * multiple audiences, but you only expect one.
                     */
                    .setAudience(Collections.singletonList(googleSignInProperties.getClientId()))
                    .build() // Finalizes the configuration and builds the GoogleIdTokenVerifier instance with all settings applied

                    /* 
                     * This line performs the actual verification of the token string:
                     * Signature Verification: Checks that the token was signed by Google using its public keys.
                     * Expiration Check: Ensures the token hasn’t expired (exp field).
                     * Audience Check: Confirms that the token is meant for your app.
                     * Issuer Check: Ensures the token comes from a trusted source (https://accounts.google.com or accounts.google.com).
                     * 
                     * If verification passes, it returns a GoogleIdToken object.
                     * If verification fails (invalid, expired, tampered), it returns null.
                     */
                    .verify(idTokenString);

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Google ID token.");
        }
    }

    private void sendOtp(User user, OtpPurpose purpose) {

        otpTokenRepository.findByUser(user).ifPresent(otpTokenRepository::delete);
        OtpToken otpToken = otpUtil.createOtpToken(user, purpose);
        otpTokenRepository.save(otpToken);
        emailService.sendOtpEmail(user.getEmail(), otpToken.getOtp(), user.getName());
    }

    private MessageResponse buildSuccessResponse(String message) {
        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message(message)
                .build();
    }
}