package controllers;

import dtos.ListingDetailDto;
import dtos.ListingSummaryDto;
import org.springframework.http.ResponseEntity;
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
}
