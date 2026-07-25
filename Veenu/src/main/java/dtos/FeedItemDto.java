package dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FeedItemDto {

    // "EVENT" or "NOTE", tells frontend which shape to render
    private String type;
    private Long id;

    // event name, or short preview of note content
    private String title;
    private Long listingId;
    private Long businessId; // null for notes and for listing-only events

    // timestamp for feed sorting, exposed so date can be shown to users
    private LocalDateTime relevantTimestamp;
    private Boolean isEvent; // convenience flag
}
