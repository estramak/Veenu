package controllers;

import dtos.CreateNoteRequestDto;
import dtos.NoteResponseDto;
import dtos.UpdateNoteRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import services.NoteService;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<List<NoteResponseDto>> getNotesForListing(@PathVariable Long listingId) {
        return ResponseEntity.ok(noteService.getNotesForListing(listingId));
    }

    @GetMapping("event/{eventId}")
    public ResponseEntity<List<NoteResponseDto>> getNotesForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(noteService.getNotesForEvent(eventId));
    }

    //combined view for event page, notes connected to event in one place
    @GetMapping("/event/{eventId}/combined")
    public ResponseEntity<List<NoteResponseDto>> getNotesForEventPage(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long listingId,
            @RequestParam(required = false) String filter
    ) {
        return ResponseEntity.ok(noteService.getNotesForEventPage(listingId, eventId, filter));
    }

    //any logged-in user can post a note
    @PostMapping public ResponseEntity<NoteResponseDto> createNote(
        @Valid @RequestBody CreateNoteRequestDto request,
        Authentication authentication
    ) {
       Long userId = (Long) authentication.getPrincipal();
       NoteResponseDto result = noteService.createNote(userId, request);
       return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    //passes user's ID through to NoteService to check for editing reinforcement
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDto> updateNote(
        @PathVariable("id") Long noteId,
        @Valid @RequestBody UpdateNoteRequestDto request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        NoteResponseDto result = noteService.updateNote(noteId, userId, request);
        return ResponseEntity.ok(result);
    }
}
