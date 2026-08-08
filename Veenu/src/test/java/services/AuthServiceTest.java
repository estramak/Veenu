package services;

import dtos.AuthResponseDto;
import dtos.LoginRequestDto;
import model.User;
import model.enums.EntityStatus;
import model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import repositories.UserRepository;
import security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setEmail("jdoe@example.com");
        user.setPasswordHash("hashed-password");
        user.setDisplayName("J Doe");
        user.setRole(UserRole.COMMUNITY);
        user.setEntityStatus(EntityStatus.ACTIVE);
    }

    @Test
    void login_unknownEmail_throwsIllegalArgumentException() {

        when(userRepository.findByEmail("nobody@example.com"))
                .thenReturn(Optional.empty());

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        assertThatThrownBy(() -> authService
                .login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_WrongPassword_throwsIllegalArgumentException() {

        when(userRepository.findByEmail("jdoe@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("jdoe@example.com");
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> authService
                .login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_takenDownAccount_throwsIllegalStateException() {
        user.setEntityStatus(EntityStatus.TAKEN_DOWN);
        user.setSuspensionReason("Repeated policy violations");

        when(userRepository.findByEmail("jdoe@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password"))
                .thenReturn(true);

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("jdoe@example.com");
        request.setPassword("correct-password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This account has been taken down: Repeated policy violations");
    }

    @Test
    void login_happyPath_returnsTokenAndUserInfo() {
        when(userRepository.findByEmail("jdoe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("jdoe@example.com");
        request.setPassword("correct-password");

        AuthResponseDto response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("jdoe");
        assertThat(response.getEmail()).isEqualTo("jdoe@example.com");
        assertThat(response.getRole()).isEqualTo("COMMUNITY");
    }
}
