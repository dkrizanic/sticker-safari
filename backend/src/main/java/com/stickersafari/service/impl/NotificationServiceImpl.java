package com.stickersafari.service.impl;

import com.stickersafari.entity.Notification;
import com.stickersafari.repository.NotificationRepository;
import com.stickersafari.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        notifications.stream()
                .filter(notification -> !notification.isRead())
                .forEach((notification) -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });

        return notifications;
    }

    @Override
    @Transactional
    public List<Notification> getUnreadNotifications(Long collectorId) {
        log.debug("Getting unread notifications for collector ID: {}", collectorId);
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadFalse(collectorId);

        return notifications.stream()
                .filter(notification -> !notification.isRead())
                .map(notification -> {
                    notification.setRead(true);
                    return notificationRepository.save(notification);
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Notification saveNotification(Notification notification) {
        log.info("Saving notification for recipient ID: {}", notification.getRecipient().getId());
        return notificationRepository.save(notification);
    }
}
