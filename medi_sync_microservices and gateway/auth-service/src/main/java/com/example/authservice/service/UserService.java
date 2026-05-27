package com.example.authservice.service;

import com.example.authservice.model.user;
import com.example.authservice.repository.userRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final userRepository userRepository;

    public UserService(userRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Optional<user> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
