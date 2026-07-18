package controllers;

import dtos.AuthResponseDto;
import dtos.LoginRequestDto;
import dtos.RegisterRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private static final int COOKIE_MAX_AGE_SECONDS = 30 * 24 * 60 * 60;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request,
            HttpServletResponse response
    ) {
        AuthResponseDto result = authService.register(request);
        setJwtCookie(response, result.getToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        AuthResponseDto result = authService.login(request);
        setJwtCookie(response, result.getToken());
        return ResponseEntity.ok(result);
    }

    // sets the HttpOnly cookie for web clients. Mobile clients use
    // the token field in the response body instead (Authorization header
    private void setJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
            .httpOnly(true)
                .secure(false) // set true once served over HTTpS in prod
                .path("/")
                .maxAge(COOKIE_MAX_AGE_SECONDS)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
