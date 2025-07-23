package com.pratham.finvera.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.AuthProvider;
import com.pratham.finvera.enums.Gender;
import com.pratham.finvera.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String profilePhoto;
    private String name;
    private String email;
    private String phone;
    private LocalDate dob;
    private Gender gender;
    private AuthProvider authProvider;
    private boolean verified;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .profilePhoto(user.getProfilePhoto())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dob(user.getDob())
                .gender(user.getGender())
                .authProvider(user.getAuthProvider())
                .verified(user.isVerified())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}