package repositories;

import model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    //find all events at a specific listing (location)
    List<Event> findByListingId(Long listingId);
    boolean existsByListingId(Long listingId);

    //find all events at a specific business
    List<Event> findByBusiness_Id(Long businessId);
    boolean existsByBusiness_Id(Long businessId);

    //find all events created by a specific user
    List<Event> findByCreator_Id(Long creatorId);
    boolean existsByCreator_Id(Long creatorId);

    //find by date ranges
    List<Event> findByStartDateTimeAfterOrderByStartDateTimeAsc(LocalDateTime now);
    List<Event> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByEndDateTimeBefore(LocalDateTime now);

    //find by free or paid events
    List<Event> findByIsFreeTrue();
    List<Event> findByIsFreeFalse();

    //find if event requires external registration
    List<Event> findByRegistrationUrlIsNotNull();


}
