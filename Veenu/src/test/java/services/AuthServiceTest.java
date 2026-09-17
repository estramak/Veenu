package services;

import dtos.AuthResponseDto;
import dtos.LoginRequestDto;
import dtos.RegisterRequestDto;
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
import static org.mockito.Mockito.*;

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

    /*
    * LOGIN TESTS
    */

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

    /*
    * REGISTRATION
    */

    @Test
    void register_duplicateEmail_throwsIllegalArgumentException() {
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);

        RegisterRequestDto register = new RegisterRequestDto();
        register.setUsername("jdoe");
        register.setEmail("jdoe@example.com");
        register.setPassword("correct-password");
        register.setDisplayName("J Doe");
        register.setNeighborhood("correct-neighborhood");

        assertThatThrownBy(() -> authService.register(register))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An account with this email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsIllegalArgumentException() {
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("jdoe")).thenReturn(true);

        RegisterRequestDto register = new RegisterRequestDto();
        register.setUsername("jdoe");
        register.setEmail("jdoe@example.com");
        register.setPassword("correct-password");
        register.setDisplayName("J Doe");
        register.setNeighborhood("correct-neighborhood");

        assertThatThrownBy(() -> authService.register(register))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This username is already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_happyPath_returnsTokenAndUserInfo() {
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(passwordEncoder.encode("Str0ng!Pass")).thenReturn("hashed-new-password");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("jdoe");
        savedUser.setEmail("jdoe@example.com");
        savedUser.setPasswordHash("hashed-new-password");
        savedUser.setDisplayName("J Doe");
        savedUser.setNeighborhood("correct-neighborhood");
        savedUser.setRole(UserRole.COMMUNITY);
        savedUser.setEntityStatus(EntityStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("fake-jwt-token");

        RegisterRequestDto register = new RegisterRequestDto();
        register.setUsername("jdoe");
        register.setEmail("jdoe@example.com");
        register.setPassword("Str0ng!Pass");
        register.setDisplayName("J Doe");
        register.setNeighborhood("correct-neighborhood");

        AuthResponseDto response = authService.register(register);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUsername()).isEqualTo("jdoe");
        assertThat(response.getEmail()).isEqualTo("jdoe@example.com");
        assertThat(response.getRole()).isEqualTo("COMMUNITY");

        verify(userRepository).save(any(User.class));
    }
}
