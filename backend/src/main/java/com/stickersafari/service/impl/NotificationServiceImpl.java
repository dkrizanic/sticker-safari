package com.stickersafari.service.impl;

import com.stickersafari.entity.Notification;
import com.stickersafari.repository.NotificationRepository;
import com.stickersafari.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public List<Notification> getNotifications(Long collectorId) {
        log.debug("Getting notifications for collector ID: {}", collectorId);
        List<Notification> notifications = notificationRepository.findByRecipientId(collectorId);
        
        // Clean up duplicates - keep track of seen messages
        Map<String, Notification> uniqueNotifications = new LinkedHashMap<>();
        List<Long> duplicateIds = new ArrayList<>();
        
        for (Notification notification : notifications) {
            String key = notification.getMessage() + "_" + notification.isRead();
            if (uniqueNotifications.containsKey(key)) {
                // This is a duplicate, mark for deletion
                duplicateIds.add(notification.getId());
                log.debug("Found duplicate notification ID {} for collector {}", notification.getId(), collectorId);
            } else {
                uniqueNotifications.put(key, notification);
            }
        }
        
        // Delete duplicates
        if (!duplicateIds.isEmpty()) {
            log.info("Deleting {} duplicate notifications for collector {}", duplicateIds.size(), collectorId);
            notificationRepository.deleteAllById(duplicateIds);
        }
        
        // Sort by createdAt descending (newest first)
        return uniqueNotifications.values().stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Notification> getUnreadNotifications(Long collectorId) {
        log.debug("Getting unread notifications for collector ID: {}", collectorId);
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadFalse(collectorId);
        
        // Sort by createdAt descending (newest first)
        return notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Notification> getReadNotifications(Long collectorId) {
        log.debug("Getting read notifications for collector ID: {}", collectorId);
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadTrue(collectorId);
        
        // Sort by createdAt descending (newest first)
        return notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> getNotificationsByFilter(Long collectorId, String filter) {
        log.debug("Getting notifications for collector ID: {} with filter: {}", collectorId, filter);
        
        return switch (filter.toLowerCase()) {
            case "read" -> getReadNotifications(collectorId);
            case "all" -> getNotifications(collectorId);
            case "unread" -> getUnreadNotifications(collectorId);
            default -> {
                log.warn("Invalid filter value: {}, defaulting to 'unread'", filter);
                yield getUnreadNotifications(collectorId);
            }
        };
    }

    @Override
    @Transactional
    public Notification saveNotification(Notification notification) {
        log.info("Saving notification for recipient ID: {}", notification.getRecipient().getId());
        return notificationRepository.save(notification);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        log.debug("Marking notification {} as read", notificationId);
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
    
    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.debug("Deleting notification {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }
    
    @Override
    public boolean notificationExists(Long collectorId, String message) {
        // Check if an unread notification with this message already exists
        List<Notification> existingNotifications = notificationRepository.findByRecipientIdAndMessageAndReadFalse(collectorId, message);
        
        // If duplicates exist, keep only one and delete the rest
        if (existingNotifications.size() > 1) {
            log.warn("Found {} duplicate notifications for collector {} with message '{}', cleaning up", 
                     existingNotifications.size(), collectorId, message);
            // Keep the first one, delete the rest
            for (int i = 1; i < existingNotifications.size(); i++) {
                notificationRepository.deleteById(existingNotifications.get(i).getId());
            }
        }
        
        return !existingNotifications.isEmpty();
    }
}
