package com.pratham.finvera.entity;

import com.pratham.finvera.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_verified")
    @Builder.Default
    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
