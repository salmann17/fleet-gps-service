package com.example.fleetgpsservice.service;

import com.example.fleetgpsservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String HARDCODED_USERNAME = "admin";
    private static final String HARDCODED_PASSWORD = "password";

    private final JwtService jwtService;

    public Optional<String> authenticate(String username, String password) {
        if (HARDCODED_USERNAME.equals(username) && HARDCODED_PASSWORD.equals(password)) {
            return Optional.of(jwtService.generateToken(username));
        }
        return Optional.empty();
    }
}

