package com.bloodbank.service;

import com.bloodbank.entity.ActivityLog;
import com.bloodbank.entity.User;
import com.bloodbank.repository.ActivityLogRepository;
import com.bloodbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(String action, String details) {
        User currentUser = getCurrentUser();
        ActivityLog log = ActivityLog.builder()
                .user(currentUser)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
