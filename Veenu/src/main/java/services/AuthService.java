package services;

import dtos.AuthResponseDto;
import dtos.LoginRequestDto;
import dtos.RegisterRequestDto;
import model.User;
import model.enums.EntityStatus;
import model.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repositories.UserRepository;
import security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("This username is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setNeighborhood(request.getNeighborhood());
        user.setRole(UserRole.COMMUNITY);
        user.setEntityStatus(EntityStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setTrustScore(0);

        User saved = userRepository.save(user);

        // TODO: trigger email verification send (Resend) here

        String token = jwtService.generateToken(saved);
        return buildResponse(saved, token);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (user.getEntityStatus() == EntityStatus.TAKEN_DOWN) {
            throw new IllegalStateException("This account has been taken down: " + user.getSuspensionReason());
        }

        String token = jwtService.generateToken(user);
        return buildResponse(user, token);
    }

    private AuthResponseDto buildResponse(User user, String token) {
        return AuthResponseDto.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
