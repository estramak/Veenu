package com.veenu.veenu_backend.repository;

import com.veenu.veenu_backend.model.Business;
import com.veenu.veenu_backend.model.BusinessTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByListingId(Long listingId);

    Optional<Business> findByOwnerId(Long ownerId);

    List<Business> findByTier(BusinessTier tier);

    boolean existsByEmail(String email);
}