package services;

import dtos.BusinessResponseDto;
import dtos.CreateBusinessRequestDto;
import dtos.ReportBusinessRequestDto;
import jakarta.transaction.Transactional;
import model.Business;
import model.BusinessReport;
import model.Listing;
import model.User;
import model.enums.EntityStatus;
import model.enums.LocationType;
import org.springframework.stereotype.Service;
import repositories.BusinessRepository;
import repositories.BusinessReportRepository;
import repositories.BusinessUserRepository;
import repositories.ListingRepository;
import repositories.UserRepository;

import java.util.List;

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

    public BusinessService(
            BusinessRepository businessRepository,
            BusinessUserRepository businessUserRepository,
            BusinessReportRepository businessReportRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.businessRepository = businessRepository;
        this.businessUserRepository = businessUserRepository;
        this.businessReportRepository = businessReportRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
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
    // action is required per current design — review happens externally.
    private void notifyAdminOfFlaggedBusiness(Business business, long reportCount) {
        // Placeholder — implement Slack webhook call here
    }

    @Transactional
    public void suspend(Long businessId, String reason) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        business.setEntityStatus(EntityStatus.SUSPENDED);
        business.setSuspensionReason(reason);
        businessRepository.save(business);

        if (reason != null && !reason.isBlank()) {
            emailService.sendBusinessSuspensionEmail(business, reason);
        }
    }

    @Transactional
    public void requestChanges(Long businessId, String reason) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        business.setEntityStatus(EntityStatus.CHANGES_REQUESTED);
        business.setSuspensionReason(reason);
        businessRepository.save(business);

        emailService.sendBusinessChangesRequestedEmail(business, reason);
    }

    @Transactional
    public void approve(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        business.setEntityStatus(EntityStatus.ACTIVE);
        business.setSuspensionReason(null);
        businessRepository.save(business);

        emailService.sendBusinessApprovedEmail(business);
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


