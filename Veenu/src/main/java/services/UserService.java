package services;

import dtos.UpdateProfileRequestDto;
import jakarta.transaction.Transactional;
import model.User;
import model.enums.AdminEntityType;
import model.enums.EntityStatus;
import org.springframework.stereotype.Service;
import repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final StatusChangeLogService statusChangeLogService;

    public UserService(
            UserRepository userRepository,
            EmailService emailService,
            StatusChangeLogService statusChangeLogService
    ) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.statusChangeLogService = statusChangeLogService;
    }

    @Transactional
    public void requestChanges(Long userId, String reason, String adminNotes, Long changedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EntityStatus previousStatus = user.getEntityStatus();
        user.setEntityStatus(EntityStatus.CHANGES_REQUESTED);
        user.setSuspensionReason(reason);
        userRepository.save(user);

        statusChangeLogService.log(AdminEntityType.USER, userId,
                previousStatus, EntityStatus.CHANGES_REQUESTED, reason, adminNotes, changedBy);

        emailService.sendUserChangesRequestedEmail(user, reason);
    }

    @Transactional
    public void approve(Long userId, String adminNotes, Long changedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getEntityStatus() == EntityStatus.TAKEN_DOWN) {
            throw new IllegalArgumentException("This user has been permanently taken down and cannot be reinstated");
        }

        EntityStatus previousStatus = user.getEntityStatus();
        user.setEntityStatus(EntityStatus.ACTIVE);
        user.setSuspensionReason(null);
        userRepository.save(user);

        statusChangeLogService.log(AdminEntityType.USER, userId,
                previousStatus, EntityStatus.ACTIVE, null, adminNotes, changedBy);

        emailService.sendUserApprovedEmail(user);
    }

    @Transactional
    public void takeDown(Long userId, String reason, String adminNotes, Long changedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EntityStatus previousStatus = user.getEntityStatus();
        user.setEntityStatus(EntityStatus.TAKEN_DOWN);
        user.setSuspensionReason(reason);
        userRepository.save(user);

        statusChangeLogService.log(AdminEntityType.USER, userId,
                previousStatus, EntityStatus.TAKEN_DOWN, reason, adminNotes, changedBy);

        emailService.sendUserTakenDownEmail(user, reason);
    }

    @Transactional
    public void overrideTakeDown(Long userId, String adminNotes, Long changedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getEntityStatus() != EntityStatus.TAKEN_DOWN) {
            throw new IllegalArgumentException("This user is not currently taken down");
        }

        EntityStatus previousStatus = user.getEntityStatus();
        user.setEntityStatus(EntityStatus.ACTIVE);
        user.setSuspensionReason(null);
        userRepository.save(user);

        statusChangeLogService.log(AdminEntityType.USER, userId,
                previousStatus, EntityStatus.ACTIVE, "Take-down overridden by admin", adminNotes, changedBy);

        emailService.sendUserOverrideTakeDownEmail(user);
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new IllegalArgumentException("Email already in use");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setNeighborhood(request.getNeighborhood());

        // If this user was asked to make changes, submitting this update
        // moves them into PENDING for re-review — matches
        // CHANGES_REQUESTED -> PENDING per Database_Schema.md
        if (user.getEntityStatus() == EntityStatus.CHANGES_REQUESTED) {
            user.setEntityStatus(EntityStatus.PENDING);
            userRepository.save(user);
            emailService.sendUserPendingReviewEmail(user);
            return;
        }

        userRepository.save(user);
    }
}
