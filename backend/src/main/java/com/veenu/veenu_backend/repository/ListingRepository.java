package com.veenu.veenu_backend.repository;

import com.veenu.veenu_backend.model.Listing;
import com.veenu.veenu_backend.model.ListingStatus;
import com.veenu.veenu_backend.model.ListingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    //find all listing by status
    List<Listing> findByStatus(ListingStatus status);

    //Haversine query - finds listing within a radius
    @Query(value = """
            SELECT * FROM listings
            WHERE (6371 * acos(
                cos(radians(:lat)) * cos(radians(latitude)) *
                cos(radians(longitude) - radians(:lon)) +
                sin(radians(:lat)) * sin(radians(latitude))
            )) <= :radiusKm
            AND status = 'APPROVED'
            """, nativeQuery = true)
    List<Listing> findWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusKm") double radiusKm
    );
}
