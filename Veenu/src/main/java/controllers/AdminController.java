package controllers;

import dtos.AdminQueueResponseDto;
import dtos.SuspendRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import services.AdminQueueService;
import services.BusinessService;
import services.ListingService;
import services.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BusinessService businessService;
    private final ListingService listingService;
    private final UserService userService;
    private final AdminQueueService adminQueueService;

    public AdminController(
            BusinessService businessService,
            ListingService listingService,
            UserService userService,
            AdminQueueService adminQueueService
    ) {
        this.businessService = businessService;
        this.listingService = listingService;
        this.userService = userService;
        this.adminQueueService = adminQueueService;
    }

    /*
    * BUSINESS
    * */
    @PostMapping("/business/{id}/suspend")
    public ResponseEntity<Void> suspendBusiness(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            @NotNull Authentication authentication
    ) {
        String reason = (request != null) ? request.getReason() : null;
        businessService.suspend(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/business/{id}/request-changes")
    public ResponseEntity<Void> requestBusinessChanges(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            @NotNull Authentication authentication
    ) {
        businessService.requestChanges(id, request.getReason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/business/{id}/approve")
    public ResponseEntity<Void> approveBusiness(
            @PathVariable Long id,
            @NotNull Authentication authentication
    ) {
        businessService.approve(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * LISTING
     */

    @PostMapping("/listings/{id}/suspend")
    public ResponseEntity<Void> suspendListing(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            @NotNull Authentication authentication
    ) {
        String reason = (request != null) ? request.getReason() : null;
        listingService.suspend(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listings/{id}/approve")
    public ResponseEntity<Void> approveListing(
            @PathVariable Long id,
            @NotNull Authentication authentication
    ) {
        listingService.approve(id);
        return ResponseEntity.noContent().build();
    }

    /*
    * USER
    * */

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            @NotNull Authentication authentication
    ) {
        String reason = (request != null) ? request.getReason() : null;
        userService.suspend(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/request-changes")
    public ResponseEntity<Void> requestUserChanges(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            @NotNull Authentication authentication
    ) {
        userService.requestChanges(id, request.getReason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<Void> approveUser(
            @PathVariable Long id,
            @NotNull Authentication authentication
    ) {
        userService.approve(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            @NotNull Authentication authentication
    ) {
        String reason = (request != null) ? request.getReason() : null;
        userService.ban(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/queue")
    public ResponseEntity<AdminQueueResponseDto> getQueue(
            @NotNull Authentication authentication
    ) {
        return ResponseEntity.ok(adminQueueService.getQueue());
    }
}
