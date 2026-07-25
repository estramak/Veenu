package dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuspendRequestDto {

    @Size(max = 500, message = "Reason must be 500 characters or fewer")
    private String reason;
}
