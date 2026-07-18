package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private List<FieldErrorDto> fieldErrors;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FieldErrorDto {
        private String field;
        private String message;
    }
}
