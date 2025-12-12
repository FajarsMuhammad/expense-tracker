package com.fajars.expensetracker.user.usecase;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.user.ProfileResponse;
import com.fajars.expensetracker.user.UpdateProfileRequest;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Use case implementation for updating user profile.
 *
 * @since Milestone 6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserProfileUseCase implements UpdateUserProfile {

    private final UserRepository userRepository;
    private final GetUserProfile getUserProfile;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        // Track changes for logging
        Map<String, Object> changes = new HashMap<>();
        boolean hasChanges = false;

        // Update name if changed
        if (request.name() != null && !request.name().equals(user.getName())) {
            changes.put("name.old", user.getName());
            changes.put("name.new", request.name());
            user.setName(request.name());
            hasChanges = true;
        }

        // Update locale if provided and changed
        if (request.locale() != null && !request.locale().equals(user.getLocale())) {
            changes.put("locale.old", user.getLocale());
            changes.put("locale.new", request.locale());
            user.setLocale(request.locale());
            hasChanges = true;
        }

        // Save if there are changes
        if (hasChanges) {
            user.setUpdatedAt(new Date());
            userRepository.save(user);

            // Log business event
            logBusinessEvent(user, changes);

            // Track metrics
            metricsService.incrementCounter("user.profile.updated");

            log.info("Profile updated for user: {}", userId);
        } else {
            log.debug("No changes detected for user profile: {}", userId);
        }

        // Return updated profile
        return getUserProfile.getProfile(userId);
    }

    /**
     * Log business event for profile update.
     */
    private void logBusinessEvent(User user, Map<String, Object> changes) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", user.getId());
        attributes.putAll(changes);

        businessEventLogger.logBusinessEvent("PROFILE_UPDATED", user.getEmail(), attributes);
    }
}
