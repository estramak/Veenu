package dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteRequestDto {

    @NotBlank(message = "note content is required")
    @Size(max = 280, message = "Notes must be 280 characters or fewer")
    private String content;

    private Long listingId;

    private Long eventId;
}
