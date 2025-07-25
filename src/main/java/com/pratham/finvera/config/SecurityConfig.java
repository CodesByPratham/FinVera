package com.pratham.finvera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.pratham.finvera.security.AuthEntryPointJwt;
import com.pratham.finvera.security.CustomUserDetailsService;
import com.pratham.finvera.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthEntryPointJwt authEntryPointJwt;

    // Define the security filter chain that configures HTTP security for the app
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /*
         * Reference: https://shorturl.at/kniSr
         * CSRF protects against what is aptly titled a Cross Site Request Forgery.
         * In this type of attack, a token such as a session ID, is stored in the
         * browser's cookies. The default behavior is to send the cookies with a
         * request to a site. Someone can exploit this default behavior to
         * impersonate you and do malicious things.
         */
        http.csrf(csrf -> csrf.disable())
                // Disable session creation; use JWT instead
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible endpoints like login and register
                        .requestMatchers("/api/auth/**").permitAll()
                        // Allow public GET access to /public/**
                        .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                        // Restrict /api/admin/** to users with ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Restrict /api/user/** to users with USER role
                        .requestMatchers("/api/user/**").hasRole("USER")
                        // All other requests require authentication
                        .anyRequest().authenticated())
                // Set the authentication provider
                .authenticationProvider(authenticationProvider())
                // Add JWT filter before Spring’s default auth filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Handle exception that occurs in Spring Security
                .exceptionHandling((handling) -> handling.authenticationEntryPoint(authEntryPointJwt));

        return http.build(); // Build and return the security configuration
    }

    // Define and configure the AuthenticationProvider using custom UserDetailsService and password encoder.
    // One can use In memory authentication provider, LDAP and many others as per the requirements.
    @Bean
    AuthenticationProvider authenticationProvider() {
        // Use DAO-based authentication
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder()); // Set password encoder to BCrypt
        return provider;
    }

    // Define the password encoder bean using BCrypt (a strong hashing algorithm)
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Define the authentication manager bean to retrieve AuthenticationManager from
    // AuthenticationConfiguration
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); // Get the authentication manager from configuration
    }
}