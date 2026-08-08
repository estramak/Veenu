package services;

import dtos.UpdateListingRequestDto;
import model.Listing;
import model.User;
import model.enums.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.lang.IllegalArgumentException;
import repositories.BusinessRepository;
import repositories.EventRepository;
import repositories.ListingRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock private ListingRepository listingRepository;
    @Mock private BusinessRepository businessRepository;
    @Mock private EventRepository eventRepository;
    @Mock private StatusChangeLogService statusChangeLogService;
    @Mock private EmailService emailService;

    @InjectMocks
    private ListingService listingService;

    private Listing listing;

    @BeforeEach
    void setUp() {
        User creator = new User();
        creator.setId(10L);
        creator.setEmail("creator@example.com");

        listing = new Listing();
        listing.setId(50L);
        listing.setCreatedBy(creator);
        listing.setEntityStatus(EntityStatus.ACTIVE);
        listing.setHasDistinctName(false);
    }

    @Test
    void updateListing_listingNotFound_throwsIllegalArgumentException() {
        when(listingRepository.findById(50L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService
                .updateListing(50L, new UpdateListingRequestDto(), 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Listing not found");
    }

    @Test
    void updateListing_wrongCreator_throwsAccessDeniedException() {
        when(listingRepository.findById(50L))
                .thenReturn(Optional.of(listing));
        assertThatThrownBy(() -> listingService
                .updateListing(50L, new UpdateListingRequestDto(), 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only update listings you submitted");
    }

    @Test
    void updateListing_nameChangeOnDistinctNameListing_throwsIllegalArgumentException() {
        listing.setHasDistinctName(true);

        when(listingRepository.findById(50L))
                .thenReturn(Optional.of(listing));

        UpdateListingRequestDto request = new UpdateListingRequestDto();
        request.setName("New Name");

        assertThatThrownBy(() -> listingService
                .updateListing(50L, request, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The name of a business-attached listing cannot be changed");
    }

    @Test
    void updateListing_nameChangeAllowed_whenNotDistinctName() {
        when(listingRepository.findById(50L)).thenReturn(Optional.of(listing));

        UpdateListingRequestDto request = new UpdateListingRequestDto();
        request.setName("New Name");

        listingService.updateListing(50L, request, 10L);

        assertThat(listing.getName()).isEqualTo("New Name");
    }

    @Test
    void updateListing_changesRequestedStatus_transitionsToPendingAndSendsEmail() {
        listing.setEntityStatus(EntityStatus.CHANGES_REQUESTED);

        when(listingRepository.findById(50L)).thenReturn(Optional.of(listing));

        listingService.updateListing(50L, new UpdateListingRequestDto(), 10L);

        assertThat(listing.getEntityStatus())
                .isEqualTo(EntityStatus.PENDING);
        verify(emailService)
                .sendListingPendingReviewEmail(listing, "creator@example.com");
        verify(listingRepository).save(listing);
    }
}
