package com.pratham.finvera.security;

import com.pratham.finvera.entity.User;

import lombok.AllArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // or user.getPhone() if you use phone as login
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // can customize if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // can implement account lock logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isVerified(); // only allow verified users to log in
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> user.getRole().name()); // Use actual role from DB
    }
}