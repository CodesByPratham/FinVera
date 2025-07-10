package com.pratham.finvera.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pratham.finvera.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}