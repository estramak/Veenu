package services;

import dtos.CreateEventRequestDto;
import dtos.EventResponseDto;
import model.Business;
import model.Event;
import model.Listing;
import model.User;
import model.enums.EntityStatus;
import model.enums.LocationType;
import org.springframework.stereotype.Service;
import repositories.BusinessRepository;
import repositories.BusinessUserRepository;
import repositories.EventRepository;
import repositories.ListingRepository;
import repositories.UserRepository;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ListingRepository listingRepository;
    private final BusinessRepository businessRepository;
    private final BusinessUserRepository businessUserRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;

    public EventService(
            EventRepository eventRepository,
            ListingRepository listingRepository,
            BusinessRepository businessRepository,
            BusinessUserRepository businessUserRepository,
            UserRepository userRepository,
            GeocodingService geocodingService
    ) {
        this.eventRepository = eventRepository;
        this.listingRepository = listingRepository;
        this.businessRepository = businessRepository;
        this.businessUserRepository = businessUserRepository;
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
    }

    // Matches BusinessService's proximity threshold for duplicate detection
    private static final double DUPLICATE_LISTING_RADIUS_METERS = 20.0;

    public EventResponseDto createEvent(Long creatorUserId, CreateEventRequestDto request) {
        boolean hasLocation = request.getListingId() != null
                || request.getBusinessId() != null
                || (request.getLatitude() != null && request.getLongitude() != null);

        if (!hasLocation) {
            throw new IllegalArgumentException(
                    "An event must be linked to a listing, a business, or a location");
        }

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Business business = null;
        Listing listing;
        boolean postedByOwner = false;

        if (request.getBusinessId() != null) {
            business = businessRepository.findById(request.getBusinessId())
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            listing = business.getListing();

            // The flag: true only if the creator is an actual member
            // (OWNER or MANAGER) of this business via BusinessUser.
            // Anyone else can still post the event, but it's marked as
            // community-submitted rather than official.
            postedByOwner = businessUserRepository.existsByBusiness_IdAndUser_Id(
                    business.getId(), creatorUserId
            );
        } else if (request.getListingId() != null) {
            listing = listingRepository.findById(request.getListingId())
                    .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        } else {
            listing = resolveOrCreateListing(request);
        }

        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setListing(listing);
        event.setBusiness(business);
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setIsFree(request.getIsFree());
        event.setPrice(request.getIsFree() ? null : request.getPrice());
        event.setCapacity(request.getCapacity());
        event.setRegistrationUrl(request.getRegistrationUrl());
        event.setCreator(creator);
        event.setPostedByOwner(postedByOwner);

        Event saved = eventRepository.save(event);
        return toResponseDto(saved);
    }

    private Listing resolveOrCreateListing(CreateEventRequestDto request) {
        if (request.getConfirmedListingId() != null) {
            Listing existing = listingRepository.findById(request.getConfirmedListingId())
                    .orElseThrow(() -> new IllegalArgumentException("Confirmed listing not found"));
            if (businessRepository.existsByListingId(existing.getId())) {
                throw new IllegalArgumentException("A business is already registered at this location");
            }
            return existing;
        }

        Listing listing = new Listing();
        listing.setName(request.getName());
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
        listing.setHasDistinctName(true);

        return listingRepository.save(listing);
    }

    private EventResponseDto toResponseDto(Event event) {
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
