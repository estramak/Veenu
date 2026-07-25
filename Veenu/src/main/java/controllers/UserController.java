package controllers;

import dtos.UpdateProfileRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDto request,
            @NotNull Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updateProfile(userId, request);
        return ResponseEntity.noContent().build();
    }
}
