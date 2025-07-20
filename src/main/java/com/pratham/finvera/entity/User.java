package com.pratham.finvera.entity;

import java.time.LocalDate;

import com.pratham.finvera.enums.AuthProvider;
import com.pratham.finvera.enums.Gender;
import com.pratham.finvera.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(nullable = false)
    private String name;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private Gender gender; // Enum: MALE, FEMALE, OTHER

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String password;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}