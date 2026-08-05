package controllers;

import dtos.ListingDetailDto;
import dtos.ListingSummaryDto;
import dtos.UpdateListingRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import services.ListingService;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    // public, no auth required
    @GetMapping("/nearby")
    public ResponseEntity<List<ListingSummaryDto>> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "20") double radius
    ) {
        List<ListingSummaryDto> results = listingService.searchNearby(lat, lon, radius);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDetailDto> getListingDetail(@PathVariable Long id) {
        ListingDetailDto result =  listingService.getListingDetail(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody UpdateListingRequestDto request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        listingService.updateListing(id, request, userId);
        return ResponseEntity.noContent().build();
    }
}
