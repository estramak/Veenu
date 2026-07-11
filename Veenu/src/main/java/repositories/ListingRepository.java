package repositories;

import model.Listing;
import model.enums.EntityStatus;
import model.enums.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    // proximity search
    @Query(value = """
            SELECT * FROM listing l
            WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(l.latitude))
                * cos(radians(l.longitude) - radians(:lon))
                + sin(radians(:lat)) * sin(radians(l.latitude)))) <= :radiusKm
            """, nativeQuery = true)
    List<Listing> findNearby(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusKm") double radiusKm
    );

    // duplicate-listing check (proximity threshold ~20m)
    @Query(value = """
        SELECT * FROM listing l
        WHERE (6371000 * acos(cos(radians(:lat)) * cos(radians(l.latitude))
            * cos(radians(l.longitude) - radians(:lon))
            + sin(radians(:lat)) * sin(radians(l.latitude)))) <= :radiusMeters
        """, nativeQuery = true)
    List<Listing> findWithinMeters(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters
    );

    //address-based distinction (shared building, different suites)
    Optional<Listing> findByAddressAndAddressLine2(String address, String addressLine2);
    List<Listing> findByAddress(String address);

    // type/status filters
    List<Listing> findByLocationType(LocationType locationType);
    List<Listing> findByEntityStatus(EntityStatus entityStatus);
    List<Listing> findByIsActiveTrue();

    // find listing still using a temp or inherited name
    List<Listing> findByHasDistinctNameFalse();

    // city/region browsing
    List<Listing> findByCityAndState(String city, String state);
}

