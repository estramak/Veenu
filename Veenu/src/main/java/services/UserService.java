package services;

import dtos.UpdateProfileRequestDto;
import jakarta.transaction.Transactional;
import model.User;
import model.enums.EntityStatus;
import org.springframework.stereotype.Service;
import repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void suspend(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEntityStatus(EntityStatus.SUSPENDED);
        user.setSuspensionReason(reason);
        userRepository.save(user);

        if (reason != null && !reason.isBlank()) {
            emailService.sendUserSuspensionEmail(user, reason);
        }
    }

    @Transactional
    public void requestChanges(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEntityStatus(EntityStatus.CHANGES_REQUESTED);
        user.setSuspensionReason(reason);
        userRepository.save(user);

        emailService.sendUserChangesRequestedEmail(user, reason);
    }

    @Transactional
    public void approve(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEntityStatus(EntityStatus.ACTIVE);
        user.setSuspensionReason(null);
        userRepository.save(user);

        emailService.sendUserApprovedEmail(user);
    }

    @Transactional
    public void ban(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEntityStatus(EntityStatus.BANNED);
        user.setSuspensionReason(reason);
        userRepository.save(user);

        emailService.sendUserBannedEmail(user, reason);
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
        }

        userRepository.save(user);
    }
}
