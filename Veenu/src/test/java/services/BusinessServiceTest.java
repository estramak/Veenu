package services;

import dtos.BusinessHoursDto;
import dtos.BusinessResponseDto;
import dtos.CreateBusinessRequestDto;
import dtos.UpdateBusinessRequestDto;
import model.*;
import model.enums.BusinessUserRole;
import model.enums.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.*;

import org.springframework.security.access.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock private BusinessRepository businessRepository;
    @Mock private BusinessUserRepository businessUserRepository;
    @Mock private BusinessReportRepository businessReportRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private StatusChangeLogService statusChangeLogService;

    @InjectMocks
    private BusinessService businessService;

    private Business business;
    private BusinessUser owner;
    private Listing listing;

    @BeforeEach
    void setUp() {
        User ownerUser = new User();
        ownerUser.setId(10L);
        ownerUser.setEmail("owner@business.com");

        listing = new Listing();
        listing.setId(100L);

        business = new Business();
        business.setId(1L);
        business.setListing(listing);
        business.setEntityStatus(EntityStatus.ACTIVE);

        owner = new BusinessUser();
        owner.setUser(ownerUser);
        owner.setBusiness(business);
        owner.setRole(BusinessUserRole.OWNER);
    }

    private CreateBusinessRequestDto buildCreateRequest(Long confirmedListingId) {
        CreateBusinessRequestDto request = new CreateBusinessRequestDto();
        request.setName("Corner Cafe");
        request.setEmail("cafe@business.com");
        request.setPhone("555-1234");
        request.setWebsite("https://cornercafe.com");
        request.setLatitude(40.0);
        request.setLongitude(-73.0);
        request.setAddress("1 Main St");
        request.setCity("Springfield");
        request.setState("NY");
        request.setZip("10001");
        request.setConfirmedListingId(confirmedListingId);
        return request;
    }

    @Test
    void createBusiness_creatorNotFound_throwsIllegalArgumentException() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessService.createBusiness(10L, buildCreateRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verifyNoInteractions(businessRepository, listingRepository);
    }

    @Test
    void createBusiness_duplicateEmail_throwsIllegalArgumentException() {
        User creator = new User();
        creator.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(businessRepository.existsByEmail("cafe@business.com")).thenReturn(true);

        assertThatThrownBy(() -> businessService.createBusiness(10L, buildCreateRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A business with this email already exists");

        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void createBusiness_noConfirmedListingId_createNewListing() {
        User creator = new User();
        creator.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(businessRepository.existsByEmail("cafe@business.com")).thenReturn(false);

        Listing newListing = new Listing();
        newListing.setId(200L);
        newListing.setName("Corner Cafe");
        when(listingRepository.save(any(Listing.class))).thenReturn(newListing);

        Business savedBusiness = new Business();
        savedBusiness.setId(1L);
        savedBusiness.setListing(newListing);
        savedBusiness.setEmail("cafe@business.com");
        savedBusiness.setEntityStatus(EntityStatus.ACTIVE);
        savedBusiness.setIsVerified(false);
        savedBusiness.setFlaggedForReview(false);
        savedBusiness.setSubmittedBy(creator);
        when(businessRepository.save(any(Business.class))).thenReturn(savedBusiness);

        BusinessResponseDto response = businessService.createBusiness(10L, buildCreateRequest(null));

        verify(listingRepository).save(argThat(l -> l.getName().equals("Corner Cafe") && l.getLatitude() == 40.0));
        verify(listingRepository, never()).findById(any());
        assertThat(response.getListingId()).isEqualTo(200L);
        assertThat(response.getName()).isEqualTo("Corner Cafe");
    }

    @Test
    void createBusiness_confirmedListingNotFound_throwsIllegalArgumentException() {
        User creator = new User();
        creator.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(businessRepository.existsByEmail("cafe@business.com")).thenReturn(false);
        when(listingRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessService.createBusiness(10L, buildCreateRequest(200L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Confirmed listing not found");

        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void createBusiness_confirmedListingAlreadyClaimed_throwsIllegalArgumentException() {
        User creator = new User();
        creator.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(businessRepository.existsByEmail("cafe@business.com")).thenReturn(false);

        Listing existing = new Listing();
        existing.setId(200L);
        when(listingRepository.findById(200L)).thenReturn(Optional.of(existing));
        when(businessRepository.existsByListingId(200L)).thenReturn(true);

        assertThatThrownBy(() -> businessService.createBusiness(10L, buildCreateRequest(200L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A business is already registered at this location");

        verify(businessRepository, never()).save(any(Business.class));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void createBusiness_confirmedListingId_happyPath_reusesExistingListing() {
        User creator = new User();
        creator.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(businessRepository.existsByEmail("cafe@business.com")).thenReturn(false);

        Listing existing = new Listing();
        existing.setId(200L);
        existing.setName("Corner Cafe");
        when(listingRepository.findById(200L)).thenReturn(Optional.of(existing));
        when(businessRepository.existsByListingId(200L)).thenReturn(false);

        Business savedBusiness = new Business();
        savedBusiness.setId(1L);
        savedBusiness.setListing(existing);
        savedBusiness.setEmail("cafe@business.com");
        savedBusiness.setEntityStatus(EntityStatus.ACTIVE);
        when(businessRepository.save(any(Business.class))).thenReturn(savedBusiness);

        BusinessResponseDto response = businessService.createBusiness(10L, buildCreateRequest(200L));

        verify(listingRepository, never()).save(any(Listing.class));
        assertThat(response.getListingId()).isEqualTo(200L);
        assertThat(response.getName()).isEqualTo("Corner Cafe");
    }

    @Test
    void updateBusiness_businessNotFound_throwsIllegalArgumentException() {
        when(businessRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessService
                .updateBusiness(1L, new UpdateBusinessRequestDto(), 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Business not found");
    }

    @Test
    void updateBusiness_noOwner_throwsIllegalArgumentException() {
        when(businessRepository
                .findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository
                .findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessService
                .updateBusiness(1L, new UpdateBusinessRequestDto(), 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This business has no owner");
    }

    @Test
    void updateBusiness_wrongOwner_throwsAccessDeniedException() {
        when(businessRepository
                .findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository
                .findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> businessService
                .updateBusiness(1L, new UpdateBusinessRequestDto(), 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only the business owner can update business details");
    }

    @Test
    void updateBusiness_duplicateEmail_throwsIllegalArgumentException() {
        when(businessRepository
                .findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository
                .findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.of(owner));
        when(businessRepository
                .existsByEmailAndIdNot("taken@biz.com", 1L))
                .thenReturn(true);

        UpdateBusinessRequestDto request = new UpdateBusinessRequestDto();
        request.setEmail("taken@biz.com");

        assertThatThrownBy(() -> businessService
                .updateBusiness(1L, request, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A business with this email already exists");
    }

    @Test
    void updateBusiness_duplicatePhone_throwsIllegalArgumentException() {
        when(businessRepository
                .findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository
                .findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.of(owner));
        when(businessRepository
                .existsByPhoneAndIdNot("555-0100", 1L))
                .thenReturn(true);

        UpdateBusinessRequestDto request = new UpdateBusinessRequestDto();
        request.setPhone("555-0100");

        assertThatThrownBy(() -> businessService
                .updateBusiness(1L, request, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A business with this phone already exists");
    }

    @Test
    void updateBusiness_duplicateWebsite_throwsIllegalArgumentException() {
        when(businessRepository
                .findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository
                .findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.of(owner));
        when(businessRepository
                .existsByWebsiteAndIdNot("biz.com", 1L))
                .thenReturn(true);

        UpdateBusinessRequestDto request = new UpdateBusinessRequestDto();
        request.setWebsite("biz.com");

        assertThatThrownBy(() -> businessService
                .updateBusiness(1L, request, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A business with this website already exists");
    }

    @Test
    void updateBusiness_happyPath_updateBusinessAndListingFields() {
        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository.findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.of(owner));

        UpdateBusinessRequestDto request = new UpdateBusinessRequestDto();
        request.setPhone("555-0199");
        request.setCity("Seattle");

        businessService.updateBusiness(1L, request, 10L);

        assertThat(business.getPhone())
                .isEqualTo("555-0199");
        assertThat(listing.getCity())
                .isEqualTo("Seattle");

        verify(listingRepository).save(listing);
        verify(businessRepository).save(business);
        verify(emailService, never()).sendBusinessPendingEmail(any(), any(), any());
    }

    @Test
    void updateBusiness_hoursProvided_replacesExistingHours() {
        business.getHours().add(new BusinessHours());

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository.findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.of(owner));

        BusinessHoursDto mondayHours = new BusinessHoursDto();
        mondayHours.setDayOfWeek(DayOfWeek.MONDAY);
        mondayHours.setOpenTime(LocalTime.of(9, 0));
        mondayHours.setCloseTime(LocalTime.of(17, 0));
        mondayHours.setClosed(false);

        UpdateBusinessRequestDto request = new UpdateBusinessRequestDto();
        request.setHours(List.of(mondayHours));

        businessService.updateBusiness(1L, request, 10L);
        assertThat(business.getHours()).hasSize(1);
        BusinessHours saved = business.getHours().get(0);

        assertThat(saved.getDayOfWeek())
                .isEqualTo(DayOfWeek.MONDAY);
        assertThat(saved.getOpenTime())
                .isEqualTo(LocalTime.of(9,0));
        assertThat(saved.getOpenTime())
                .isEqualTo(LocalTime.of(9,0));
        assertThat(saved.getBusiness())
                .isSameAs(business);
    }

    @Test
    void updateBusiness_changeRequestedStatus_transitionsToPendingAndSendingEmail() {
        business.setEntityStatus(EntityStatus.CHANGES_REQUESTED);

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));
        when(businessUserRepository.findByBusiness_IdAndRole(1L, BusinessUserRole.OWNER))
                .thenReturn(Optional.of(owner));

        businessService.updateBusiness(1L, new UpdateBusinessRequestDto(), 10L);

        assertThat(business.getEntityStatus())
                .isEqualTo(EntityStatus.PENDING);

        verify(emailService).sendBusinessPendingEmail(business, null, "owner@business.com");
        verify(businessRepository).save(business);
    }
}
