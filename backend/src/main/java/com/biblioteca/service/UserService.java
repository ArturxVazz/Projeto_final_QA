package com.biblioteca.service;

import com.biblioteca.dto.request.RegisterRequest;
import com.biblioteca.dto.response.UserResponse;
import com.biblioteca.exception.DuplicateResourceException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.User;
import com.biblioteca.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username já em uso: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email já em uso: " + request.getEmail());
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .active(true)
            .build();

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public UserResponse findById(String id) {
        return userRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    public UserResponse findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
