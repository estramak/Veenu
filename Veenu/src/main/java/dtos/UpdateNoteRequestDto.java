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
public class UpdateNoteRequestDto {

    @NotBlank(message = "Note content is required")
    @Size(max = 280, message = "Note must be 280 characters or fewer")
    private String content;
}
