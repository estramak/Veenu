//CRUD operations
package repositories;

import model.Business;
import model.enums.BusinessUserRole;
import model.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByListingId(Long listingId);
    boolean existsByListingId(Long listingId);

    //unique field lookups
    Optional<Business> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Business> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<Business> findByWebsite(String website);
    boolean existsByWebsite(String website);

    //ownership / access
    List<Business> findByBusinessUsers_UserId(Long userId);
    List<Business> findByBusinessUsers_UserIdAndBusinessUsers_Role(
            Long userId, BusinessUserRole role);

    //auth check, does this user have any access to this business?
    boolean existsByIdAndBusinessUsers_UserId(Long businessId, Long userId);

    //status-based queries
    List<Business> findByEntityStatus(EntityStatus entityStatus);

    long countByEntityStatus(EntityStatus entityStatus);

    //verification badge
    List<Business> findByIsVerifiedTrue();

    //businesses flagged with a suspension reason
    @Query("SELECT b FROM Business b WHERE b.entityStatus = 'ACTIVE'")
    List<Business> findAllActive();

    List<Business> findBySuspensionReasonIsNotNull();
}
