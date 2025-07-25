package com.pratham.finvera.service;

import com.pratham.finvera.dto.ChangePasswordRequest;
import com.pratham.finvera.dto.UpdateUserProfileRequest;
import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.Gender;
import com.pratham.finvera.exception.BadRequestException;
import com.pratham.finvera.exception.ResourceNotFoundException;
import com.pratham.finvera.payload.GetUserResponse;
import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.payload.UserResponse;
import com.pratham.finvera.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public MessageResponse getCurrentUserProfile() {

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + currentEmail + "."));

        return GetUserResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("User found with email: " + currentEmail + ".")
                .user(UserResponse.fromUser(user))
                .build();
    }

    public MessageResponse updateUserProfile(UpdateUserProfileRequest request) {

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + currentEmail + "."));

        user.setName(request.getName());
        user.setPhone(request.getPhone() == null || request.getPhone().isBlank() ? null : request.getPhone());
        user.setDob(request.getDob() == null ? null : request.getDob());
        user.setGender(request.getGender() == null || request.getGender().isBlank() ? null
                : Gender.valueOf(request.getGender()));
        user.setProfilePhoto(request.getProfilePhotoUrl() == null || request.getProfilePhotoUrl().isBlank() ? null
                : request.getProfilePhotoUrl());

        userRepository.save(user);

        return GetUserResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Profile updated Successfully.")
                .user(UserResponse.fromUser(user))
                .build();
    }

    public MessageResponse changePassword(ChangePasswordRequest request) {

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail).orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + currentEmail + "."));

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("Old password and new password cannot be same.");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return MessageResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Password updated successfully.")
                .build();
    }
}