package com.veenu.veenu_backend.controller;

import com.veenu.veenu_backend.model.Listing;
import com.veenu.veenu_backend.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // GET /api/listings?lat=&lon=&radius=
    @GetMapping
    public List<Listing> getListingsInRadius(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") double radius) {
        return listingService.findWithinRadius(lat, lon, radius);
    }

    // GET /api/listings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(@PathVariable Long id) {
        return listingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/listings
    @PostMapping
    public ResponseEntity<Listing> createListing(@RequestBody Listing listing) {
        Listing created = listingService.createListing(listing);
        return ResponseEntity.ok(created);
    }

    // PUT /api/listings/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Listing> updateListing(
            @PathVariable Long id,
            @RequestBody Listing updated) {
        return listingService.updateListing(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/listings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
        listingService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }

    // Admin endpoints
    @PostMapping("/{id}/suspend")
    public ResponseEntity<Listing> suspendListing(@PathVariable Long id) {
        return listingService.suspendListing(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/request-changes")
    public ResponseEntity<Listing> requestChanges(@PathVariable Long id) {
        return listingService.requestChanges(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
