package repositories;

import model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // location-based lookups
    List<Note> findByListing_Id(Long listingId);

    List<Note> findByEvent_Id(Long eventId);

    // author lookups
    List<Note> findByAuthor_Id(Long authorId);

    //mod queue
    List<Note> findByHoldTrue();

    long countByOnHoldTrue();
}
