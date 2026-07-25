package controllers;

import dtos.BusinessResponseDto;
import dtos.CreateBusinessRequestDto;
import dtos.ReportBusinessRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
