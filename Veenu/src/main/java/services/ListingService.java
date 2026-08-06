package services;

import dtos.BusinessResponseDto;
import dtos.EventResponseDto;
import dtos.ListingDetailDto;
import dtos.ListingSummaryDto;
import dtos.UpdateListingRequestDto;
import model.Business;
import model.Event;
import model.Listing;
import model.enums.AdminEntityType;
import model.enums.EntityStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import repositories.BusinessRepository;
import repositories.EventRepository;
import repositories.ListingRepository;
import jakarta.transaction.Transactional;

import javax.swing.text.html.parser.Entity;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final BusinessRepository businessRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final StatusChangeLogService statusChangeLogService;

    public ListingService(
        ListingRepository listingRepository,
        BusinessRepository businessRepository,
        EventRepository eventRepository,
        StatusChangeLogService statusChangeLogService,
        EmailService emailService
    ) {
        this.listingRepository = listingRepository;
        this.businessRepository = businessRepository;
        this.eventRepository = eventRepository;
        this.statusChangeLogService = statusChangeLogService;
        this.emailService = emailService;
    }

    // nearby search
    public List<ListingSummaryDto> searchNearby(double lat, double lon, double radiusKm) {
        List<Listing> results = listingRepository.findNearby(lat, lon, radiusKm);

        return results.stream()
                .filter(listing -> listing.getEntityStatus() == EntityStatus.ACTIVE && Boolean.TRUE.equals(listing.getIsActive()))
                .map(this::toSummaryDto)
                .toList();
    }

    // listing detail, full view
    public ListingDetailDto getListingDetail(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        Optional<Business> business = businessRepository.findByListingId(listingId);

        List<EventResponseDto> upcomingEvents = eventRepository.findByListingId(listingId)
                .stream()
                .filter(event -> event.getStartDateTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Event::getStartDateTime))
                .map(this::toEventResponseDto)
                .toList();

        return ListingDetailDto.builder()
                .id(listing.getId())
                .name(listing.getName())
                .description(listing.getDescription())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .address(listing.getAddress())
                .addressLine2(listing.getAddressLine2())
                .city(listing.getCity())
                .state(listing.getState())
                .zip(listing.getZip())
                .locationType(listing.getLocationType().name())
                .business(business.map(this::toBusinessResponseDto).orElse(null))
                .upcomingEvents(upcomingEvents)
                .build();
    }

    private ListingSummaryDto toSummaryDto(Listing listing) {
        boolean hasBusiness = businessRepository.existsByListingId(listing.getId());

        return ListingSummaryDto.builder()
                .id(listing.getId())
                .name(listing.getName())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .locationType(listing.getLocationType().name())
                .hasBusiness(hasBusiness)
                .build();
    }

    private BusinessResponseDto toBusinessResponseDto(Business business) {
        return BusinessResponseDto.builder()
                .id(business.getId())
                .listingId(business.getListing().getId())
                .name(business.getListing().getName())
                .email(business.getEmail())
                .phone(business.getPhone())
                .website(business.getWebsite())
                .entityStatus(business.getEntityStatus().name())
                .isVerified(business.getIsVerified())
                .flaggedForReview(business.getFlaggedForReview())
                .submittedByUserId(business.getSubmittedBy() != null ? business.getSubmittedBy().getId() : null)
                .build();
    }

    private EventResponseDto toEventResponseDto(Event event) {
        return EventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .listingId(event.getListing().getId())
                .businessId(event.getBusiness() != null ? event.getBusiness().getId() : null)
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .isFree(event.getIsFree())
                .price(event.getPrice())
                .capacity(event.getCapacity())
                .registrationUrl(event.getRegistrationUrl())
                .postedByOwner(event.getPostedByOwner())
                .build();
    }

// ... inside the class, alongside your existing methods ...

    @Transactional
    public void updateListing(Long listingId, UpdateListingRequestDto request, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (!listing.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("You can only update listings you submitted");
        }

        if (request.getName() != null) {
            if (listing.getHasDistinctName()) {
                throw new IllegalArgumentException("The name of a business-attached listing cannot be changed");
            }
            listing.setName(request.getName());
        }
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getLatitude() != null) listing.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) listing.setLongitude(request.getLongitude());
        if (request.getAddress() != null) listing.setAddress(request.getAddress());
        if (request.getAddressLine2() != null) listing.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) listing.setCity(request.getCity());
        if (request.getState() != null) listing.setState(request.getState());
        if (request.getZip() != null) listing.setZip(request.getZip());
        if (request.getCountry() != null) listing.setCountry(request.getCountry());
        if (request.getLocationType() != null) listing.setLocationType(request.getLocationType());

        if (listing.getEntityStatus() == EntityStatus.CHANGES_REQUESTED) {
            listing.setEntityStatus(EntityStatus.PENDING);
            listingRepository.save(listing);
            emailService.sendListingPendingReviewEmail(listing, listing.getCreatedBy().getEmail());
        } else {
            listingRepository.save(listing);
        }
    }

    @Transactional
    public void requestChanges(Long listingId, String reason, String adminNotes, Long changedBy) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        EntityStatus previousStatus = listing.getEntityStatus();
        listing.setEntityStatus(EntityStatus.CHANGES_REQUESTED);
        listing.setSuspensionReason(reason);
        listingRepository.save(listing);

        statusChangeLogService.log(AdminEntityType.LISTING, listingId,
                previousStatus, EntityStatus.CHANGES_REQUESTED, reason, adminNotes, changedBy);

        emailService.sendListingChangesRequestedEmail(listing, reason, listing.getCreatedBy().getEmail());
    }

    @Transactional
    public void approve(Long listingId, String adminNotes, Long changedBy) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (listing.getEntityStatus() == EntityStatus.TAKEN_DOWN) {
            throw new IllegalArgumentException("This listing has been permanently taken down and cannot be reinstated");
        }

        EntityStatus previousStatus = listing.getEntityStatus();
        listing.setEntityStatus(EntityStatus.ACTIVE);
        listing.setSuspensionReason(null);
        listingRepository.save(listing);

        statusChangeLogService.log(AdminEntityType.LISTING, listingId,
                previousStatus, EntityStatus.ACTIVE, null, adminNotes, changedBy);

        emailService.sendListingApprovedEmail(listing, listing.getCreatedBy().getEmail());
    }

    @Transactional
    public void takeDown(Long listingId, String reason, String adminNotes, Long changedBy) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        EntityStatus previousStatus = listing.getEntityStatus();
        listing.setEntityStatus(EntityStatus.TAKEN_DOWN);
        listing.setSuspensionReason(reason);
        listingRepository.save(listing);

        statusChangeLogService.log(AdminEntityType.LISTING, listingId,
                previousStatus, EntityStatus.TAKEN_DOWN, reason, adminNotes, changedBy);

        emailService.sendListingTakenDownEmail(listing, reason, listing.getCreatedBy().getEmail());
    }

    @Transactional
    public void overrideTakeDown(Long listingId, String adminNotes, Long changedBy) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (listing.getEntityStatus() != EntityStatus.TAKEN_DOWN) {
            throw new IllegalArgumentException("This listing is not currently taken down");
        }

        EntityStatus previousStatus = listing.getEntityStatus();
        listing.setEntityStatus(EntityStatus.ACTIVE);
        listing.setSuspensionReason(null);
        listingRepository.save(listing);

        statusChangeLogService.log(AdminEntityType.LISTING, listingId,
                previousStatus, EntityStatus.ACTIVE, "Take-down overridden by admin", adminNotes, changedBy);

        emailService.sendListingOverrideTakeDownEmail(listing, listing.getCreatedBy().getEmail());
    }

    @Transactional
    public void remove(Long listingId, String reason, String adminNotes, Long changedBy) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        EntityStatus previousStatus = listing.getEntityStatus();
        listing.setEntityStatus(EntityStatus.REMOVED);
        listing.setSuspensionReason(reason);
        listingRepository.save(listing);

        statusChangeLogService.log(AdminEntityType.LISTING, listingId,
                previousStatus, EntityStatus.REMOVED, reason, adminNotes, changedBy);

        emailService.sendListingRemovedEmail(listing, reason, listing.getCreatedBy().getEmail());
    }
}
