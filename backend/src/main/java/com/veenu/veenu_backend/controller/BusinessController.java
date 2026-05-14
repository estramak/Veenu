package com.veenu.veenu_backend.controller;

import com.veenu.veenu_backend.dto.BusinessRequest;
import com.veenu.veenu_backend.dto.BusinessResponse;
import com.veenu.veenu_backend.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    // POST /api/business — create a business listing
    @PostMapping
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.ok(businessService.createBusiness(request));
    }

    // GET /api/business/listing/{listingId}
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<BusinessResponse> getByListingId(
            @PathVariable Long listingId) {
        return businessService.findByListingId(listingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/business/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BusinessResponse> updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody BusinessRequest request) {
        return businessService.updateBusiness(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}