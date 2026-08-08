package controllers;

import dtos.SuspendRequestDto;
import jakarta.validation.Valid;
import model.StatusChangeLog;
import model.enums.AdminEntityType;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import services.*;
import dtos.AdminQueueResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BusinessService businessService;
    private final ListingService listingService;
    private final UserService userService;
    private final AdminQueueService adminQueueService;
    private final StatusChangeLogService statusChangeLogService;
    private final NoteService noteService;

    public AdminController(
            BusinessService businessService,
            ListingService listingService,
            UserService userService,
            AdminQueueService adminQueueService,
            StatusChangeLogService statusChangeLogService,
            NoteService noteService
    ) {
        this.businessService = businessService;
        this.listingService = listingService;
        this.userService = userService;
        this.adminQueueService = adminQueueService;
        this.statusChangeLogService = statusChangeLogService;
        this.noteService = noteService;
    }



    // ---------- Business ----------

    @PostMapping("/business/{id}/request-changes")
    public ResponseEntity<Void> requestBusinessChanges(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        businessService.requestChanges(id, request.getReason(), request.getAdminNotes(), adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/business/{id}/approve")
    public ResponseEntity<Void> approveBusiness(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        businessService.approve(id, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/business/{id}/take-down")
    public ResponseEntity<Void> takeDownBusiness(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = (request != null) ? request.getReason() : null;
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        businessService.takeDown(id, reason, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/business/{id}/override-take-down")
    public ResponseEntity<Void> overrideBusinessTakeDown(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        businessService.overrideTakeDown(id, request.getAdminNotes(), adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/business/{id}/remove")
    public ResponseEntity<Void> removeBusiness(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = (request != null) ? request.getReason() : null;
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        businessService.remove(id, reason, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Listing ----------

    @PostMapping("/listings/{id}/request-changes")
    public ResponseEntity<Void> requestListingChanges(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        listingService.requestChanges(id, request.getReason(), request.getAdminNotes(), adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listings/{id}/approve")
    public ResponseEntity<Void> approveListing(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        listingService.approve(id, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listings/{id}/take-down")
    public ResponseEntity<Void> takeDownListing(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = (request != null) ? request.getReason() : null;
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        listingService.takeDown(id, reason, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listings/{id}/override-take-down")
    public ResponseEntity<Void> overrideListingTakeDown(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        listingService.overrideTakeDown(id, request.getAdminNotes(), adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listings/{id}/remove")
    public ResponseEntity<Void> removeListing(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = (request != null) ? request.getReason() : null;
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        listingService.remove(id, reason, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    // ---------- User ----------

    @PostMapping("/users/{id}/request-changes")
    public ResponseEntity<Void> requestUserChanges(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        userService.requestChanges(id, request.getReason(), request.getAdminNotes(), adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<Void> approveUser(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        userService.approve(id, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/take-down")
    public ResponseEntity<Void> takeDownUser(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = (request != null) ? request.getReason() : null;
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        userService.takeDown(id, reason, adminNotes, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/override-take-down")
    public ResponseEntity<Void> overrideUserTakeDown(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        userService.overrideTakeDown(id, request.getAdminNotes(), adminId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Note ----------
    @PostMapping("/notes/{id}/taken-down")
    public ResponseEntity<Void> takeDownNote(
            @PathVariable Long id,
            @RequestBody(required = false)
            SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String reason = (request != null) ? request.getReason() : null;
        String adminNotes = (request != null) ? request.getAdminNotes() : null;
        noteService.takeDown(id, reason, adminNotes, adminId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notes/{id}/override-take-down")
    public ResponseEntity<Void> overrideNoteTakeDown(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        noteService.overrideTakeDown(id, request.getAdminNotes(), adminId);

        return ResponseEntity.noContent().build();
    }

    // ---------- Queue ----------

    @GetMapping("/queue")
    public ResponseEntity<AdminQueueResponseDto> getQueue() {
        return ResponseEntity.ok(adminQueueService.getQueue());
    }

    // ---------- Logs ----------

    @GetMapping("/logs/recent")
    public ResponseEntity<List<StatusChangeLog>> getRecentChanges() {
        return ResponseEntity.ok(statusChangeLogService.getRecent(10));
    }

    @GetMapping("/logs/{entityType}/{entityId}")
    public ResponseEntity<List<StatusChangeLog>> getEntityHistory(
            @PathVariable AdminEntityType entityType,
            @PathVariable Long entityId
    ) {
        return ResponseEntity.ok(statusChangeLogService.getHistory(entityType, entityId));
    }
}
