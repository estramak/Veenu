package services;

import dtos.AdminQueueItemDto;
import dtos.AdminQueueResponseDto;
import model.Business;
import model.Listing;
import model.User;
import model.enums.EntityStatus;
import org.springframework.stereotype.Service;
import repositories.BusinessRepository;
import repositories.ListingRepository;
import repositories.UserRepository;

import java.util.List;

@Service
public class AdminQueueService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public AdminQueueService(
            BusinessRepository businessRepository,
            UserRepository userRepository,
            ListingRepository listingRepository
    ) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    public AdminQueueResponseDto getQueue() {
        List<AdminQueueItemDto> flaggedBusinesses = businessRepository
                .findByFlaggedForReviewTrue()
                .stream()
                .map(this::toBusinessItem)
                .toList();

        List<AdminQueueItemDto> pendingBusinesses = businessRepository
                .findByEntityStatus(EntityStatus.PENDING)
                .stream()
                .map(this::toBusinessItem)
                .toList();

        List<AdminQueueItemDto> pendingUsers = userRepository
                .findByEntityStatus(EntityStatus.PENDING)
                .stream()
                .map(this::toUserItem)
                .toList();

        List<AdminQueueItemDto> pendingListings = listingRepository
                .findByEntityStatus(EntityStatus.PENDING)
                .stream()
                .map(this::toListingItem)
                .toList();

        List<AdminQueueItemDto> takenDownListings = listingRepository
                .findByEntityStatus(EntityStatus.TAKEN_DOWN)
                .stream()
                .map(this::toListingItem)
                .toList();

        return AdminQueueResponseDto.builder()
                .flaggedBusinesses(flaggedBusinesses)
                .pendingBusinesses(pendingBusinesses)
                .pendingUsers(pendingUsers)
                .pendingListings(pendingListings)
                .suspendedListings(takenDownListings)
                .build();
    }

    private AdminQueueItemDto toBusinessItem(Business business) {
        return AdminQueueItemDto.builder()
                .id(business.getId())
                .entityType("BUSINESS")
                .name(business.getListing().getName())
                .status(business.getEntityStatus().name())
                .reason(business.getSuspensionReason())
                .flagged(business.getFlaggedForReview())
                .build();
    }

    private AdminQueueItemDto toUserItem(User user) {
        return AdminQueueItemDto.builder()
                .id(user.getId())
                .entityType("USER")
                .name(user.getDisplayName())
                .status(user.getEntityStatus().name())
                .reason(user.getSuspensionReason())
                .flagged(null)
                .build();
    }

    private AdminQueueItemDto toListingItem(Listing listing) {
        return AdminQueueItemDto.builder()
                .id(listing.getId())
                .entityType("LISTING")
                .name(listing.getName())
                .status(listing.getEntityStatus().name())
                .reason(listing.getSuspensionReason())
                .flagged(null)
                .build();
    }
}
