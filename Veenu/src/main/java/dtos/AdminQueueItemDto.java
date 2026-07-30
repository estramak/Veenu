package dtos;

import lombok.AllArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminQueueItemDto {
    private Long id;
    private String entityType;
    private String name;
    private String status;
    private String reason;
    private Boolean flagged;

}
