package com.pratham.finvera.config;

import com.pratham.finvera.entity.User;
import com.pratham.finvera.enums.Role;
import com.pratham.finvera.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Value("${admin.default.name}")
    private String adminName;

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.existsByEmail(adminEmail);
        if (!adminExists) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .name(adminName)
                    .phone("0000000000") // placeholder
                    .role(Role.ROLE_ADMIN)
                    .isVerified(true)
                    .build();

            userRepository.save(admin);
            System.out.println("Default admin created: " + adminEmail);
        }
    }
}
