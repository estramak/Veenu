package com.veenu.veenu_backend.service;

import com.veenu.veenu_backend.dto.ListingRequest;
import com.veenu.veenu_backend.dto.ListingResponse;
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

    public List<ListingResponse> findWithinRadius(double lat, double lon, double radiusKm) {
        return listingRepository.findWithinRadius(lat, lon, radiusKm)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ListingResponse> findById(Long id) {
        return listingRepository.findById(id).map(this::toResponse);
    }

    public ListingResponse createListing(ListingRequest request) {
        Listing listing = new Listing();
        listing.setName(request.getName());
        listing.setType(request.getType());
        listing.setDescription(request.getDescription());
        listing.setDateLabel(request.getDateLabel());
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());
        listing.setAddress(request.getAddress());
        listing.setStatus(ListingStatus.APPROVED);
        return toResponse(listingRepository.save(listing));
    }

    public Optional<ListingResponse> updateListing(Long id, ListingRequest request) {
        return listingRepository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            existing.setAddress(request.getAddress());
            existing.setLatitude(request.getLatitude());
            existing.setLongitude(request.getLongitude());
            existing.setDateLabel(request.getDateLabel());
            return toResponse(listingRepository.save(existing));
        });
    }

    public Optional<ListingResponse> suspendListing(Long id) {
        return listingRepository.findById(id).map(listing -> {
            listing.setStatus(ListingStatus.SUSPENDED);
            return toResponse(listingRepository.save(listing));
        });
    }

    public Optional<ListingResponse> requestChanges(Long id) {
        return listingRepository.findById(id).map(listing -> {
            listing.setStatus(ListingStatus.CHANGES_REQUESTED);
            return toResponse(listingRepository.save(listing));
        });
    }

    public void deleteListing(Long id) {
        listingRepository.deleteById(id);
    }

    // Maps Listing entity to ListingResponse DTO
    private ListingResponse toResponse(Listing listing) {
        ListingResponse response = new ListingResponse();
        response.setId(listing.getId());
        response.setName(listing.getName());
        response.setType(listing.getType());
        response.setDescription(listing.getDescription());
        response.setDateLabel(listing.getDateLabel());
        response.setLatitude(listing.getLatitude());
        response.setLongitude(listing.getLongitude());
        response.setAddress(listing.getAddress());
        response.setStatus(listing.getStatus());
        response.setCreatedAt(listing.getCreatedAt());
        response.setUpdatedAt(listing.getUpdatedAt());
        return response;
    }
}