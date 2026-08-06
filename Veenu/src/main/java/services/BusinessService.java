package services;

import dtos.BusinessHoursDto;
import dtos.BusinessResponseDto;
import dtos.CreateBusinessRequestDto;
import dtos.ReportBusinessRequestDto;
import dtos.UpdateBusinessRequestDto;
import jakarta.transaction.Transactional;
import model.*;
import model.enums.AdminEntityType;
import model.enums.BusinessUserRole;
import model.enums.EntityStatus;
import model.enums.LocationType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import repositories.BusinessRepository;
import repositories.BusinessReportRepository;
import repositories.BusinessUserRepository;
import repositories.ListingRepository;
import repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    // Matches Architecture.md / Database_Schema.md proximity threshold
    private static final double DUPLICATE_LISTING_RADIUS_METERS = 20.0;

    // Notification fires once, the moment reports cross this count
    private static final long REPORT_REVIEW_THRESHOLD = 3;

    private final BusinessRepository businessRepository;
    private final BusinessUserRepository businessUserRepository;
    private final BusinessReportRepository businessReportRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final StatusChangeLogService statusChangeLogService;

    public BusinessService(
            BusinessRepository businessRepository,
            BusinessUserRepository businessUserRepository,
            BusinessReportRepository businessReportRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            EmailService emailService,
            StatusChangeLogService statusChangeLogService
    ) {
        this.businessRepository = businessRepository;
        this.businessUserRepository = businessUserRepository;
        this.businessReportRepository = businessReportRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.statusChangeLogService = statusChangeLogService;
    }

    // create business
    public BusinessResponseDto createBusiness(Long creatorUserId, CreateBusinessRequestDto request) {
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (businessRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A business with this email already exists");
        }

        Listing listing = resolveListing(request);

        Business business = new Business();
        business.setListing(listing);
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setWebsite(request.getWebsite());
        business.setEntityStatus(EntityStatus.ACTIVE); // auto-approved, no PENDING review
        business.setIsVerified(false);
        business.setFlaggedForReview(false);
        business.setSubmittedBy(creator); // tracked for moderation/trust — grants no ownership

        Business savedBusiness = businessRepository.save(business);

        // No BusinessUser is created here. This Business is unclaimed —
        // ownership is established later via a separate claim process,
        // not by whoever happened to submit the listing.

        return toResponseDto(savedBusiness);
    }

    // finds an existing Listing within the duplicate-detection radius, or creates a new one if this is the first business at this location
    private Listing resolveListing(CreateBusinessRequestDto request) {
        List<Listing> nearby = listingRepository.findWithinMeters(
                request.getLatitude(), request.getLongitude(), DUPLICATE_LISTING_RADIUS_METERS
        );

        // TODO: once the frontend confirmation dialog exists, nearby results should be returned to the user to confirm "is this the same place?"
        if (!nearby.isEmpty()) {
            Listing existing = nearby.get(0);
            if (businessRepository.existsByListingId(existing.getId())) {
                throw new IllegalArgumentException(
                        "A business is already registered at this location");
            }
            return existing;
        }

        Listing listing = new Listing();
        listing.setName(request.getName()); // business attached -> name inherited, locked
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());
        listing.setAddress(request.getAddress());
        listing.setAddressLine2(request.getAddressLine2());
        listing.setCity(request.getCity());
        listing.setState(request.getState());
        listing.setZip(request.getZip());
        listing.setLocationType(LocationType.BUSINESS);
        listing.setEntityStatus(EntityStatus.ACTIVE);
        listing.setIsActive(true);
        listing.setHasDistinctName(true); // Business attached -> name is locked

        return listingRepository.save(listing);
    }

    // report business
    public void reportBusiness(Long businessId, Long reporterUserId, ReportBusinessRequestDto request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        if (businessReportRepository.existsByBusiness_IdAndReportedBy_Id(businessId, reporterUserId)) {
            throw new IllegalArgumentException("You have already reported this business");
        }

        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BusinessReport report = new BusinessReport();
        report.setBusiness(business);
        report.setReportedBy(reporter);
        report.setReason(request.getReason());
        businessReportRepository.save(report);

        long reportCount = businessReportRepository.countByBusiness_Id(businessId);

        // only fire once, on the crossing
        if (reportCount >= REPORT_REVIEW_THRESHOLD && !business.getFlaggedForReview()) {
            business.setFlaggedForReview(true);
            businessRepository.save(business);
            notifyAdminOfFlaggedBusiness(business, reportCount);
        }
    }

    // TODO: wire up to the Slack webhook per Architecture.md's admin
    // notification pattern (currently used for new-listing review,
    // repurposed here for report-threshold review). No in-app admin
    // action is required per current design, review happens externally.
    private void notifyAdminOfFlaggedBusiness(Business business, long reportCount) {
        // placeholder
    }

    @Transactional
    public void updateBusiness(Long businessId, UpdateBusinessRequestDto request, Long userId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        BusinessUser owner = businessUserRepository
                .findByBusiness_IdAndRole(businessId, BusinessUserRole.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("This business has no owner"));
        if (!owner.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Only the business owner can update business details");
        }

        if (request.getEmail() != null && businessRepository.existsByEmailAndIdNot(request.getEmail(), businessId)) {
            throw new IllegalArgumentException("A business with this email already exists");
        }
        if (request.getPhone() != null && businessRepository.existsByPhoneAndIdNot(request.getPhone(), businessId)) {
            throw new IllegalArgumentException("A business with this phone already exists");
        }
        if (request.getWebsite() != null && businessRepository.existsByWebsiteAndIdNot(request.getWebsite(), businessId)) {
            throw new IllegalArgumentException("A business with this website already exists");
        }

        if (request.getEmail() != null) business.setEmail(request.getEmail());
        if (request.getPhone() != null) business.setPhone(request.getPhone());
        if (request.getWebsite() != null) business.setWebsite(request.getWebsite());

        Listing listing = business.getListing();
        if (request.getLatitude() != null) listing.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) listing.setLongitude(request.getLongitude());
        if (request.getAddress() != null) listing.setAddress(request.getAddress());
        if (request.getAddressLine2() != null) listing.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) listing.setCity(request.getCity());
        if (request.getState() != null) listing.setState(request.getState());
        if (request.getZip() != null) listing.setZip(request.getZip());
        listingRepository.save(listing);

        if (request.getHours() != null) {
            business.getHours().clear();
            for (BusinessHoursDto h : request.getHours()) {
                BusinessHours bh = new BusinessHours();
                bh.setBusiness(business);
                bh.setDayOfWeek(h.getDayOfWeek());
                bh.setOpenTime(h.getOpenTime());
                bh.setCloseTime(h.getCloseTime());
                bh.setClosed(h.isClosed());
                business.getHours().add(bh);
            }
        }

        if (business.getEntityStatus() == EntityStatus.CHANGES_REQUESTED) {
            business.setEntityStatus(EntityStatus.PENDING);
            businessRepository.save(business);
            resolveOwnerEmail(business).ifPresent(email ->
                    emailService.sendBusinessPendingEmail(business, null, email));
        } else {
            businessRepository.save(business);
        }
    }

    @Transactional
    public void requestChanges(Long businessId, String reason, String adminNotes, Long changedBy) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        EntityStatus previousStatus = business.getEntityStatus();
        business.setEntityStatus(EntityStatus.CHANGES_REQUESTED);
        business.setSuspensionReason(reason);
        business.setFlaggedForReview(false);
        businessRepository.save(business);

        statusChangeLogService.log(AdminEntityType.BUSINESS, businessId,
                previousStatus, EntityStatus.CHANGES_REQUESTED, reason, adminNotes, changedBy);

        resolveOwnerEmail(business).ifPresent(email ->
                emailService.sendBusinessChangesRequestedEmail(business, reason, email));
    }

    @Transactional
    public void approve(Long businessId, String adminNotes, Long changedBy) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        if (business.getEntityStatus() == EntityStatus.TAKEN_DOWN) {
            throw new IllegalArgumentException("This business has been permanently taken down and cannot be reinstated");
        }
        EntityStatus previousStatus = business.getEntityStatus();
        business.setEntityStatus(EntityStatus.ACTIVE);
        business.setSuspensionReason(null);
        business.setFlaggedForReview(false);
        businessRepository.save(business);

        statusChangeLogService.log(AdminEntityType.BUSINESS, businessId,
                previousStatus, EntityStatus.ACTIVE, null, adminNotes, changedBy);

        resolveOwnerEmail(business).ifPresent(email ->
                emailService.sendBusinessApprovedEmail(business, email));
    }

    @Transactional
    public void takeDown(Long businessId, String reason, String adminNotes, Long changedBy) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        EntityStatus previousStatus = business.getEntityStatus();
        business.setEntityStatus(EntityStatus.TAKEN_DOWN);
        business.setSuspensionReason(reason);
        business.setFlaggedForReview(false);
        businessRepository.save(business);

        statusChangeLogService.log(AdminEntityType.BUSINESS, businessId,
                previousStatus, EntityStatus.TAKEN_DOWN, reason, adminNotes, changedBy);

        resolveOwnerEmail(business).ifPresent(email ->
                emailService.sendBusinessTakenDownEmail(business, reason, email));
    }

    @Transactional
    public void overrideTakeDown(Long businessId, String adminNotes, Long changedBy) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        if (business.getEntityStatus() != EntityStatus.TAKEN_DOWN) {
            throw new IllegalArgumentException("This business is not currently taken down");
        }

        EntityStatus previousStatus = business.getEntityStatus();
        business.setEntityStatus(EntityStatus.ACTIVE);
        business.setSuspensionReason(null);
        businessRepository.save(business);

        statusChangeLogService.log(AdminEntityType.BUSINESS, businessId,
                previousStatus, EntityStatus.ACTIVE, "Take-down overridden by admin", adminNotes, changedBy);

        resolveOwnerEmail(business).ifPresent(email ->
                emailService.sendBusinessApprovedEmail(business, email));
    }

    @Transactional
    public void remove(Long businessId, String reason, String adminNotes, Long changedBy) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        EntityStatus previousStatus = business.getEntityStatus();
        business.setEntityStatus(EntityStatus.REMOVED);
        business.setSuspensionReason(reason);
        business.setFlaggedForReview(false);
        businessRepository.save(business);

        statusChangeLogService.log(AdminEntityType.BUSINESS, businessId,
                previousStatus, EntityStatus.REMOVED, reason, adminNotes, changedBy);

        resolveOwnerEmail(business).ifPresent(email ->
                emailService.sendBusinessRemovedEmail(business, reason, email));
    }

    private Optional<String> resolveOwnerEmail(Business business) {
        return businessUserRepository.findByBusiness_IdAndRole(business.getId(), BusinessUserRole.OWNER)
                .map(bu -> bu.getUser().getEmail());
    }

    private BusinessResponseDto toResponseDto(Business business) {
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
}


