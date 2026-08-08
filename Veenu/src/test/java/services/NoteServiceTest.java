package services;

import dtos.UpdateNoteRequestDto;
import dtos.NoteResponseDto;
import model.Note;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.EventRepository;
import repositories.ListingRepository;
import repositories.NoteRepository;
import repositories.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    private Note note;

    @BeforeEach
    void setUp() {
        User author = new User();
        author.setId(20L);

        note = new Note();
        note.setId(5L);
        note.setAuthor(author);
        note.setContent("original content");
        note.setOnHold(false);
        note.setCreatedAt(LocalDateTime.now().minusMinutes(30));
    }

    @Test
    void updateNote_noteNotFound_throwsIllegalArgumentException() {
        when(noteRepository.findById(5L)).thenReturn(Optional.empty());

        UpdateNoteRequestDto request = new UpdateNoteRequestDto();
        request.setContent("updated");

        assertThatThrownBy(() -> noteService
                .updateNote(5L, 20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Note not found");
    }

    @Test
    void updateNote_notAuthor_throwsAccessDeniedException() {
        when(noteRepository.findById(5L))
                .thenReturn(Optional.of(note));

        UpdateNoteRequestDto request = new UpdateNoteRequestDto();
        request.setContent("updated");

        assertThatThrownBy(() -> noteService
                .updateNote(5L, 999L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only edit your own notes");
    }

    @Test
    void updateNote_editWindowExpired_throwsIllegalStateException() {
        note.setCreatedAt(LocalDateTime.now().minusHours(2));
        when(noteRepository.findById(5L))
                .thenReturn(Optional.of(note));

        UpdateNoteRequestDto request = new UpdateNoteRequestDto();
        request.setContent("updated");

        assertThatThrownBy(() -> noteService
                .updateNote(5L, 20L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This note can no longer be edited - the 1-hour edit window has passed");
    }

    @Test
    void updateNote_happyPath_updatesContentAndReturnsDto() {
        when(noteRepository.findById(5L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        UpdateNoteRequestDto request = new UpdateNoteRequestDto();
        request.setContent("updated content");

        NoteResponseDto response = noteService.updateNote(5L, 20L, request);

        assertThat(note.getContent()).isEqualTo("updated content");
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getContent()).isEqualTo("updated content");
        assertThat(response.getOnHold()).isFalse();
    }

    @Test
    void updateNote_contentWithTriggerWord_setsOnHoldFalse() {
        when(noteRepository.findById(5L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        UpdateNoteRequestDto request = new UpdateNoteRequestDto();
        request.setContent("Nice weather today");

        NoteResponseDto response = noteService.updateNote(5L, 20L, request);

        assertThat(note.getOnHold()).isFalse();
        assertThat(response.getOnHold()).isFalse();
    }
}
