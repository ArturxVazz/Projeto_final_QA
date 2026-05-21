package com.biblioteca.service;

import com.biblioteca.dto.request.LoginRequest;
import com.biblioteca.dto.request.RegisterRequest;
import com.biblioteca.dto.response.AuthResponse;
import com.biblioteca.dto.response.UserResponse;
import com.biblioteca.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);
        UserResponse user = userService.findByUsername(request.getUsername());
        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .username(user.getUsername())
            .email(user.getEmail())
            .build();
    }

    public UserResponse register(RegisterRequest request) {
        return userService.register(request);
    }
}
