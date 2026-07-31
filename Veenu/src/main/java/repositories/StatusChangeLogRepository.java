package repositories;

import model.StatusChangeLog;
import model.enums.AdminEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface StatusChangeLogRepository extends JpaRepository<StatusChangeLog, Long> {

    List<StatusChangeLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(
            AdminEntityType entityType, Long entityId
    );

    List<StatusChangeLog> findAllByOrderByChangedAtDesc(Pageable pageable);
}
