package dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessRequestDto {

    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String website;

    private Double latitude;
    private Double longitude;

    @Size(max = 255)
    private String address;

    @Size(max = 50)
    private String addressLine2;

    private String city;

    @Size(min = 2, max = 2, message = "State must be a 2-letter abbreviation")
    private String state;

    private String zip;

    @Valid
    private List<BusinessHoursDto> hours;
}
