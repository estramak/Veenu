package controllers;

import dtos.BusinessResponseDto;
import dtos.CreateBusinessRequestDto;
import dtos.ReportBusinessRequestDto;
import dtos.UpdateBusinessRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import services.BusinessService;

@RestController
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/api/listings")
    public ResponseEntity<BusinessResponseDto> createBusiness(
        @Valid @RequestBody CreateBusinessRequestDto request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        BusinessResponseDto result = businessService.createBusiness(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/api/business/{id}")
    public ResponseEntity<Void> updateBusiness(
        @PathVariable Long id,
        @Valid @RequestBody UpdateBusinessRequestDto request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        businessService.updateBusiness(id, request, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/business/{id}/report")
    public ResponseEntity<Void> reportBusiness(
        @PathVariable("id") Long businessId,
        @Valid @RequestBody ReportBusinessRequestDto request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        businessService.reportBusiness(businessId, userId, request);
        return ResponseEntity.noContent().build();
    }
}
