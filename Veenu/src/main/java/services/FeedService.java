package services;

import dtos.FeedItemDto;
import model.Event;
import model.Note;
import org.springframework.stereotype.Service;
import repositories.EventRepository;
import repositories.NoteRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FeedService {

    private static final int SOURCE_FETCH_LIMIT = 100;

    private final EventRepository eventRepository;
    private final NoteRepository noteRepository;

    public FeedService(EventRepository eventRepository, NoteRepository noteRepository) {
        this.eventRepository = eventRepository;
        this.noteRepository = noteRepository;
    }

    // type
    public List<FeedItemDto> getFeed(int page, int size, String type) {
        LocalDateTime now = LocalDateTime.now();
        List<FeedItemDto> items = new ArrayList<>();

        if (!"notes".equalsIgnoreCase(type)) {
            eventRepository.findByStartDateTimeAfterOrderByStartDateTimeAsc(now).stream()
                    .limit(SOURCE_FETCH_LIMIT)
                    .map(this::toFeedItem)
                    .forEach(items::add);
        }

        if (!"events".equalsIgnoreCase(type)) {
            noteRepository.findByOnHoldFalseOrderByCreatedAtDesc().stream()
                    .limit(SOURCE_FETCH_LIMIT)
                    .map(this::toFeedItem)
                    .forEach(items::add);
        }

        // sort by closeness to "now"
        items.sort(Comparator.comparing(item ->
                Duration.between(now, item.getRelevantTimestamp()).abs()));

        int fromIndex = Math.min(page * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());
        return items.subList(fromIndex, toIndex);
    }

    private FeedItemDto toFeedItem(Event event) {
        return FeedItemDto.builder()
            .type("EVENT")
            .id(event.getId())
            .title(event.getName())
            .listingId(event.getListing().getId())
            .businessId(event.getBusiness() != null ? event.getBusiness().getId() : null)
            .relevantTimestamp(event.getStartDateTime())
            .isEvent(true)
            .build();
    }

    private FeedItemDto toFeedItem(Note note) {
        String preview = note.getContent().length() > 100
            ? note.getContent().substring(0, 100) + "..."
            : note.getContent();

        return FeedItemDto.builder()
            .type("NOTE")
            .id(note.getId())
            .title(preview)
            .listingId(note.getListing() != null ? note.getListing().getId() : null)
            .businessId(null)
            .relevantTimestamp(note.getCreatedAt())
            .isEvent(false)
            .build();
    }
}
