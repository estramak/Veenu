package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EventResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long listingId;
    private Long businessId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean isFree;
    private Double price;
    private Integer capacity;
    private String registrationUrl;
    private Boolean postedByOwner;
}
