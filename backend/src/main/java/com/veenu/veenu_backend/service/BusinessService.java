package com.veenu.veenu_backend.service;

import com.veenu.veenu_backend.dto.BusinessRequest;
import com.veenu.veenu_backend.dto.BusinessResponse;
import com.veenu.veenu_backend.model.*;
import com.veenu.veenu_backend.repository.BusinessRepository;
import com.veenu.veenu_backend.repository.ListingRepository;
import com.veenu.veenu_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public BusinessResponse createBusiness(BusinessRequest request) {
        // Get logged in user from JWT
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create the listing first
        Listing listing = new Listing();
        listing.setName(request.getName());
        listing.setType(ListingType.BUSINESS);
        listing.setDescription(request.getDescription());
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());
        listing.setAddress(request.getAddress());
        listing.setStatus(ListingStatus.APPROVED);
        Listing savedListing = listingRepository.save(listing);

        // Create the business linked to the listing
        Business business = new Business();
        business.setListing(savedListing);
        business.setOwner(owner);
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setWebsite(request.getWebsite());
        business.setHours(request.getHours());
        business.setTier(BusinessTier.FREE);
        business.setSubscriptionActive(false);
        Business savedBusiness = businessRepository.save(business);

        // Upgrade user role to BUSINESS
        owner.setRole(UserRole.BUSINESS);
        userRepository.save(owner);

        return toResponse(savedBusiness);
    }

    public Optional<BusinessResponse> findByListingId(Long listingId) {
        return businessRepository.findByListingId(listingId).map(this::toResponse);
    }

    public Optional<BusinessResponse> updateBusiness(Long id, BusinessRequest request) {
        return businessRepository.findById(id).map(business -> {
            business.setEmail(request.getEmail());
            business.setPhone(request.getPhone());
            business.setWebsite(request.getWebsite());
            business.setHours(request.getHours());

            Listing listing = business.getListing();
            listing.setName(request.getName());
            listing.setDescription(request.getDescription());
            listing.setAddress(request.getAddress());
            listing.setLatitude(request.getLatitude());
            listing.setLongitude(request.getLongitude());
            listingRepository.save(listing);

            return toResponse(businessRepository.save(business));
        });
    }

    private BusinessResponse toResponse(Business business) {
        BusinessResponse response = new BusinessResponse();
        response.setId(business.getId());
        response.setListingId(business.getListing().getId());
        response.setName(business.getListing().getName());
        response.setDescription(business.getListing().getDescription());
        response.setLatitude(business.getListing().getLatitude());
        response.setLongitude(business.getListing().getLongitude());
        response.setAddress(business.getListing().getAddress());
        response.setEmail(business.getEmail());
        response.setPhone(business.getPhone());
        response.setWebsite(business.getWebsite());
        response.setHours(business.getHours());
        response.setTier(business.getTier());
        response.setSubscriptionActive(business.getSubscriptionActive());
        response.setListingStatus(business.getListing().getStatus());
        return response;
    }
}