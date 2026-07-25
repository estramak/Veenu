package security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import model.enums.EntityStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import repositories.UserRepository;

import java.io.IOException;
import java.util.List;

/**
 * Reads a JWT from either the "jwt" HttpOnly cookie (web clients) or the
 * Authorization header (mobile clients, per Architecture.md auth flow),
 * validates it, and populates the SecurityContext so downstream
 * @PreAuthorize checks and SecurityFilterChain rules can use it.
 *
 * Performs a DB lookup on every authenticated request to check the
 * user's current EntityStatus, so suspended/banned users are rejected
 * immediately rather than waiting for their token to expire. This
 * trades a small amount of per-request latency for instant enforcement.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                Long userId = jwtService.extractUserId(token);
                String role = jwtService.extractRole(token);

                if (jwtService.isTokenValid(token, userId)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    User user = userRepository.findById(userId).orElse(null);

                    boolean blocked = user == null
                            || user.getEntityStatus() == EntityStatus.BANNED
                            || user.getEntityStatus() == EntityStatus.SUSPENDED;

                    if (!blocked) {
                        List<GrantedAuthority> authorities =
                                List.of(new SimpleGrantedAuthority("ROLE_" + role));

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userId, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Invalid/expired/malformed token — leave SecurityContext
                // empty, request falls through as unauthenticated and gets
                // rejected by the SecurityFilterChain rules if the route
                // requires auth.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // Mobile: Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Web: HttpOnly cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}

