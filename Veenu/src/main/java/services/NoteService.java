package services;

import dtos.CreateNoteRequestDto;
import dtos.NoteResponseDto;
import dtos.UpdateNoteRequestDto;
import model.Event;
import model.Listing;
import model.Note;
import model.User;
import org.springframework.stereotype.Service;
import repositories.EventRepository;
import repositories.ListingRepository;
import repositories.NoteRepository;
import repositories.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class NoteService {
    private static final List<String> AUTO_HOLD_TRIGGER_WORDS = List.of(
            "suspicious", "beware", "watch out", "soliciting", "door to door"
    );

    // can only be edited within this window
    private static final long EDIT_WINDOW_HOURS = 1;

    private final NoteRepository noteRepository;
    private final ListingRepository listingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public NoteService(
            NoteRepository noteRepository,
            ListingRepository listingRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.noteRepository = noteRepository;
        this.listingRepository = listingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public NoteResponseDto createNote(Long authorUserId, CreateNoteRequestDto request) {
        if (request.getListingId() == null & request.getEventId() == null) {
            throw new IllegalArgumentException(
                    "A note must be attached to a listing or an event"
            );
        }

        User author = userRepository.findById(authorUserId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Listing listing = null;
        if (request.getListingId() != null) {
            listing = listingRepository.findById(request.getListingId()).orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        }

        Event event = null;
        if (request.getEventId() != null) {
            event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        }

        Note note = new Note();
        note.setContent(request.getContent());
        note.setListing(listing);
        note.setEvent(event);
        note.setAuthor(author);
        note.setOnHold(containsTriggerWord(request.getContent()));

        Note saved = noteRepository.save(note);
        return toResponseDto(saved);

    }

    // public read
    public List<NoteResponseDto> getNotesForListing(Long listingId) {
        return noteRepository.findByListing_Id(listingId).stream()
                .filter(note -> !Boolean.TRUE.equals(note.getOnHold()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponseDto)
                .toList();
    }

    public List<NoteResponseDto> getNotesForEvent(Long eventId) {
        return noteRepository.findByEvent_Id(eventId).stream()
                .filter(note -> !Boolean.TRUE.equals(note.getOnHold()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponseDto)
                .toList();
    }

    //combined view for notes and events
    public List<NoteResponseDto> getNotesForEventPage(Long listingId, Long eventId, String filter) {
        List<Note> notes;
        if ("listing".equalsIgnoreCase(filter)) {
            notes = listingId != null ? noteRepository.findByListing_Id(listingId) : List.of();
        } else if ("event".equalsIgnoreCase(filter)) {
            notes = listingId != null ? noteRepository.findByEvent_Id(eventId) : List.of();
        } else {
            List<Note> listingNotes = listingId != null ? noteRepository.findByListing_Id(listingId) : List.of();
            List<Note> eventNotes = eventId != null ? noteRepository.findByEvent_Id(eventId) : List.of();

            notes = new java.util.ArrayList<>(listingNotes.size() + eventNotes.size());
            notes.addAll(listingNotes);
            notes.addAll(eventNotes);
        }
        return notes.stream()
                .filter(note -> !Boolean.TRUE.equals(note.getOnHold()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponseDto)
                .toList();
    }

    public NoteResponseDto updateNote(Long noteId, Long requestingUserId, UpdateNoteRequestDto request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (!note.getAuthor().getId().equals(requestingUserId)) {
            throw new IllegalStateException("You can only edit your own notes");
        }

        Duration timeSinceCreation = Duration.between(note.getCreatedAt(), LocalDateTime.now());
        if (timeSinceCreation.toHours() >= EDIT_WINDOW_HOURS) {
            throw new IllegalStateException(
                    "This note can no longer be edited - the " + EDIT_WINDOW_HOURS + "-hour edit window has passed"
            );
        }

        note.setContent(request.getContent());
        note.setOnHold(containsTriggerWord(request.getContent()));

        Note saved = noteRepository.save(note);
        return toResponseDto(saved);
    }

    private boolean containsTriggerWord(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        return AUTO_HOLD_TRIGGER_WORDS.stream().anyMatch(lower::contains);
    }

    private NoteResponseDto toResponseDto(Note note) {
        return NoteResponseDto.builder()
                .id(note.getId())
                .content(note.getContent())
                .listingId(note.getListing() != null ? note.getListing().getId() : null)
                .eventId(note.getEvent() != null ? note.getEvent().getId() : null)
                .authorId(note.getAuthor().getId())
                .onHold(note.getOnHold())
                .createdAt(note.getCreatedAt())
                .build();
    }
}
