package services;

import dtos.BusinessResponseDto;
import dtos.EventResponseDto;
import dtos.ListingDetailDto;
import dtos.ListingSummaryDto;
import model.Business;
import model.Event;
import model.Listing;
import model.enums.EntityStatus;
import org.springframework.stereotype.Service;
import repositories.BusinessRepository;
import repositories.EventRepository;
import repositories.ListingRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final BusinessRepository businessRepository;
    private final EventRepository eventRepository;

    public ListingService(
        ListingRepository listingRepository,
        BusinessRepository businessRepository,
        EventRepository eventRepository
    ) {
        this.listingRepository = listingRepository;
        this.businessRepository = businessRepository;
        this.eventRepository = eventRepository;
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
}
