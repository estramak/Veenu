package com.veenu.veenu_backend.service;

import com.veenu.veenu_backend.dto.UserResponse;
import com.veenu.veenu_backend.dto.UserUpdateRequest;
import com.veenu.veenu_backend.model.User;
import com.veenu.veenu_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getNeighborhood() != null) {
            user.setNeighborhood(request.getNeighborhood());
        }
        if (request.getUsername() != null) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setNeighborhood(user.getNeighborhood());
        response.setRole(user.getRole());
        response.setTrustScore(user.getTrustScore());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}