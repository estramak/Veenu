package com.veenu.veenu_backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.veenu.veenu_backend.dto.AuthResponse;
import com.veenu.veenu_backend.dto.LoginRequest;
import com.veenu.veenu_backend.dto.RegisterRequest;
import com.veenu.veenu_backend.model.MagicLinkToken;
import com.veenu.veenu_backend.model.TokenPurpose;
import com.veenu.veenu_backend.model.User;
import com.veenu.veenu_backend.model.UserRole;
import com.veenu.veenu_backend.repository.MagicLinkTokenRepository;
import com.veenu.veenu_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${app.base.url}")
    private String baseUrl;

    // REGISTER
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.COMMUNITY);
        user.setEmailVerified(false);
        userRepository.save(user);

        sendVerificationEmail(request.getEmail());

        return "Registration successful. Please check your email to verify your account.";
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getIdentifier())
                .or(() -> userRepository.findByUsername(request.getIdentifier()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.getEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    // VERIFY EMAIL
    public String verifyEmail(String token) {
        MagicLinkToken magicToken = magicLinkTokenRepository
                .findByTokenAndPurpose(token, TokenPurpose.EMAIL_VERIFICATION)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (magicToken.getUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (magicToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = userRepository.findByEmail(magicToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        magicToken.setUsed(true);
        magicLinkTokenRepository.save(magicToken);

        return "Email verified successfully. You can now log in.";
    }

    // SEND VERIFICATION EMAIL
    private void sendVerificationEmail(String email) {
        String token = UUID.randomUUID().toString();

        MagicLinkToken magicToken = new MagicLinkToken();
        magicToken.setToken(token);
        magicToken.setEmail(email);
        magicToken.setPurpose(TokenPurpose.EMAIL_VERIFICATION);
        magicLinkTokenRepository.save(magicToken);

        String verificationLink = baseUrl + "/auth/verify?token=" + token;

        Resend resend = new Resend(resendApiKey);
        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to("defaul89@gmail.com")
                .subject("Verify your Veenu account")
                .html("<h2>Welcome to Veenu</h2>" +
                        "<p>Click the link below to verify your email address.</p>" +
                        "<a href='" + verificationLink + "'>Verify my email</a>" +
                        "<p>This link expires in 15 minutes.</p>")
                .build();

        try {
            resend.emails().send(emailOptions);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send verification email");
        }
    }
}
