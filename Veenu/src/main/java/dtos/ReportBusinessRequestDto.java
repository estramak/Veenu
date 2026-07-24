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
public class ReportBusinessRequestDto {
    @Size(max = 280, message = "Reason must be under 280 characters")
    private String reason;
}
