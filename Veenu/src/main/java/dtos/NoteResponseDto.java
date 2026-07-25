package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NoteResponseDto {
    private Long id;
    private String content;
    private Long listingId;
    private Long eventId;
    private Long authorId;
    private Boolean onHold;
    private LocalDateTime createdAt;
}
