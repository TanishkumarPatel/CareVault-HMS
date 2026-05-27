package com.example.authservice.repository;

import com.example.authservice.model.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface userRepository extends JpaRepository<user, UUID> {
    Optional<user> findByEmail(String email);
}
