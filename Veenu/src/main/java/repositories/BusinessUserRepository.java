package repositories;

import model.BusinessUser;
import model.enums.BusinessUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessUserRepository extends JpaRepository<BusinessUser, Long> {

    // membership lookups
    List<BusinessUser> findByBusiness_Id(Long businessId);

    List<BusinessUser> findByUser_Id(Long userId);

    Optional<BusinessUser> findByBusiness_IdAndUser_Id(Long businessId, Long userId);

    boolean existsByBusiness_IdAndUser_Id(Long businessId, Long userId);

    // role-specific queries
    Optional<BusinessUser> findByBusiness_IdAndRole(Long businessId, BusinessUserRole role);

    long countByBusiness_IdAndRole(Long businessId, BusinessUserRole role);

    // removal
    void deleteByBusiness_IdAndUser_Id(Long businessId, Long userId);
}
