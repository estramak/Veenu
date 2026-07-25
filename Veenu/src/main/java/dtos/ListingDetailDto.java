package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ListingDetailDto {
    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private String addressLine2;
    private String city;
    private String state;
    private String zip;
    private String locationType;
    private BusinessResponseDto business;
    private List<EventResponseDto> upcomingEvents;
}
