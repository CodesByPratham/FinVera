package com.pratham.finvera.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pratham.finvera.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
