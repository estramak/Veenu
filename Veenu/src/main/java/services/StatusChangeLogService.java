package services;

import model.StatusChangeLog;
import model.enums.AdminEntityType;
import model.enums.EntityStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import repositories.StatusChangeLogRepository;

import java.util.List;

@Service
public class StatusChangeLogService {

    private final StatusChangeLogRepository logRepository;

    public StatusChangeLogService(StatusChangeLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void log(
            AdminEntityType entityType,
            Long entityId,
            EntityStatus previousStatus,
            EntityStatus newStatus,
            String reason,
            String adminNotes,
            Long changedBy
    ) {
        StatusChangeLog entry = new StatusChangeLog();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setPreviousStatus(previousStatus);
        entry.setNewStatus(newStatus);
        entry.setReason(reason);
        entry.setAdminNotes(adminNotes);
        entry.setChangedBy(changedBy);
        logRepository.save(entry);
    }

    public List<StatusChangeLog> getHistory(AdminEntityType entityType, Long entityId) {
        return logRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
    }

    public List<StatusChangeLog> getRecent(int limit) {
        return logRepository.findAllByOrderByChangedAtDesc(PageRequest.of(0, limit));
    }
}
