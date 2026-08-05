package dtos;

import jakarta.validation.constraints.Size;
import lombok.*;
import model.enums.LocationType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateListingRequestDto {

    @Size(max = 100, message = "Name must be under 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must be under 1000 characters")
    private String description;

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

    @Size(max = 50)
    private String country;

    private LocationType locationType;
}
