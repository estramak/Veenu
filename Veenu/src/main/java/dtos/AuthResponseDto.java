package dtos;

import lombok.*;

// returned on successful register/login

@Getter
@Builder
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String role;
}
