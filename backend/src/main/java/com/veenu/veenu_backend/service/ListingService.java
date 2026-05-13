package com.veenu.veenu_backend.service;

import com.veenu.veenu_backend.model.Listing;
import com.veenu.veenu_backend.model.ListingStatus;
import com.veenu.veenu_backend.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;

    //Get all approved listings within a radius
    public List<Listing> findWithinRadius(double lat, double lon, double radiusKm) {
        return listingRepository.findWithinRadius(lat, lon, radiusKm);
    }

    //Get a single listing by ID
    public Optional<Listing> findByID(Long id) {
        return listingRepository.findById(id);
    }

    //Create a new listing - automatically approved
    public Listing createListing(Listing listing) {
        listing.setStatus(ListingStatus.APPROVED);
        return listingRepository.save(listing);
    }

    //update an existing listing
    public Optional<Listing> updateListing(Long id, Listing updated) {
        return listingRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setAddress(updated.getAddress());
            existing.setLatitude(updated.getLatitude());
            existing.setLongitude(updated.getLongitude());
            existing.setDateLabel(updated.getDateLabel());
            return listingRepository.save(existing);
        });
    }

    //suspend a listing
    public Optional<Listing> suspendListing(Long id) {
        return listingRepository.findById(id).map(listing -> {
            listing.setStatus(ListingStatus.SUSPENDED);
            return listingRepository.save(listing);
        });
    }

    //request changes on a listing
    public Optional<Listing> requestChanges(Long id) {
        return listingRepository.findById(id).map(listing -> {
            listing.setStatus(ListingStatus.CHANGES_REQUESTED);
            return listingRepository.save(listing);
        });
    }

    //delete a listing
    public void deleteListing(Long id) {
        listingRepository.deleteById(id);
    }
}
