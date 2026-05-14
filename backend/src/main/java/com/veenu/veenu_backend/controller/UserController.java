package com.veenu.veenu_backend.controller;

import com.veenu.veenu_backend.dto.UserResponse;
import com.veenu.veenu_backend.dto.UserUpdateRequest;
import com.veenu.veenu_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me — get current user profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // PUT /api/users/me — update current user profile
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }
}