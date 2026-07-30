package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminQueueResponseDto {
    private List<AdminQueueItemDto> flaggedBusinesses;
    private List<AdminQueueItemDto> pendingBusinesses;
    private List<AdminQueueItemDto> pendingUsers;
    private List<AdminQueueItemDto> pendingListings;
    private List<AdminQueueItemDto> suspendedListings;
}
