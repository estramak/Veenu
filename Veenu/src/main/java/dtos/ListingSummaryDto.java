package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ListingSummaryDto {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String locationType;
    private Boolean hasBusiness;
}
