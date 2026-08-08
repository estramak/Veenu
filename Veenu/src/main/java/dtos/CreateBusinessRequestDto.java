package dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.bridge.IMessage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessRequestDto {

    @NotBlank(message = "Business name is required")
    @Size(max = 100, message = "Business name must be under 100 characters")
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String website;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String address;

    @Size(max = 50)
    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be a 2-letter abbreviation")
    private String state;

    @NotBlank(message = "Zip is required")
    private String zip;

    // set when the frontend showed nearby-match candidates, null means user said "no match"
    private Long confirmedListingId;
}
