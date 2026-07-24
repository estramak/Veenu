package repositories;

import model.BusinessReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessReportRepository extends JpaRepository<BusinessReport, Long> {

    //duplicate report prevention
    boolean existsByBusiness_IdAndReportedBy_Id(Long businessId, Long userId);

    //threshold check
    long countByBusiness_Id(Long businessId);

    //admin visibility of reported businesses meeting threshold
    List<BusinessReport> findByBusiness_Id(Long businessId);
}
