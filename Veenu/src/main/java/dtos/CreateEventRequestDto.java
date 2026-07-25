package dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequestDto {

    @NotBlank(message = "Event name is required")
    @Size(max = 150, message = "Event name must be under 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must be under 1000 characters")
    private String description;

    // At least one of listingId, businessId, or (latitude + longitude)
    // must be provided — enforced in the service, not here, since Bean
    // Validation doesn't cleanly express "one of these" across fields.
    private Long listingId;

    private Long businessId;

    // Used only when neither listingId nor businessId is given — a new
    // pin drop / address for a standalone event with no existing Listing.
    private Double latitude;
    private Double longitude;
    private String address;
    private String addressLine2;
    private String city;
    private String state;
    private String zip;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @NotNull(message = "isFree must be specified")
    private Boolean isFree;

    private BigDecimal price;

    private Integer capacity;

    private String registrationUrl;
}
