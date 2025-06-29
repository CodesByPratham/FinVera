package com.pratham.finvera.security;

import com.pratham.finvera.entity.User;
import com.pratham.finvera.entity.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // e.g., ROLE_USER
                .collect(Collectors.toSet());
    }

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
}
