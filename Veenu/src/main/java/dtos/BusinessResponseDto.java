package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BusinessResponseDto {
    private Long id;
    private Long listingId;
    private String name;
    private String email;
    private String phone;
    private String website;
    private String entityStatus;
    private Boolean isVerified;
    private Boolean flaggedForReview;
    private Long submittedByUserId;
}
